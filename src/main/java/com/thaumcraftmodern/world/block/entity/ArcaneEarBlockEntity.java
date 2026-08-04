package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.arcaneear.ArcaneEarNoteEvents;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.ArcaneEarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Exact gameplay state carried by TC4 TileSensor. */
public final class ArcaneEarBlockEntity extends BlockEntity {
    public static final int SIGNAL_TICKS = 10;
    public static final int NOTE_COUNT = 25;

    private byte note;
    private byte tone;
    private int redstoneSignal;

    public ArcaneEarBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ARCANE_EAR.get(), position, state);
    }

    public static void serverTick(
            Level rawLevel,
            BlockPos position,
            BlockState state,
            ArcaneEarBlockEntity ear
    ) {
        if (!(rawLevel instanceof ServerLevel level)
                || ear.redstoneSignal <= 0) {
            return;
        }
        ear.redstoneSignal--;
        if (ear.redstoneSignal == 0) {
            ear.setPowered(level, state, false);
        }
    }

    public int note() {
        return Byte.toUnsignedInt(note);
    }

    public int tone() {
        return Byte.toUnsignedInt(tone);
    }

    public boolean powered() {
        return redstoneSignal > 0;
    }

    public boolean matches(int expectedTone, int expectedNote) {
        return tone() == expectedTone && note() == expectedNote;
    }

    public void updateTone() {
        if (level == null) {
            return;
        }
        int updated = ArcaneEarNoteEvents.classicTone(
                level.getBlockState(worldPosition.below()).instrument()
        );
        if (tone() != updated) {
            tone = (byte) updated;
            setChanged();
        }
    }

    public void changePitchAndPlay() {
        note = (byte) ((note() + 1) % NOTE_COUNT);
        setChanged();
        playConfiguredNote(true);
    }

    public void hearNote() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        playConfiguredNote(false);
        redstoneSignal = SIGNAL_TICKS;
        setChanged();
        setPowered(serverLevel, getBlockState(), true);
    }

    private void playConfiguredNote(boolean sound) {
        if (!(level instanceof ServerLevel serverLevel)
                || !level.getBlockState(worldPosition.above()).isAir()) {
            return;
        }
        if (sound) {
            serverLevel.playSound(
                    null,
                    worldPosition,
                    ArcaneEarNoteEvents.instrument(tone()).getSoundEvent().value(),
                    SoundSource.BLOCKS,
                    3.0F,
                    (float) Math.pow(2.0D, (note() - 12) / 12.0D)
            );
        }
        serverLevel.sendParticles(
                ParticleTypes.NOTE,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.2D,
                worldPosition.getZ() + 0.5D,
                0,
                note() / 24.0D,
                0.0D,
                0.0D,
                1.0D
        );
    }

    private void setPowered(
            ServerLevel level,
            BlockState currentState,
            boolean powered
    ) {
        if (currentState.hasProperty(ArcaneEarBlock.POWERED)
                && currentState.getValue(ArcaneEarBlock.POWERED) != powered) {
            level.setBlock(
                    worldPosition,
                    currentState.setValue(ArcaneEarBlock.POWERED, powered),
                    Block.UPDATE_ALL
            );
        }
        level.updateNeighborsAt(worldPosition, currentState.getBlock());
        level.updateNeighborsAt(worldPosition.below(), currentState.getBlock());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        ArcaneEarNoteEvents.register(this);
        if (level instanceof ServerLevel serverLevel) {
            if (getBlockState().hasProperty(ArcaneEarBlock.POWERED)
                    && getBlockState().getValue(ArcaneEarBlock.POWERED)) {
                setPowered(serverLevel, getBlockState(), false);
            }
            updateTone();
        }
    }

    @Override
    public void setRemoved() {
        ArcaneEarNoteEvents.unregister(this);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte("note", note);
        tag.putByte("tone", tone);
        // TC4 intentionally did not persist the short redstone pulse.
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        note = (byte) Math.max(0, Math.min(24, tag.getByte("note")));
        tone = (byte) Math.max(0, Math.min(4, tag.getByte("tone")));
        redstoneSignal = 0;
    }
}
