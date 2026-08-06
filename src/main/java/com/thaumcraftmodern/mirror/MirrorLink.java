package com.thaumcraftmodern.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Persistent, cross-dimensional half of a TC4 mirror pair. */
public record MirrorLink(ResourceLocation dimension, BlockPos position) {
    private static final String DIMENSION = "LinkDimension";
    private static final String POSITION = "LinkPosition";

    public void save(CompoundTag tag) {
        tag.putString(DIMENSION, dimension.toString());
        tag.putLong(POSITION, position.asLong());
    }

    public static @Nullable MirrorLink load(CompoundTag tag) {
        ResourceLocation dimension = ResourceLocation.tryParse(
                tag.getString(DIMENSION));
        return dimension == null || !tag.contains(POSITION)
                ? null : new MirrorLink(dimension, BlockPos.of(tag.getLong(POSITION)));
    }

    public static MirrorLink of(ServerLevel level, BlockPos position) {
        return new MirrorLink(level.dimension().location(), position.immutable());
    }

    public @Nullable ServerLevel level(MinecraftServer server) {
        return server.getLevel(ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, dimension));
    }

    public String display() {
        return position.getX() + "," + position.getY() + ","
                + position.getZ() + " in " + dimension;
    }
}
