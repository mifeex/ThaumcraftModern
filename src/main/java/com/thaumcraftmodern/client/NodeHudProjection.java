package com.thaumcraftmodern.client;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;

/**
 * Projects the point just above a node into scaled GUI coordinates. Keeping
 * this independent from the view/projection matrices also makes the overlay
 * stable across GUI-scale settings.
 */
final class NodeHudProjection {
    private static final double NODE_LABEL_HEIGHT = 1.22D;
    private static final int HUD_MARGIN_X = 24;
    private static final int HUD_MARGIN_TOP = 44;
    private static final int HUD_MARGIN_BOTTOM = 16;

    private NodeHudProjection() {
    }

    static Optional<ScreenPoint> aboveNode(
            Camera camera,
            BlockPos position,
            double anchorHeight,
            double verticalFovDegrees,
            int screenWidth,
            int screenHeight
    ) {
        Vec3 anchor = new Vec3(
                position.getX() + 0.5D,
                position.getY() + anchorHeight,
                position.getZ() + 0.5D
        );
        Vector3f look = camera.getLookVector();
        Vector3f up = camera.getUpVector();
        Vector3f left = camera.getLeftVector();
        Vec3 cameraPosition = camera.getPosition();
        return project(
                anchor.x - cameraPosition.x,
                anchor.y - cameraPosition.y,
                anchor.z - cameraPosition.z,
                look.x(),
                look.y(),
                look.z(),
                up.x(),
                up.y(),
                up.z(),
                left.x(),
                left.y(),
                left.z(),
                verticalFovDegrees,
                screenWidth,
                screenHeight
        );
    }

    static Optional<ScreenPoint> aboveNode(
            Camera camera,
            BlockPos position,
            double verticalFovDegrees,
            int screenWidth,
            int screenHeight
    ) {
        return aboveNode(camera, position, NODE_LABEL_HEIGHT,
                verticalFovDegrees, screenWidth, screenHeight);
    }

    static Optional<HudAnchor> aboveNodeAnchored(
            Camera camera,
            BlockPos position,
            double anchorHeight,
            double verticalFovDegrees,
            int screenWidth,
            int screenHeight
    ) {
        return worldAnchored(camera, new Vec3(
                position.getX() + 0.5D,
                position.getY() + anchorHeight,
                position.getZ() + 0.5D
        ), verticalFovDegrees, screenWidth, screenHeight);
    }

    static Optional<HudAnchor> worldAnchored(
            Camera camera,
            Vec3 anchor,
            double verticalFovDegrees,
            int screenWidth,
            int screenHeight
    ) {
        Vector3f look = camera.getLookVector();
        Vector3f up = camera.getUpVector();
        Vector3f left = camera.getLeftVector();
        Vec3 cameraPosition = camera.getPosition();
        return projectUnbounded(
                anchor.x - cameraPosition.x,
                anchor.y - cameraPosition.y,
                anchor.z - cameraPosition.z,
                look.x(), look.y(), look.z(),
                up.x(), up.y(), up.z(),
                left.x(), left.y(), left.z(),
                verticalFovDegrees, screenWidth, screenHeight
        ).map(point -> pinToViewport(point, screenWidth, screenHeight));
    }

    static Optional<ScreenPoint> project(
            double relativeX,
            double relativeY,
            double relativeZ,
            double lookX,
            double lookY,
            double lookZ,
            double upX,
            double upY,
            double upZ,
            double leftX,
            double leftY,
            double leftZ,
            double verticalFovDegrees,
            int screenWidth,
            int screenHeight
    ) {
        return projectUnbounded(relativeX, relativeY, relativeZ,
                lookX, lookY, lookZ, upX, upY, upZ,
                leftX, leftY, leftZ, verticalFovDegrees,
                screenWidth, screenHeight).filter(point ->
                point.x() >= -32
                        && point.x() <= screenWidth + 32
                        && point.y() >= -32
                        && point.y() <= screenHeight + 32);
    }

    private static Optional<ScreenPoint> projectUnbounded(
            double relativeX, double relativeY, double relativeZ,
            double lookX, double lookY, double lookZ,
            double upX, double upY, double upZ,
            double leftX, double leftY, double leftZ,
            double verticalFovDegrees, int screenWidth, int screenHeight) {
        if (screenWidth <= 0 || screenHeight <= 0) return Optional.empty();
        double depth = dot(
                relativeX,
                relativeY,
                relativeZ,
                lookX,
                lookY,
                lookZ
        );
        if (depth <= 0.05D) {
            return Optional.empty();
        }

        double halfVertical = Math.tan(
                Math.toRadians(verticalFovDegrees) * 0.5D
        ) * depth;
        if (halfVertical <= 0.0D) {
            return Optional.empty();
        }
        double halfHorizontal =
                halfVertical * screenWidth / (double) screenHeight;
        double leftOffset = dot(
                relativeX,
                relativeY,
                relativeZ,
                leftX,
                leftY,
                leftZ
        );
        double upOffset = dot(
                relativeX,
                relativeY,
                relativeZ,
                upX,
                upY,
                upZ
        );
        double x = screenWidth * 0.5D
                - leftOffset / halfHorizontal * screenWidth * 0.5D;
        double y = screenHeight * 0.5D
                - upOffset / halfVertical * screenHeight * 0.5D;
        return Optional.of(new ScreenPoint(
                (int) Math.round(x),
                (int) Math.round(y)
        ));
    }

    static HudAnchor pinToViewport(ScreenPoint projected,
            int screenWidth, int screenHeight) {
        int minX = Math.min(HUD_MARGIN_X, Math.max(0, screenWidth / 2));
        int maxX = Math.max(minX, screenWidth - minX);
        int minY = Math.min(HUD_MARGIN_TOP, Math.max(0, screenHeight / 2));
        int maxY = Math.max(minY, screenHeight - HUD_MARGIN_BOTTOM);
        boolean visible = projected.x() >= minX && projected.x() <= maxX
                && projected.y() >= minY && projected.y() <= maxY;
        if (visible) {
            return new HudAnchor(projected.x(), projected.y(), AnchorMode.WORLD);
        }
        return new HudAnchor(
                Math.max(minX, Math.min(maxX, projected.x())),
                Math.max(minY, Math.min(maxY, projected.y())),
                AnchorMode.CAMERA_PINNED);
    }

    private static double dot(
            double ax,
            double ay,
            double az,
            double bx,
            double by,
            double bz
    ) {
        return ax * bx + ay * by + az * bz;
    }

    record ScreenPoint(int x, int y) {
    }

    enum AnchorMode {
        WORLD,
        CAMERA_PINNED
    }

    record HudAnchor(int x, int y, AnchorMode mode) {
    }
}
