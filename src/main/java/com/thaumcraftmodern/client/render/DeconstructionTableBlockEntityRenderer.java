package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.DeconstructionTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Direct modern rendering of TC4 TileDeconstructionTableRenderer. */
public final class DeconstructionTableBlockEntityRenderer
        implements BlockEntityRenderer<DeconstructionTableBlockEntity> {
    private static final float THAUMOMETER_SCALE = 0.8F;
    private static final double THAUMOMETER_CENTER_X = 0.84D;
    private static final double THAUMOMETER_CENTER_Y = 0.84D;
    private static final double THAUMOMETER_CENTER_Z = 1.128D;
    private static final double THAUMOMETER_TABLE_Y = 1.026D;
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "deconstruction_table"
            ),
            "main"
    );
    public static final ResourceLocation TABLE_TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/decontable.png"
    );
    private final DeconstructionTableModel model;
    private final ClassicThaumometerItemRenderer thaumometerRenderer =
            new ClassicThaumometerItemRenderer();

    public DeconstructionTableBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
        model = new DeconstructionTableModel(context.bakeLayer(LAYER));
    }

    @Override
    public void render(
            DeconstructionTableBlockEntity table,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        renderTable(poses, buffers, packedLight, packedOverlay);
        renderThaumometer(
                thaumometerRenderer,
                poses,
                buffers,
                packedLight,
                packedOverlay
        );

        float ticks = table.getLevel() == null
                ? partialTick
                : table.getLevel().getGameTime() + partialTick;
        ItemStack input = table.getItem(DeconstructionTableBlockEntity.INPUT_SLOT);
        if (!input.isEmpty()) {
            renderInput(
                    table,
                    input,
                    ticks,
                    poses,
                    buffers,
                    packedLight,
                    packedOverlay
            );
        }
        if (table.aspectId() != null) {
            AspectRegistryRuntime.find(table.aspectId()).ifPresent(aspect ->
                    renderAspect(
                            aspect,
                            ticks,
                            poses,
                            buffers,
                            packedLight,
                            packedOverlay
                    )
            );
        }
    }

    private void renderTable(
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        poses.pushPose();
        poses.translate(0.5D, 1.0D, 0.5D);
        poses.mulPose(Axis.XP.rotationDegrees(180.0F));
        model.render(
                poses,
                buffers.getBuffer(RenderType.entityCutoutNoCull(TABLE_TEXTURE)),
                packedLight,
                packedOverlay
        );
        poses.popPose();
    }

    static void renderThaumometer(
            ClassicThaumometerItemRenderer renderer,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        poses.pushPose();
        poses.translate(0.5D, THAUMOMETER_TABLE_Y, 0.5D);
        poses.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poses.mulPose(Axis.YP.rotationDegrees(180.0F));
        poses.scale(
                THAUMOMETER_SCALE,
                THAUMOMETER_SCALE,
                THAUMOMETER_SCALE
        );
        // NONE uses the original Forge OBJ block-centred coordinate space.
        // Rebase its actual mesh bounds before rotating so the hexagonal frame
        // fills the inset tabletop square without drifting towards one edge.
        poses.translate(
                -THAUMOMETER_CENTER_X,
                -THAUMOMETER_CENTER_Y,
                -THAUMOMETER_CENTER_Z
        );
        renderer.renderByItem(
                ModItems.THAUMOMETER.get().getDefaultInstance(),
                ItemDisplayContext.NONE,
                poses,
                buffers,
                packedLight,
                packedOverlay
        );
        poses.popPose();
    }

    private static void renderInput(
            DeconstructionTableBlockEntity table,
            ItemStack input,
            float ticks,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack displayed = input.copy();
        displayed.setCount(1);
        poses.pushPose();
        poses.translate(
                0.5D,
                1.15D + Math.sin(
                        Math.sin(ticks / 14.0F) * 0.2F + 0.2F
                ) * 0.1D,
                0.5D
        );
        poses.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
        poses.scale(0.75F, 0.75F, 0.75F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                displayed,
                ItemDisplayContext.GROUND,
                packedLight,
                packedOverlay,
                poses,
                buffers,
                table.getLevel(),
                0
        );
        poses.popPose();
    }

    private static void renderAspect(
            AspectDefinition aspect,
            float ticks,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        ResourceLocation texture = new ResourceLocation(aspect.icon());
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityTranslucent(texture)
        );
        float red = ((aspect.color() >> 16) & 0xFF) / 255.0F;
        float green = ((aspect.color() >> 8) & 0xFF) / 255.0F;
        float blue = (aspect.color() & 0xFF) / 255.0F;
        float radius = 0.12F;

        poses.pushPose();
        poses.translate(0.5D, 1.081D, 0.5D);
        poses.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
        PoseStack.Pose pose = poses.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        aspectVertex(vertices, matrix, normal, -radius, -radius,
                0.0F, 1.0F, red, green, blue, packedLight, packedOverlay);
        aspectVertex(vertices, matrix, normal, -radius, radius,
                0.0F, 0.0F, red, green, blue, packedLight, packedOverlay);
        aspectVertex(vertices, matrix, normal, radius, radius,
                1.0F, 0.0F, red, green, blue, packedLight, packedOverlay);
        aspectVertex(vertices, matrix, normal, radius, -radius,
                1.0F, 1.0F, red, green, blue, packedLight, packedOverlay);
        poses.popPose();
    }

    private static void aspectVertex(
            VertexConsumer vertices,
            Matrix4f matrix,
            Matrix3f normal,
            float x,
            float z,
            float u,
            float v,
            float red,
            float green,
            float blue,
            int packedLight,
            int packedOverlay
    ) {
        vertices.vertex(matrix, x, 0.0F, z)
                .color(red, green, blue, 0.8F)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
