package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Direct modern-buffer port of TC4's {@code obelisk_cap.obj}. Vertex, UV,
 * normal and triangle order are intentionally kept identical to the source
 * model.
 */
final class LegacyEldritchCapModel {
    /*
     * The source OBJ stores the closed cap shell first and the small central
     * "Tip" prism last. The shell already has its own complete end face.
     */
    private static final int CAP_TRIANGLE_COUNT = 20;
    private static final float[][] VERTICES = {
            {-0.5000F, -0.5000F, 0.0000F},
            {-0.5000F, 0.5000F, 0.0000F},
            {0.5000F, 0.5000F, 0.0000F},
            {0.5000F, -0.5000F, 0.0000F},
            {-0.2500F, -0.2500F, 0.8000F},
            {0.2500F, -0.2500F, 0.8000F},
            {0.2500F, 0.2500F, 0.8000F},
            {-0.2500F, 0.2500F, 0.8000F},
            {0.0000F, -0.3750F, 0.4000F},
            {0.3750F, 0.0000F, 0.4000F},
            {0.0000F, 0.3750F, 0.4000F},
            {-0.3750F, 0.0000F, 0.4000F},
            {-0.1250F, -0.1250F, 0.8000F},
            {-0.1250F, 0.1250F, 0.8000F},
            {0.1250F, 0.1250F, 0.8000F},
            {0.1250F, -0.1250F, 0.8000F},
            {-0.1250F, -0.1250F, 1.0000F},
            {0.1250F, -0.1250F, 1.0000F},
            {0.1250F, 0.1250F, 1.0000F},
            {-0.1250F, 0.1250F, 1.0000F}
    };
    private static final float[][] UVS = {
            {0.0000F, 0.5000F}, {0.0000F, 0.0000F},
            {0.5000F, 0.0000F}, {0.5000F, 0.5000F},
            {0.0000F, 0.7500F}, {0.2500F, 0.7500F},
            {0.2500F, 1.0000F}, {0.0000F, 1.0000F},
            {1.0000F, 0.5000F}, {0.7500F, 0.7500F},
            {1.0000F, 1.0000F}, {0.5000F, 1.0000F},
            {0.8438F, 0.0313F}, {0.8438F, 0.2188F},
            {0.6563F, 0.2188F}, {0.6563F, 0.0313F},
            {0.8438F, 0.3438F}, {0.6563F, 0.3438F}
    };
    private static final float[][] NORMALS = {
            {0.0000F, 0.0000F, -1.0000F},
            {0.0000F, 0.0000F, 1.0000F},
            {0.0000F, -0.9545F, 0.2983F},
            {0.9545F, 0.0000F, 0.2983F},
            {0.0000F, 0.9545F, 0.2983F},
            {-0.9545F, 0.0000F, 0.2983F},
            {0.0000F, 0.0000F, -1.0000F},
            {0.0000F, 0.0000F, 1.0000F},
            {0.0000F, -1.0000F, 0.0000F},
            {1.0000F, 0.0000F, 0.0000F},
            {0.0000F, 1.0000F, 0.0000F},
            {-1.0000F, 0.0000F, 0.0000F}
    };
    private static final int[][] TRIANGLES = {
            {1,1,1, 2,2,1, 3,3,1}, {3,3,1, 4,4,1, 1,1,1},
            {5,5,2, 6,6,2, 7,7,2}, {7,7,2, 8,8,2, 5,5,2},
            {1,4,3, 4,9,3, 9,10,3}, {4,9,3, 6,11,3, 9,10,3},
            {6,11,3, 5,12,3, 9,10,3}, {5,12,3, 1,4,3, 9,10,3},
            {4,4,4, 3,9,4, 10,10,4}, {3,9,4, 7,11,4, 10,10,4},
            {7,11,4, 6,12,4, 10,10,4}, {6,12,4, 4,4,4, 10,10,4},
            {3,4,5, 2,9,5, 11,10,5}, {2,9,5, 8,11,5, 11,10,5},
            {8,11,5, 7,12,5, 11,10,5}, {7,12,5, 3,4,5, 11,10,5},
            {2,4,6, 1,9,6, 12,10,6}, {1,9,6, 5,11,6, 12,10,6},
            {5,11,6, 8,12,6, 12,10,6}, {8,12,6, 2,4,6, 12,10,6},
            {13,13,7, 14,14,7, 15,15,7}, {15,15,7, 16,16,7, 13,13,7},
            {17,16,8, 18,13,8, 19,14,8}, {19,14,8, 20,15,8, 17,16,8},
            {13,15,9, 16,14,9, 18,17,9}, {18,17,9, 17,18,9, 13,15,9},
            {16,15,10, 15,14,10, 19,17,10}, {19,17,10, 18,18,10, 16,15,10},
            {15,15,11, 14,14,11, 20,17,11}, {20,17,11, 19,18,11, 15,15,11},
            {14,15,12, 13,14,12, 17,17,12}, {17,17,12, 20,18,12, 14,15,12}
    };

    void render(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight
    ) {
        renderRange(
                poseStack,
                vertices,
                packedLight,
                TRIANGLES.length
        );
    }

    /**
     * Renders the already-closed Cap group without the separate dark Tip
     * prism. Used by the obelisk pair so its upper and lower ends do not
     * expose the near-black tip rectangles at close range.
     */
    void renderClosedShell(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight
    ) {
        renderRange(
                poseStack,
                vertices,
                packedLight,
                CAP_TRIANGLE_COUNT
        );
    }

    private static void renderRange(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight,
            int triangleCount
    ) {
        PoseStack.Pose pose = poseStack.last();
        for (int index = 0; index < triangleCount; index++) {
            int[] triangle = TRIANGLES[index];
            vertex(pose, vertices, triangle, 0, packedLight);
            vertex(pose, vertices, triangle, 3, packedLight);
            vertex(pose, vertices, triangle, 6, packedLight);
        }
    }

    private static void vertex(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int[] triangle,
            int offset,
            int packedLight
    ) {
        float[] position = VERTICES[triangle[offset] - 1];
        float[] uv = UVS[triangle[offset + 1] - 1];
        float[] normal = NORMALS[triangle[offset + 2] - 1];
        Matrix4f matrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        consumer.vertex(matrix, position[0], position[1], position[2])
                .color(255, 255, 255, 255)
                .uv(uv[0], 1.0F - uv[1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalMatrix, normal[0], normal[1], normal[2])
                .endVertex();
    }
}
