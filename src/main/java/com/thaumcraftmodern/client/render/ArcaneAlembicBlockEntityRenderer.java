package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Draws the original TC4 paper label and aspect icon over the alembic panel. */
public final class ArcaneAlembicBlockEntityRenderer
        implements BlockEntityRenderer<ArcaneAlembicBlockEntity> {
    private static final ResourceLocation LABEL = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/label.png");
    private static final float CENTER_Y = 0.468F;
    private static final float PANEL_OFFSET = 0.409F;

    public ArcaneAlembicBlockEntityRenderer(
            BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ArcaneAlembicBlockEntity alembic, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers,
            int packedLight, int packedOverlay) {
        String aspectId = alembic.filterAspect();
        if (aspectId == null) return;
        AspectDefinition aspect = AspectRegistryRuntime.find(aspectId).orElse(null);
        if (aspect == null) return;
        ResourceLocation icon = ResourceLocation.tryParse(aspect.icon());
        if (icon == null) return;

        BlockState state = alembic.getBlockState();
        Direction facing = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        draw(facing, 0.135F, PANEL_OFFSET, LABEL, poseStack, buffers,
                packedLight, packedOverlay);
        draw(facing, 0.060F, PANEL_OFFSET + 0.002F, icon, poseStack, buffers,
                packedLight, packedOverlay);
    }

    private static void draw(Direction facing, float halfSize, float offset,
            ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        float cx = 0.5F + facing.getStepX() * offset;
        float cz = 0.5F + facing.getStepZ() * offset;
        float minY = CENTER_Y - halfSize;
        float maxY = CENTER_Y + halfSize;
        float rx = -facing.getStepZ() * halfSize;
        float rz = facing.getStepX() * halfSize;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        vertex(out, matrix, normal, cx - rx, minY, cz - rz, 0, 1,
                facing, packedLight, packedOverlay);
        vertex(out, matrix, normal, cx + rx, minY, cz + rz, 1, 1,
                facing, packedLight, packedOverlay);
        vertex(out, matrix, normal, cx + rx, maxY, cz + rz, 1, 0,
                facing, packedLight, packedOverlay);
        vertex(out, matrix, normal, cx - rx, maxY, cz - rz, 0, 0,
                facing, packedLight, packedOverlay);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix,
            Matrix3f normal, float x, float y, float z, float u, float v,
            Direction facing, int packedLight, int packedOverlay) {
        out.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(packedOverlay).uv2(packedLight)
                .normal(normal, facing.getStepX(), 0, facing.getStepZ())
                .endVertex();
    }
}
