package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Bridges the first-person item and level render passes.
 *
 * <p>The legacy node renderer estimated the held wand tip from the player's
 * body rotation. That estimate cannot follow the separately rendered modern
 * first-person item, so the stream visibly detaches while the wand enters its
 * casting pose. Capturing the rendered cap in clip space lets the following
 * level frame reconstruct that exact point in the node renderer's local
 * coordinates.</p>
 */
final class FirstPersonWandTipTracker {
    private static final float MIN_W = 1.0E-6F;
    private static final long MAX_AGE_TICKS = 2L;

    private static ProjectedTip latest;

    private FirstPersonWandTipTracker() {
    }

    static void capture(
            ClientLevel level,
            PoseStack itemPose,
            ClassicWandRenderCalibration.Vector modelTip
    ) {
        Vector4f projected = projectToNdc(
                itemPose.last().pose(),
                RenderSystem.getProjectionMatrix(),
                new Vector4f(
                        modelTip.x(),
                        modelTip.y(),
                        modelTip.z(),
                        1.0F
                )
        );
        if (projected == null) {
            latest = null;
            return;
        }
        latest = new ProjectedTip(
                level,
                level.getGameTime(),
                projected.x,
                projected.y,
                projected.z
        );
    }

    static Vec3 resolveInNodeSpace(
            ClientLevel level,
            PoseStack nodePose
    ) {
        ProjectedTip tip = latest;
        if (tip == null || tip.level != level) {
            return null;
        }

        long age = level.getGameTime() - tip.gameTime;
        if (age < 0L || age > MAX_AGE_TICKS) {
            latest = null;
            return null;
        }

        Vector4f local = unprojectFromNdc(
                nodePose.last().pose(),
                RenderSystem.getProjectionMatrix(),
                new Vector4f(tip.x, tip.y, tip.z, 1.0F)
        );
        return local == null
                ? null
                : new Vec3(local.x, local.y, local.z);
    }

    static Vector4f projectToNdc(
            Matrix4f modelView,
            Matrix4f projection,
            Vector4f point
    ) {
        Vector4f clip = new Matrix4f(projection)
                .mul(modelView)
                .transform(new Vector4f(point));
        if (!Float.isFinite(clip.w) || Math.abs(clip.w) < MIN_W) {
            return null;
        }
        clip.div(clip.w);
        return finite(clip) ? clip : null;
    }

    static Vector4f unprojectFromNdc(
            Matrix4f modelView,
            Matrix4f projection,
            Vector4f point
    ) {
        Matrix4f inverse = new Matrix4f(projection).mul(modelView);
        float determinant = inverse.determinant();
        if (!Float.isFinite(determinant)
                || Math.abs(determinant) < MIN_W) {
            return null;
        }
        inverse.invert();

        Vector4f local = inverse.transform(new Vector4f(point));
        if (!Float.isFinite(local.w) || Math.abs(local.w) < MIN_W) {
            return null;
        }
        local.div(local.w);
        return finite(local) ? local : null;
    }

    private static boolean finite(Vector4f vector) {
        return Float.isFinite(vector.x)
                && Float.isFinite(vector.y)
                && Float.isFinite(vector.z)
                && Float.isFinite(vector.w);
    }

    private record ProjectedTip(
            ClientLevel level,
            long gameTime,
            float x,
            float y,
            float z
    ) {
    }
}
