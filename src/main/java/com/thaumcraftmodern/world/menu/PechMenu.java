package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.entity.PechBehavior;
import com.thaumcraftmodern.entity.PechTradeService;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PechMenu extends AbstractContainerMenu {
    public static final int TRADE_BUTTON = 0;
    private static final String DROP_TAG = "ThaumcraftPechTradeDrop";

    private final LegacyThaumcraftMob pech;
    private final SimpleContainer trade = new SimpleContainer(5);

    public PechMenu(
            int containerId,
            Inventory playerInventory,
            LegacyThaumcraftMob pech
    ) {
        super(ModMenus.PECH.get(), containerId);
        this.pech = pech;
        addSlot(new Slot(trade, 0, 36, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PechBehavior.value(stack) > 0;
            }
        });
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 2; column++) {
                addSlot(new Slot(
                        trade,
                        1 + column + row * 2,
                        106 + column * 18,
                        20 + row * 18
                ) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        84 + row * 18
                ));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    142
            ));
        }
        if (pech != null && !playerInventory.player.level().isClientSide) {
            pech.setPechTrading(true);
        }
    }

    public static PechMenu fromNetwork(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf buffer
    ) {
        Entity entity = inventory.player.level().getEntity(buffer.readVarInt());
        LegacyThaumcraftMob pech =
                entity instanceof LegacyThaumcraftMob mob
                        && mob.kind() == LegacyMobKind.PECH
                        ? mob
                        : null;
        return new PechMenu(containerId, inventory, pech);
    }

    public boolean canTrade() {
        if (PechBehavior.value(trade.getItem(0)) <= 0) {
            return false;
        }
        for (int slot = 1; slot < 5; slot++) {
            if (!trade.getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != TRADE_BUTTON
                || !(player instanceof ServerPlayer serverPlayer)
                || pech == null
                || !stillValid(player)
                || !canTrade()) {
            return false;
        }
        ItemStack payment = trade.getItem(0);
        PechTradeService.TradeResult result = PechTradeService.roll(
                pech.pechType(),
                payment,
                pech.pechPack(),
                pech.getRandom(),
                serverPlayer
        );
        payment.shrink(1);
        trade.setItem(0, payment);
        for (int index = 0; index < result.output().size() && index < 4;
             index++) {
            trade.setItem(index + 1, result.output().get(index));
        }
        if (result.losesTrust()) {
            pech.setPechTamed(false);
        }
        pech.level().playSound(
                null,
                pech,
                ModSounds.PECH_TRADE.get(),
                SoundSource.HOSTILE,
                0.4F,
                1.0F
        );
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return pech != null
                && pech.isAlive()
                && pech.isPechTamed()
                && player.distanceToSqr(pech) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 5) {
                if (!moveItemStackTo(stack, 5, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (pech != null && !player.level().isClientSide) {
            pech.setPechTrading(false);
        }
        if (!player.level().isClientSide) {
            for (int slot = 0; slot < trade.getContainerSize(); slot++) {
                ItemStack stack = trade.removeItemNoUpdate(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemEntity dropped = player.drop(stack, false);
                if (dropped != null) {
                    dropped.getPersistentData().putBoolean(DROP_TAG, true);
                }
            }
        }
    }

    public static boolean isTradeDrop(ItemEntity item) {
        return item.getPersistentData().getBoolean(DROP_TAG);
    }
}
