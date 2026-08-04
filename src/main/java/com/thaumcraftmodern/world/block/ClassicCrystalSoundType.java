package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

/** Exact modern equivalent of TC4 4.2.3.5's crystal SoundType. */
public final class ClassicCrystalSoundType extends SoundType {
    public static final ClassicCrystalSoundType INSTANCE =
            new ClassicCrystalSoundType();

    private ClassicCrystalSoundType() {
        super(
                1.0F,
                1.0F,
                SoundEvents.GLASS_BREAK,
                SoundEvents.STONE_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
        );
    }

    @Override
    public SoundEvent getBreakSound() {
        return crystal();
    }

    @Override
    public SoundEvent getStepSound() {
        return crystal();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return crystal();
    }

    @Override
    public SoundEvent getHitSound() {
        return crystal();
    }

    @Override
    public SoundEvent getFallSound() {
        return crystal();
    }

    private static SoundEvent crystal() {
        // Resolve lazily: blocks can be registered before sound events.
        return ModSounds.CRYSTAL.get();
    }
}
