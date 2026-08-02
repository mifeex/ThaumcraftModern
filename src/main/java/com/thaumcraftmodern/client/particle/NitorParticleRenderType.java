package com.thaumcraftmodern.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

/** Full-bright additive pass used by the original TC4 {@code FXWisp}. */
final class NitorParticleRenderType implements ParticleRenderType {
    static final NitorParticleRenderType INSTANCE =
            new NitorParticleRenderType();

    private NitorParticleRenderType() {
    }

    @Override
    public void begin(BufferBuilder buffer, TextureManager textures) {
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE
        );
        buffer.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.PARTICLE
        );
    }

    @Override
    public void end(Tesselator tesselator) {
        tesselator.end();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    @Override
    public String toString() {
        return "THAUMCRAFTMODERN_NITOR_WISP";
    }
}
