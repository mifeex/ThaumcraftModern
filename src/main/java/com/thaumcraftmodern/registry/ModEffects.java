package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.effect.VisExhaustEffect;
import com.thaumcraftmodern.effect.ClassicWarpEffect;
import com.thaumcraftmodern.effect.FluxTaintEffect;
import com.thaumcraftmodern.effect.InfectiousVisExhaustEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(
                    ForgeRegistries.MOB_EFFECTS,
                    ThaumcraftModern.MOD_ID
            );

    public static final RegistryObject<MobEffect> VIS_EXHAUST =
            EFFECTS.register(
                    "vis_exhaust",
                    () -> new VisExhaustEffect(
                            0x8888FF,
                            "potion.visexhaust"
                    )
            );
    public static final RegistryObject<MobEffect> INFECTIOUS_VIS_EXHAUST =
            EFFECTS.register(
                    "infectious_vis_exhaust",
                    InfectiousVisExhaustEffect::new
            );
    public static final RegistryObject<MobEffect> FLUX_TAINT =
            EFFECTS.register("flux_taint", FluxTaintEffect::new);
    public static final RegistryObject<MobEffect> UNNATURAL_HUNGER =
            warp("unnatural_hunger", 0x9A6A35, "potion.unhunger",
                    ClassicWarpEffect.Behavior.UNNATURAL_HUNGER);
    public static final RegistryObject<MobEffect> WARP_WARD =
            EFFECTS.register("warp_ward", () -> new ClassicWarpEffect(
                    MobEffectCategory.BENEFICIAL, 0xFA7FEC, "potion.warpward",
                    ClassicWarpEffect.Behavior.WARP_WARD));
    public static final RegistryObject<MobEffect> DEATH_GAZE =
            warp("death_gaze", 0x5A104A, "potion.deathgaze",
                    ClassicWarpEffect.Behavior.DEATH_GAZE);
    public static final RegistryObject<MobEffect> BLURRED_VISION =
            warp("blurred_vision", 0x763B86, "potion.blurred",
                    ClassicWarpEffect.Behavior.BLURRED_VISION);
    public static final RegistryObject<MobEffect> SUN_SCORNED =
            warp("sun_scorned", 0xF5D85B, "potion.sunscorned",
                    ClassicWarpEffect.Behavior.SUN_SCORNED);
    public static final RegistryObject<MobEffect> THAUMARHIA =
            warp("thaumarhia", 0x725A85, "potion.thaumarhia",
                    ClassicWarpEffect.Behavior.THAUMARHIA);

    private ModEffects() {
    }

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }

    private static RegistryObject<MobEffect> warp(
            String id,
            int color,
            String descriptionId,
            ClassicWarpEffect.Behavior behavior
    ) {
        return EFFECTS.register(id, () -> new ClassicWarpEffect(
                MobEffectCategory.HARMFUL,
                color,
                descriptionId,
                behavior
        ));
    }
}
