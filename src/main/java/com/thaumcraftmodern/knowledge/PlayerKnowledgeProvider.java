package com.thaumcraftmodern.knowledge;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerKnowledgeProvider implements ICapabilitySerializable<CompoundTag> {
    private final PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
    private LazyOptional<PlayerThaumKnowledge> optional = createOptional();

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability != KnowledgeCapabilities.PLAYER) {
            return LazyOptional.empty();
        }
        if (!optional.isPresent()) {
            optional = createOptional();
        }
        return optional.cast();
    }

    @Override
    public CompoundTag serializeNBT() {
        return knowledge.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        try {
            knowledge.deserializeNBT(tag);
        } catch (RuntimeException exception) {
            ThaumcraftModern.LOGGER.error(
                    "Could not load player thaumaturgy knowledge; version={}, keys={}. "
                            + "Keeping the safe default state.",
                    tag.getInt("version"),
                    tag.getAllKeys(),
                    exception
            );
        }
    }

    public void invalidate() {
        optional.invalidate();
    }

    private LazyOptional<PlayerThaumKnowledge> createOptional() {
        return LazyOptional.of(() -> knowledge);
    }
}
