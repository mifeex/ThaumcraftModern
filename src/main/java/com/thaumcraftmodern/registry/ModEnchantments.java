package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.enchantment.HasteEnchantment;
import com.thaumcraftmodern.enchantment.RepairEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(
                    ForgeRegistries.ENCHANTMENTS,
                    ThaumcraftModern.MOD_ID
            );

    public static final RegistryObject<Enchantment> HASTE =
            ENCHANTMENTS.register("haste", HasteEnchantment::new);
    public static final RegistryObject<Enchantment> REPAIR =
            ENCHANTMENTS.register("repair", RepairEnchantment::new);

    private ModEnchantments() {
    }

    public static void register(IEventBus bus) {
        ENCHANTMENTS.register(bus);
    }
}
