package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.mirror.LinkedMirrorBlockEntity;
import com.thaumcraftmodern.mirror.MirrorLink;
import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Server-authoritative TC4 item mirror, including its delayed output queue. */
public final class MagicMirrorBlockEntity extends LinkedMirrorBlockEntity
        implements WorldlyContainer {
    private static final int[] SLOT = {0};
    private static final String OUTPUT_ORIGIN = "TcMirrorOutput";
    private final java.util.ArrayList<ItemStack> output = new java.util.ArrayList<>();
    private final Set<UUID> portableLinks = new HashSet<>();
    private int instability;
    private int ticks;

    public MagicMirrorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_MIRROR.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel,
            BlockPos pos, BlockState state, MagicMirrorBlockEntity mirror) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        mirror.linkTick();
        mirror.ticks++;
        int tickRate = mirror.instability / 50;
        if (tickRate == 0 || mirror.ticks % (tickRate * tickRate) == 0) {
            mirror.eject(level);
        }
        if (mirror.instability > 0 && mirror.ticks % 20 == 0) {
            mirror.instability--;
            mirror.sync();
        }
    }

    public boolean transport(ItemEntity entity) {
        if (!(level instanceof ServerLevel local) || entity.getItem().isEmpty()
                || fromThisOutput(entity) || !validReciprocalLink()) return false;
        MirrorLink destination = link();
        if (destination == null) return false;
        ServerLevel remoteLevel = destination.level(local.getServer());
        if (remoteLevel == null || !remoteLevel.hasChunkAt(destination.position())
                || !(remoteLevel.getBlockEntity(destination.position())
                instanceof MagicMirrorBlockEntity remote)) return false;
        remote.output.add(entity.getItem().copy());
        remote.sync();
        instability += entity.getItem().getCount();
        entity.discard();
        sync();
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
        return true;
    }

    /** Paired block mirrors and bound hand mirrors both use the open surface. */
    public boolean visuallyOpen() {
        return linked() || !portableLinks.isEmpty();
    }

    public void addPortableLink(UUID id) {
        if (portableLinks.add(id)) sync();
    }

    public void removePortableLink(UUID id) {
        if (portableLinks.remove(id)) sync();
    }

    private boolean fromThisOutput(ItemEntity entity) {
        CompoundTag data = entity.getPersistentData().getCompound(OUTPUT_ORIGIN);
        return level != null && data.getString("Dimension")
                .equals(level.dimension().location().toString())
                && data.getLong("Position") == worldPosition.asLong();
    }

    private void eject(ServerLevel level) {
        if (output.isEmpty() || ticks <= 20) return;
        int index = level.random.nextInt(output.size());
        ItemStack queued = output.get(index);
        if (queued.isEmpty()) {
            output.remove(index);
            return;
        }
        ItemStack one = queued.copy();
        one.setCount(1);
        spawnItem(level, one);
        queued.shrink(1);
        if (queued.isEmpty()) output.remove(index);
        instability++;
        sync();
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
    }

    public void spawnItem(ServerLevel level, ItemStack stack) {
        Direction facing = getBlockState().getValue(
                com.thaumcraftmodern.world.block.MagicMirrorBlock.FACING);
        ItemEntity entity = new ItemEntity(level,
                worldPosition.getX() + 0.5D - facing.getStepX() * 0.3D,
                worldPosition.getY() + 0.5D - facing.getStepY() * 0.3D,
                worldPosition.getZ() + 0.5D - facing.getStepZ() * 0.3D,
                stack);
        entity.setDeltaMovement(facing.getStepX() * 0.15D,
                facing.getStepY() * 0.15D, facing.getStepZ() * 0.15D);
        entity.setPickUpDelay(20);
        CompoundTag origin = new CompoundTag();
        origin.putString("Dimension", level.dimension().location().toString());
        origin.putLong("Position", worldPosition.asLong());
        entity.getPersistentData().put(OUTPUT_ORIGIN, origin);
        level.addFreshEntity(entity);
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return output.isEmpty(); }
    @Override public ItemStack getItem(int slot) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != 0 || stack.isEmpty() || !(level instanceof ServerLevel local)) return;
        MirrorLink destination = link();
        ServerLevel remoteLevel = destination == null ? null
                : destination.level(local.getServer());
        BlockEntity remote = remoteLevel == null || !remoteLevel.hasChunkAt(
                destination.position()) ? null
                : remoteLevel.getBlockEntity(destination.position());
        if (remote instanceof MagicMirrorBlockEntity mirror
                && validReciprocalLink()) {
            mirror.output.add(stack.copy());
            mirror.sync();
            instability += stack.getCount();
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
            sync();
        } else {
            spawnItem(local, stack.copy());
        }
    }

    @Override public boolean stillValid(Player player) { return false; }
    @Override public void clearContent() { output.clear(); sync(); }
    @Override public int[] getSlotsForFace(Direction side) { return SLOT; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack,
            Direction side) { return slot == 0 && validReciprocalLink(); }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack,
            Direction side) { return false; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Instability", instability);
        ListTag items = new ListTag();
        for (ItemStack stack : output) {
            if (!stack.isEmpty()) items.add(stack.save(new CompoundTag()));
        }
        tag.put("Items", items);
        ListTag handMirrors = new ListTag();
        for (UUID id : portableLinks) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", id);
            handMirrors.add(entry);
        }
        tag.put("PortableLinks", handMirrors);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        instability = Math.max(0, tag.getInt("Instability"));
        output.clear();
        ListTag items = tag.getList("Items", CompoundTag.TAG_COMPOUND);
        for (int index = 0; index < items.size(); index++) {
            ItemStack stack = ItemStack.of(items.getCompound(index));
            if (!stack.isEmpty()) output.add(stack);
        }
        portableLinks.clear();
        ListTag handMirrors = tag.getList("PortableLinks", CompoundTag.TAG_COMPOUND);
        for (int index = 0; index < handMirrors.size(); index++) {
            CompoundTag entry = handMirrors.getCompound(index);
            if (entry.hasUUID("Id")) portableLinks.add(entry.getUUID("Id"));
        }
    }
}
