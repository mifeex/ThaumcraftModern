package com.thaumcraftmodern.client.render;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.SimpleUnbakedGeometry;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Small OBJ loader for legacy TC4 assets that remains valid when OptiFine
 * changes {@link DefaultVertexFormat#BLOCK} during a shader reload.
 *
 * <p>Forge 47's OBJ path captures the quad stride statically, but later asks
 * the live vertex format for offsets. Old OptiFine can therefore combine a
 * 32-int allocation with 36-int offsets. This loader snapshots both stride
 * and offsets together for every bake.</p>
 */
public final class ReloadSafeObjLoader
        implements IGeometryLoader<ReloadSafeObjLoader.Geometry> {
    public static final ReloadSafeObjLoader INSTANCE =
            new ReloadSafeObjLoader();

    private ReloadSafeObjLoader() {
    }

    @Override
    public Geometry read(
            JsonObject json,
            JsonDeserializationContext context
    ) throws JsonParseException {
        if (!json.has("model")) {
            throw new JsonParseException(
                    "Reload-safe OBJ loader requires a 'model' key"
            );
        }
        ResourceLocation location = new ResourceLocation(
                json.get("model").getAsString()
        );
        boolean flipV = GsonHelper.getAsBoolean(json, "flip_v", false);
        boolean automaticCulling = GsonHelper.getAsBoolean(
                json,
                "automatic_culling",
                true
        );
        return parse(location, flipV, automaticCulling);
    }

    private static Geometry parse(
            ResourceLocation location,
            boolean flipV,
            boolean automaticCulling
    ) {
        Resource resource = Minecraft.getInstance()
                .getResourceManager()
                .getResource(location)
                .orElseThrow(() -> new JsonParseException(
                        "Missing OBJ model " + location
                ));
        List<Vector3f> positions = new ArrayList<>();
        List<Vector2> textureCoordinates = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Face> faces = new ArrayList<>();
        String component = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        resource.open(),
                        StandardCharsets.UTF_8
                )
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                String[] values = trimmed.split("\\s+");
                switch (values[0]) {
                    case "v" -> positions.add(new Vector3f(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2]),
                            Float.parseFloat(values[3])
                    ));
                    case "vt" -> textureCoordinates.add(new Vector2(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2])
                    ));
                    case "vn" -> normals.add(new Vector3f(
                            Float.parseFloat(values[1]),
                            Float.parseFloat(values[2]),
                            Float.parseFloat(values[3])
                    ));
                    case "o", "g" -> component = values[1];
                    case "f" -> faces.add(parseFace(values, component));
                    default -> {
                        // Materials all resolve through the model's #texture0.
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            throw new JsonParseException(
                    "Could not read OBJ model " + location,
                    exception
            );
        }
        return new Geometry(
                List.copyOf(positions),
                List.copyOf(textureCoordinates),
                List.copyOf(normals),
                List.copyOf(faces),
                flipV,
                automaticCulling
        );
    }

    private static Face parseFace(String[] values, String component) {
        Vertex[] vertices = new Vertex[values.length - 1];
        for (int index = 1; index < values.length; index++) {
            String[] indices = values[index].split("/");
            vertices[index - 1] = new Vertex(
                    parseIndex(indices[0]),
                    indices.length > 1 && !indices[1].isEmpty()
                            ? parseIndex(indices[1])
                            : -1,
                    indices.length > 2 && !indices[2].isEmpty()
                            ? parseIndex(indices[2])
                            : -1
            );
        }
        return new Face(component, vertices);
    }

    private static int parseIndex(String value) {
        int index = Integer.parseInt(value);
        if (index <= 0) {
            throw new JsonParseException(
                    "Negative OBJ indices are not supported by this loader"
            );
        }
        return index - 1;
    }

    public static final class Geometry
            extends SimpleUnbakedGeometry<Geometry> {
        private final List<Vector3f> positions;
        private final List<Vector2> textureCoordinates;
        private final List<Vector3f> normals;
        private final List<Face> faces;
        private final boolean flipV;
        private final boolean automaticCulling;

        private Geometry(
                List<Vector3f> positions,
                List<Vector2> textureCoordinates,
                List<Vector3f> normals,
                List<Face> faces,
                boolean flipV,
                boolean automaticCulling
        ) {
            this.positions = positions;
            this.textureCoordinates = textureCoordinates;
            this.normals = normals;
            this.faces = faces;
            this.flipV = flipV;
            this.automaticCulling = automaticCulling;
        }

        @Override
        protected void addQuads(
                IGeometryBakingContext owner,
                IModelBuilder<?> modelBuilder,
                ModelBaker baker,
                Function<Material, TextureAtlasSprite> spriteGetter,
                ModelState modelTransform,
                ResourceLocation modelLocation
        ) {
            TextureAtlasSprite texture = spriteGetter.apply(
                    owner.getMaterial("texture0")
            );
            Transformation root = owner.getRootTransform();
            Transformation transform = root.isIdentity()
                    ? modelTransform.getRotation()
                    : modelTransform.getRotation().compose(root);
            if (!transform.isIdentity()) {
                transform = transform.blockCenterToCorner();
            }
            VertexLayout layout = VertexLayout.capture();

            for (Face face : faces) {
                if (!owner.isComponentVisible(face.component(), true)) {
                    continue;
                }
                Quad quad = bakeFace(face, texture, transform, layout);
                if (automaticCulling && quad.cullDirection() != null) {
                    modelBuilder.addCulledFace(
                            quad.cullDirection(),
                            quad.bakedQuad()
                    );
                } else {
                    modelBuilder.addUnculledFace(quad.bakedQuad());
                }
            }
        }

        @Override
        public Set<String> getConfigurableComponentNames() {
            Set<String> components = new HashSet<>();
            for (Face face : faces) {
                components.add(face.component());
            }
            return Set.copyOf(components);
        }

        private Quad bakeFace(
                Face face,
                TextureAtlasSprite texture,
                Transformation transform,
                VertexLayout layout
        ) {
            Vector3f calculatedNormal = faceNormal(face);
            int[] data = new int[layout.stride() * 4];
            Direction direction = Direction.DOWN;
            Vector3f firstNormal = null;
            Vector4f[] transformedPositions = new Vector4f[4];

            for (int corner = 0; corner < 4; corner++) {
                Vertex vertex = face.vertices()[
                        Math.min(corner, face.vertices().length - 1)
                ];
                Vector3f sourcePosition = positions.get(vertex.position());
                Vector4f position = new Vector4f(sourcePosition, 1.0F);
                Vector3f normal = vertex.normal() >= 0
                        ? new Vector3f(normals.get(vertex.normal()))
                        : new Vector3f(calculatedNormal);
                if (!transform.isIdentity()) {
                    transform.transformPosition(position);
                    transform.transformNormal(normal);
                }
                normal.normalize();
                transformedPositions[corner] = position;
                if (firstNormal == null) {
                    firstNormal = new Vector3f(normal);
                    direction = Direction.getNearest(
                            normal.x(),
                            normal.y(),
                            normal.z()
                    );
                }

                Vector2 uv = vertex.textureCoordinate() >= 0
                        ? textureCoordinates.get(vertex.textureCoordinate())
                        : defaultUv(corner);
                int base = corner * layout.stride();
                putFloat(data, base + layout.position(), position.x());
                putFloat(data, base + layout.position() + 1, position.y());
                putFloat(data, base + layout.position() + 2, position.z());
                data[base + layout.color()] = 0xFFFFFFFF;
                putFloat(
                        data,
                        base + layout.uv0(),
                        texture.getU(uv.u() * 16.0F)
                );
                putFloat(
                        data,
                        base + layout.uv0() + 1,
                        texture.getV(
                                (flipV ? 1.0F - uv.v() : uv.v()) * 16.0F
                        )
                );
                data[base + layout.uv2()] = 0;
                data[base + layout.normal()] = packNormal(normal);
            }
            Direction cull = automaticCulling
                    ? cullDirection(transformedPositions, firstNormal)
                    : null;
            return new Quad(
                    new BakedQuad(
                            data,
                            -1,
                            direction,
                            texture,
                            true,
                            true
                    ),
                    cull
            );
        }

        private Vector3f faceNormal(Face face) {
            Vector3f origin = positions.get(face.vertices()[0].position());
            Vector3f edgeA = new Vector3f(
                    positions.get(face.vertices()[1].position())
            ).sub(origin);
            Vector3f edgeB = new Vector3f(
                    positions.get(face.vertices()[2].position())
            ).sub(origin);
            return edgeA.cross(edgeB).normalize();
        }
    }

    private static Direction cullDirection(
            Vector4f[] positions,
            Vector3f normal
    ) {
        if (normal == null) {
            return null;
        }
        for (Direction direction : Direction.values()) {
            float boundary = direction.getAxisDirection()
                    == Direction.AxisDirection.POSITIVE ? 1.0F : 0.0F;
            boolean allOnBoundary = true;
            for (Vector4f position : positions) {
                float coordinate = switch (direction.getAxis()) {
                    case X -> position.x();
                    case Y -> position.y();
                    case Z -> position.z();
                };
                allOnBoundary &= Math.abs(coordinate - boundary) < 1.0E-5F;
            }
            float normalCoordinate = switch (direction.getAxis()) {
                case X -> normal.x();
                case Y -> normal.y();
                case Z -> normal.z();
            };
            if (allOnBoundary
                    && Math.signum(normalCoordinate)
                    == direction.getAxisDirection().getStep()) {
                return direction;
            }
        }
        return null;
    }

    private static Vector2 defaultUv(int corner) {
        return switch (corner) {
            case 0 -> new Vector2(0.0F, 0.0F);
            case 1 -> new Vector2(0.0F, 1.0F);
            case 2 -> new Vector2(1.0F, 1.0F);
            default -> new Vector2(1.0F, 0.0F);
        };
    }

    private static void putFloat(int[] data, int index, float value) {
        data[index] = Float.floatToRawIntBits(value);
    }

    private static int packNormal(Vector3f normal) {
        return ((int) (normal.x() * 127.0F) & 0xFF)
                | (((int) (normal.y() * 127.0F) & 0xFF) << 8)
                | (((int) (normal.z() * 127.0F) & 0xFF) << 16);
    }

    private record VertexLayout(
            int stride,
            int position,
            int color,
            int uv0,
            int uv2,
            int normal
    ) {
        private static VertexLayout capture() {
            VertexFormat format = DefaultVertexFormat.BLOCK;
            return new VertexLayout(
                    format.getIntegerSize(),
                    offset(format, DefaultVertexFormat.ELEMENT_POSITION),
                    offset(format, DefaultVertexFormat.ELEMENT_COLOR),
                    offset(format, DefaultVertexFormat.ELEMENT_UV0),
                    offset(format, DefaultVertexFormat.ELEMENT_UV2),
                    offset(format, DefaultVertexFormat.ELEMENT_NORMAL)
            );
        }

        private static int offset(
                VertexFormat format,
                VertexFormatElement element
        ) {
            int index = format.getElements().indexOf(element);
            if (index < 0) {
                throw new IllegalStateException(
                        "Active block vertex format is missing " + element
                );
            }
            return format.getOffset(index) / Integer.BYTES;
        }
    }

    private record Vector2(float u, float v) {
    }

    private record Vertex(
            int position,
            int textureCoordinate,
            int normal
    ) {
    }

    private record Face(String component, Vertex[] vertices) {
    }

    private record Quad(BakedQuad bakedQuad, Direction cullDirection) {
    }
}
