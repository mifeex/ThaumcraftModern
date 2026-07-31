package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Modern baked equivalent of TC4's {@code ModelWand}.
 *
 * <p>TC4 used one exact 2x18 rod and two exact 2x2 cap cubes for every form.
 * Staff length and the layered scepter head came from renderer transforms,
 * not from larger UV-mapped cubes. Keeping that split is important: changing
 * the cube dimensions also changes the UV span and makes the original 32x32
 * textures blur, wrap or appear detached.</p>
 */
final class ClassicWandModel {
    private static final int RUNE_COUNT = 16;
    private static final String ROD = "rod";
    private static final String TOP_CAP = "top_cap";
    private static final String BOTTOM_CAP = "bottom_cap";

    private final ModelPart rod;
    private final ModelPart topCap;
    private final ModelPart bottomCap;

    ClassicWandModel() {
        ModelPart root = createBodyLayer().bakeRoot();
        rod = root.getChild(ROD);
        topCap = root.getChild(TOP_CAP);
        bottomCap = root.getChild(BOTTOM_CAP);
    }

    private static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                ROD,
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .mirror()
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 18.0F, 2.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                TOP_CAP,
                cap(),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                BOTTOM_CAP,
                cap(),
                PartPose.offset(0.0F, 20.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 32);
    }

    private static CubeListBuilder cap() {
        return CubeListBuilder.create()
                .texOffs(0, 0)
                .mirror()
                .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F);
    }

    void renderRod(
            PoseStack poseStack,
            VertexConsumer vertices,
            int light,
            int overlay,
            float alpha
    ) {
        rod.render(poseStack, vertices, light, overlay, 1, 1, 1, alpha);
    }

    void renderTopCap(
            PoseStack poseStack,
            VertexConsumer vertices,
            int light,
            int overlay
    ) {
        topCap.render(poseStack, vertices, light, overlay);
    }

    void renderBottomCap(
            PoseStack poseStack,
            VertexConsumer vertices,
            int light,
            int overlay
    ) {
        bottomCap.render(poseStack, vertices, light, overlay);
    }

    void renderRune(
            int index,
            PoseStack poseStack,
            VertexConsumer vertices,
            int light,
            int overlay,
            float red,
            float green,
            float blue,
            float alpha,
            float halfSize
    ) {
        int frame = Math.floorMod(index, RUNE_COUNT);
        float minU = frame / (float) RUNE_COUNT;
        float maxU = (frame + 1) / (float) RUNE_COUNT;
        PoseStack.Pose pose = poseStack.last();
        runeVertex(
                vertices, pose,
                -halfSize, halfSize,
                maxU, 1.0F,
                red, green, blue, alpha,
                light, overlay
        );
        runeVertex(
                vertices, pose,
                halfSize, halfSize,
                maxU, 0.0F,
                red, green, blue, alpha,
                light, overlay
        );
        runeVertex(
                vertices, pose,
                halfSize, -halfSize,
                minU, 0.0F,
                red, green, blue, alpha,
                light, overlay
        );
        runeVertex(
                vertices, pose,
                -halfSize, -halfSize,
                minU, 1.0F,
                red, green, blue, alpha,
                light, overlay
        );
    }

    private static void runeVertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            float red,
            float green,
            float blue,
            float alpha,
            int light,
            int overlay
    ) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        vertices.vertex(position, x, y, 0.0F)
                .color(
                        colorChannel(red),
                        colorChannel(green),
                        colorChannel(blue),
                        colorChannel(alpha)
                )
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    private static int colorChannel(float value) {
        return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
    }
}
