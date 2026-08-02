package com.thaumcraftmodern.item;

import com.thaumcraftmodern.essentia.EssentiaTransport;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** In-world transport diagnostic, including the improved buffer controller. */
public final class EssentiaResonatorItem extends Item {
    public EssentiaResonatorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockEntity entity = context.getLevel().getBlockEntity(
                context.getClickedPos());
        if (entity instanceof AdvancedEssentiaBufferBlockEntity buffer) {
            if (!context.getLevel().isClientSide) {
                String aspect = buffer.returnedContents().keySet().stream()
                        .findFirst().orElseGet(() -> buffer.supplyContents()
                                .keySet().stream().findFirst().orElse(""));
                player.displayClientMessage(Component.translatable(
                        "message.thaumcraftmodern.resonator.advanced",
                        Component.translatable(
                                "state.thaumcraftmodern.advanced_buffer."
                                        + buffer.flowState().name().toLowerCase()),
                        buffer.totalAmount(),
                        aspect.isBlank()
                                ? Component.translatable("tc.resonator3")
                                : Component.translatable("tc.aspect." + aspect),
                        Component.translatable(buffer.diagnosticReasonKey())),
                        false);
            }
            return InteractionResult.sidedSuccess(
                    context.getLevel().isClientSide);
        }
        if (entity instanceof EssentiaTransport transport) {
            Direction side = context.getClickedFace();
            if (!context.getLevel().isClientSide) {
                String aspect = transport.essentiaType(side);
                String suction = transport.suctionType(side);
                player.displayClientMessage(Component.translatable(
                        "message.thaumcraftmodern.resonator.transport",
                        transport.essentiaAmount(side),
                        aspect == null
                                ? Component.translatable("tc.resonator3")
                                : Component.translatable("tc.aspect." + aspect),
                        transport.suctionAmount(side),
                        suction == null
                                ? Component.translatable("tc.resonator3")
                                : Component.translatable("tc.aspect." + suction),
                        transport.suctionFlowMode(side).name()), false);
            }
            return InteractionResult.sidedSuccess(
                    context.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }
}
