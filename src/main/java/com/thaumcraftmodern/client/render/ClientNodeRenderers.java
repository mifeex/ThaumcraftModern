package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.util.Objects;

/**
 * Isolated registration hook. The central client event class calls this after
 * the node block-entity types and revealing items are registered.
 */
public final class ClientNodeRenderers {
    private ClientNodeRenderers() {
    }

    public static void register(
            EntityRenderersEvent.RegisterRenderers event,
            BlockEntityType<AuraNodeBlockEntity> auraNodeType,
            BlockEntityType<JarredAuraNodeBlockEntity> jarredAuraNodeType,
            Item thaumometer,
            Item goggles
    ) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(auraNodeType, "auraNodeType");
        Objects.requireNonNull(jarredAuraNodeType, "jarredAuraNodeType");
        Objects.requireNonNull(thaumometer, "thaumometer");
        Objects.requireNonNull(goggles, "goggles");

        event.registerBlockEntityRenderer(
                auraNodeType,
                context -> new AuraNodeBlockEntityRenderer(
                        context,
                        thaumometer,
                        goggles
                )
        );
        event.registerBlockEntityRenderer(
                jarredAuraNodeType,
                JarredAuraNodeBlockEntityRenderer::new
        );
    }
}
