package com.thaumcraftmodern.api.wand;

import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.wand.WandCapDefinition;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import com.thaumcraftmodern.wand.WandForm;
import com.thaumcraftmodern.wand.WandRodDefinition;
import com.thaumcraftmodern.wand.WandState;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Stable entry point for creating and inspecting data-driven casting tools.
 *
 * <p>Rod and cap definitions are supplied by datapacks. The returned stacks
 * use the same server-authoritative vis state as built-in tools; callers do
 * not need to create another capability or NBT format.</p>
 */
public final class WandApi {
    private WandApi() {
    }

    public static List<WandRodDefinition> rods() {
        return WandComponentRegistry.catalog().rods();
    }

    public static List<WandCapDefinition> caps() {
        return WandComponentRegistry.catalog().caps();
    }

    public static ItemStack createWand(
            String rodId,
            String capId,
            boolean filled
    ) {
        return create(
                (WandItem) ModItems.CASTING_WAND.get(),
                rodId,
                capId,
                filled
        );
    }

    public static ItemStack createSceptre(
            String rodId,
            String capId,
            boolean filled
    ) {
        return create(
                (WandItem) ModItems.CRAFTING_SCEPTRE.get(),
                rodId,
                capId,
                filled
        );
    }

    public static ItemStack createStaff(
            String staffRodId,
            String capId,
            boolean filled
    ) {
        return create(
                (WandItem) ModItems.GREATWOOD_STAFF.get(),
                staffRodId,
                capId,
                filled
        );
    }

    public static ItemStack createCodexWand() {
        return ModItems.CODEX_WAND.get().getDefaultInstance();
    }

    public static Optional<WandState> state(ItemStack stack) {
        return WandVisService.state(stack);
    }

    public static int capacityVis(ItemStack stack) {
        return WandVisService.capacity(stack);
    }

    public static WandForm form(ItemStack stack) {
        if (!(stack.getItem() instanceof WandItem wand)) {
            throw new IllegalArgumentException("stack is not a casting tool");
        }
        return wand.form();
    }

    public static boolean isCraftingTool(ItemStack stack) {
        return WandVisService.isCraftingTool(stack);
    }

    public static boolean acceptsFocus(ItemStack stack) {
        return stack.getItem() instanceof WandItem wand
                && wand.form().acceptsFocus();
    }

    private static ItemStack create(
            WandItem wand,
            String rodId,
            String capId,
            boolean filled
    ) {
        validateForm(wand.form(), rodId);
        WandComponentRegistry.cap(capId).orElseThrow(() ->
                new IllegalArgumentException("unknown wand cap id: " + capId)
        );
        return filled
                ? wand.createFilled(rodId, capId)
                : wand.create(rodId, capId);
    }

    private static void validateForm(WandForm form, String rodId) {
        WandRodDefinition rod = WandComponentRegistry.rod(rodId).orElseThrow(
                () -> new IllegalArgumentException(
                        "unknown wand rod id: " + rodId
                )
        );
        if ((form == WandForm.STAFF) != rod.staff()) {
            throw new IllegalArgumentException(
                    "rod " + rodId + " is incompatible with " + form
            );
        }
    }
}
