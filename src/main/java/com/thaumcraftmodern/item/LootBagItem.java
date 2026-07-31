package com.thaumcraftmodern.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Server-authoritative modern equivalent of TC4's right-click loot bags.
 */
public final class LootBagItem extends Item {
    private final ResourceLocation lootTable;

    public LootBagItem(
            Properties properties,
            ResourceLocation lootTable,
            Rarity rarity
    ) {
        super(properties.stacksTo(16).rarity(rarity));
        this.lootTable = lootTable;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack bag = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            LootTable table = serverLevel.getServer()
                    .getLootData()
                    .getLootTable(lootTable);
            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .create(LootContextParamSets.CHEST);
            for (ItemStack reward : table.getRandomItems(params)) {
                if (!player.addItem(reward)) {
                    player.drop(reward, false);
                }
            }
            if (!player.getAbilities().instabuild) {
                bag.shrink(1);
            }
            serverLevel.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.ITEM_PICKUP,
                    SoundSource.PLAYERS,
                    0.75F,
                    1.0F
            );
        }
        return InteractionResultHolder.sidedSuccess(
                bag,
                level.isClientSide()
        );
    }
}
