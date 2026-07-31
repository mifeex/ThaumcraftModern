package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Vanilla spider geometry with TC4's ten-percent translucent body fade.
 */
public final class MindSpiderModel
        extends SpiderModel<LegacyThaumcraftMob> {
    private float renderAlpha = 0.1F;

    public MindSpiderModel(ModelPart root) {
        super(root);
    }

    public void setRenderAlpha(float alpha) {
        renderAlpha = alpha;
    }

    @Override
    public void renderToBuffer(
            PoseStack pose,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        super.renderToBuffer(
                pose,
                vertices,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha * renderAlpha
        );
    }

    public void renderEyes(
            PoseStack pose,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay
    ) {
        super.renderToBuffer(
                pose,
                vertices,
                packedLight,
                packedOverlay,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }
}
