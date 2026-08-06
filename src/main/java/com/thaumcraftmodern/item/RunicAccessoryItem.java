package com.thaumcraftmodern.item;

import com.thaumcraftmodern.api.runic.RunicArmor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** One of TC4's runic rings, amulets, or girdles. */
public final class RunicAccessoryItem extends CurioAccessoryItem implements RunicArmor {
    public enum Upgrade { NONE, CHARGED, HEALING, EMERGENCY, KINETIC }

    private final int charge;
    private final Upgrade upgrade;

    public RunicAccessoryItem(int charge, Upgrade upgrade, Properties properties) {
        super(properties);
        this.charge = charge;
        this.upgrade = upgrade;
    }

    @Override public int baseRunicCharge(ItemStack stack) { return charge; }
    public Upgrade upgrade() { return upgrade; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(RunicShieldService.chargeTooltip(stack));
    }
}
