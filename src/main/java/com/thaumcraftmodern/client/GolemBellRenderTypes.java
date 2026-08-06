package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

/** Fixed-function equivalents of TC4's additive bell marker and script passes. */
final class GolemBellRenderTypes extends RenderStateShard {
    private GolemBellRenderTypes() {
        super("thaumcraftmodern_golem_bell_render_types", () -> { }, () -> { });
    }

    static RenderType overlay(String name, ResourceLocation texture) {
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":golem_bell_" + name,
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                512,
                false,
                true,
                state(texture)
        );
    }

    static RenderType link(ResourceLocation texture) {
        return RenderType.create(
                ThaumcraftModern.MOD_ID + ":golem_bell_link",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.TRIANGLE_STRIP,
                4096,
                false,
                true,
                state(texture)
        );
    }

    private static RenderType.CompositeState state(ResourceLocation texture) {
        return RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                .setTextureState(new TextureStateShard(texture, true, false))
                .setTransparencyState(ADDITIVE_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
    }
}
