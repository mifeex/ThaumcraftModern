package com.thaumcraftmodern.registry;

import com.mojang.serialization.Codec;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.loot.ElementalPickaxeLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    ThaumcraftModern.MOD_ID);
    public static final RegistryObject<Codec<ElementalPickaxeLootModifier>> ELEMENTAL_PICKAXE =
            SERIALIZERS.register("elemental_pickaxe", () -> ElementalPickaxeLootModifier.CODEC);

    private ModLootModifiers() {}
    public static void register(IEventBus bus) { SERIALIZERS.register(bus); }
}
