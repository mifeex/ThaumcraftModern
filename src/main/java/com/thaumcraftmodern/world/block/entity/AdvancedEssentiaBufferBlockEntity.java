package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.essentia.AdvancedBufferFlowController;
import com.thaumcraftmodern.essentia.AdvancedBufferSideRole;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaFlowMode;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumcraftmodern.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Four-point, side-programmable buffer and local flow controller. It only
 * inspects its six direct neighbours; ordinary tube suction carries remote
 * demand and return intent to those neighbours.
 */
public final class AdvancedEssentiaBufferBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final int CAPACITY_PER_ASPECT = 4;
    /**
     * Must be strictly stronger than an ordinary jar. Essentia transport only
     * moves when local suction is greater than source suction; the previous
     * 32 == 32 tie made the configured input visibly connect but never pull.
     */
    public static final int INPUT_SUCTION = EssentiaJarBlockEntity.SUCTION + 1;
    public static final int RETURN_SUCTION = 64;
    public static final int MAIN_OUTPUT_DECISION_TICKS = 40;

    private final EssentiaStore supply = new EssentiaStore();
    private final EssentiaStore returned = new EssentiaStore();
    private final AdvancedBufferSideRole[] roles = {
            AdvancedBufferSideRole.INPUT,
            AdvancedBufferSideRole.BLOCKED,
            AdvancedBufferSideRole.MAIN_OUTPUT,
            AdvancedBufferSideRole.RESERVE_OUTPUT,
            AdvancedBufferSideRole.BLOCKED,
            AdvancedBufferSideRole.BLOCKED
    };
    private AdvancedBufferFlowController.Snapshot controller =
            AdvancedBufferFlowController.Snapshot.idle();
    private String blockedReasonKey =
            "diagnostic.thaumcraftmodern.advanced_buffer.ok";
    private int tickCount;
    private int mainOutputMissTicks;

    public AdvancedEssentiaBufferBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_ESSENTIA_BUFFER.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel,
            BlockPos pos, BlockState state,
            AdvancedEssentiaBufferBlockEntity buffer) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        buffer.tickCount++;

        boolean mainPathReversed = buffer.mainPathReversed(level);
        if (mainPathReversed) {
            buffer.queueSupplyForReserve();
        } else {
            buffer.restoreReserveQueue();
        }
        boolean activeConsumer = buffer.activeConsumer(level);
        boolean reserveAccepts = buffer.reserveAccepts(level);
        AdvancedBufferFlowController.Signals signals =
                new AdvancedBufferFlowController.Signals(
                        activeConsumer,
                        mainPathReversed,
                        !buffer.returned.isEmpty(),
                        reserveAccepts
                );
        AdvancedBufferFlowController.Snapshot previous = buffer.controller;
        buffer.controller = AdvancedBufferFlowController.advance(
                previous, signals, cooldownTicks(pos));
        if (!buffer.controller.equals(previous)) {
            if (buffer.controller.state()
                    == AdvancedBufferFlowController.State.BLOCKED) {
                boolean reserveBlocked = !buffer.returned.isEmpty();
                buffer.blockedReasonKey = reserveBlocked
                        ? buffer.sideFor(AdvancedBufferSideRole.RESERVE_OUTPUT) == null
                                ? "diagnostic.thaumcraftmodern.advanced_buffer.no_reserve"
                                : "diagnostic.thaumcraftmodern.advanced_buffer.reserve_full"
                        : "diagnostic.thaumcraftmodern.advanced_buffer.ok";
            }
            EssentiaSync.changed(buffer);
        }

        if (buffer.tickCount % 5 != 0) return;
        buffer.fillFromInputs(level);
        if (mainPathReversed) buffer.queueSupplyForReserve();
        switch (buffer.controller.state()) {
            case RESERVE -> buffer.sendToReserve(level);
            default -> { }
        }
    }

    public static int cooldownTicks(BlockPos pos) {
        return 20 + Math.floorMod(Long.hashCode(pos.asLong() ^ 0x5F3759DFL), 21);
    }

    private boolean activeConsumer(ServerLevel level) {
        Direction main = sideFor(AdvancedBufferSideRole.MAIN_OUTPUT);
        if (main == null || supply.isEmpty()) return false;
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, main).orElse(null);
        if (remote == null || !remote.canInputFrom(main.getOpposite())) return false;
        Direction remoteSide = main.getOpposite();
        if (remote.suctionFlowMode(remoteSide) == EssentiaFlowMode.RETURN
                || remote.suctionAmount(remoteSide) <= minimumSuction()) {
            return false;
        }
        String wanted = remote.suctionType(remoteSide);
        return wanted == null ? !supply.isEmpty() : supply.amount(wanted) > 0;
    }

    private void queueSupplyForReserve() {
        if (supply.isEmpty()) return;
        for (Map.Entry<String, Integer> entry
                : new ArrayList<>(supply.view().entrySet())) {
            int amount = entry.getValue();
            if (amount <= 0 || !supply.remove(entry.getKey(), amount)) continue;
            returned.add(entry.getKey(), amount);
        }
        EssentiaSync.changed(this);
    }

    private void restoreReserveQueue() {
        if (returned.isEmpty()) return;
        for (Map.Entry<String, Integer> entry
                : new ArrayList<>(returned.view().entrySet())) {
            int amount = entry.getValue();
            if (amount <= 0 || !returned.remove(entry.getKey(), amount)) continue;
            supply.add(entry.getKey(), amount);
        }
        EssentiaSync.changed(this);
    }

    private void fillFromInputs(ServerLevel level) {
        for (Direction side : Direction.values()) {
            if (role(side) != AdvancedBufferSideRole.INPUT) continue;
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, side).orElse(null);
            if (remote == null || !remote.canOutputTo(side.getOpposite())) continue;
            Direction remoteSide = side.getOpposite();
            String aspect = remote.essentiaType(remoteSide);
            if (aspect == null) aspect = remote.essentiaType(null);
            if (!hasRoom(aspect)
                    || suctionAmount(side) <= remote.suctionAmount(remoteSide)
                    || suctionAmount(side) < remote.minimumSuction()) continue;
            int taken = remote.takeEssentia(aspect, 1, remoteSide);
            if (taken > 0) {
                supply.add(aspect, 1);
                EssentiaSync.changed(this);
                return;
            }
        }
    }

    private void sendToReserve(ServerLevel level) {
        Direction reserve = sideFor(AdvancedBufferSideRole.RESERVE_OUTPUT);
        String aspect = requestedAspect(returned, reserve);
        if (reserve == null || aspect == null) return;
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, reserve).orElse(null);
        Direction remoteSide = reserve.getOpposite();
        if (remote == null || !remote.canInputFrom(remoteSide)
                || !acceptsDemand(remote, remoteSide, aspect)) return;
        int accepted = remote.addEssentia(aspect, 1, remoteSide);
        if (accepted > 0 && returned.remove(aspect, 1)) {
            EssentiaSync.changed(this);
        }
    }

    private boolean reserveAccepts(ServerLevel level) {
        Direction reserve = sideFor(AdvancedBufferSideRole.RESERVE_OUTPUT);
        String aspect = requestedAspect(returned, reserve);
        if (reserve == null || aspect == null) return returned.isEmpty();
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, reserve).orElse(null);
        Direction remoteSide = reserve.getOpposite();
        if (remote == null || !remote.canInputFrom(remoteSide)) return false;
        return acceptsDemand(remote, remoteSide, aspect);
    }

    private boolean acceptsDemand(EssentiaTransport remote,
            Direction remoteSide, String aspect) {
        if (remote.suctionFlowMode(remoteSide) == EssentiaFlowMode.RETURN
                || remote.suctionAmount(remoteSide) <= minimumSuction()) {
            return false;
        }
        String wanted = remote.suctionType(remoteSide);
        return wanted == null || Objects.equals(wanted, aspect);
    }

    private boolean mainPathReversed(ServerLevel level) {
        Direction main = sideFor(AdvancedBufferSideRole.MAIN_OUTPUT);
        if (main == null) return false;
        EssentiaTransport remote = EssentiaConnections.neighbour(
                level, worldPosition, main).orElse(null);
        if (remote == null) return false;
        Direction remoteSide = main.getOpposite();
        return remote.suctionFlowMode(remoteSide) == EssentiaFlowMode.RETURN
                || remote instanceof EssentiaTubeBlockEntity tube
                        && tube.policy().reversibleController()
                        && tube.returnEnabled();
    }

    private boolean hasRoom(@Nullable String aspect) {
        return aspect != null && !aspect.isBlank()
                && supply.amount(aspect) + returned.amount(aspect)
                        < CAPACITY_PER_ASPECT;
    }

    private static @Nullable String first(EssentiaStore store) {
        return store.view().keySet().stream().findFirst().orElse(null);
    }

    public AdvancedBufferSideRole role(Direction side) {
        return roles[side.ordinal()];
    }

    public void cycleRole(Direction side) {
        AdvancedBufferSideRole next = role(side).next();
        if (next != AdvancedBufferSideRole.BLOCKED) {
            for (Direction other : Direction.values()) {
                if (other != side && role(other) == next) {
                    roles[other.ordinal()] = AdvancedBufferSideRole.BLOCKED;
                }
            }
        }
        roles[side.ordinal()] = next;
        EssentiaSync.changed(this);
        if (level != null && !level.isClientSide) {
            for (Direction direction : Direction.values()) {
                EssentiaTubeBlock.refreshConnections(
                        level, worldPosition.relative(direction));
            }
        }
    }

    public @Nullable Direction sideFor(AdvancedBufferSideRole role) {
        for (Direction side : Direction.values()) {
            if (role(side) == role) return side;
        }
        return null;
    }

    public AdvancedBufferFlowController.State flowState() {
        return controller.state();
    }

    public int stateTimer() {
        return controller.timer();
    }

    public int totalAmount() {
        return supply.total() + returned.total();
    }

    public Map<String, Integer> supplyContents() {
        return supply.view();
    }

    public Map<String, Integer> returnedContents() {
        return returned.view();
    }

    public Map<String, Integer> contents() {
        Map<String, Integer> combined = new LinkedHashMap<>(supply.view());
        returned.view().forEach((aspect, amount) ->
                combined.merge(aspect, amount, Integer::sum));
        return Map.copyOf(combined);
    }

    public String diagnosticReasonKey() {
        if (controller.state() == AdvancedBufferFlowController.State.BLOCKED) {
            if (sideFor(AdvancedBufferSideRole.MAIN_OUTPUT) == null) {
                return "diagnostic.thaumcraftmodern.advanced_buffer.no_main";
            }
            if (sideFor(AdvancedBufferSideRole.RESERVE_OUTPUT) == null) {
                return "diagnostic.thaumcraftmodern.advanced_buffer.no_reserve";
            }
            return blockedReasonKey;
        }
        return "diagnostic.thaumcraftmodern.advanced_buffer.ok";
    }

    @Override
    public boolean isConnectable(Direction side) {
        return side != null && role(side) != AdvancedBufferSideRole.BLOCKED;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        if (side == null) return false;
        return role(side) == AdvancedBufferSideRole.INPUT;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        if (side == null) return false;
        if (controller.state() == AdvancedBufferFlowController.State.BLOCKED) {
            return false;
        }
        if (role(side) == AdvancedBufferSideRole.MAIN_OUTPUT) {
            return controller.state() != AdvancedBufferFlowController.State.RESERVE;
        }
        // The reserve face is also a normal supply outlet. Its special role is
        // selecting the returned queue while the controller is in RESERVE;
        // it must not leave a directly attached requesting jar permanently dry.
        return role(side) == AdvancedBufferSideRole.RESERVE_OUTPUT;
    }

    @Override
    public void setSuction(@Nullable String aspect, int amount) {
    }

    @Override
    public @Nullable String suctionType(Direction side) {
        return null;
    }

    @Override
    public int suctionAmount(Direction side) {
        if (side == null) return 0;
        if (role(side) == AdvancedBufferSideRole.INPUT) {
            return INPUT_SUCTION;
        }
        return 0;
    }

    @Override
    public EssentiaFlowMode suctionFlowMode(Direction side) {
        return EssentiaFlowMode.SUPPLY;
    }

    @Override
    public long suctionController(Direction side) {
        return 0L;
    }

    @Override
    public @Nullable String essentiaType(Direction side) {
        return requestedAspect(outputStore(side), side);
    }

    @Override
    public int essentiaAmount(Direction side) {
        return outputStore(side).total();
    }

    @Override
    public int minimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(String aspect, int amount, Direction side) {
        if (amount <= 0 || !canOutputTo(side)) return 0;
        EssentiaStore store = outputStore(side);
        if (!store.remove(aspect, 1)) return 0;
        EssentiaSync.changed(this);
        return 1;
    }

    private EssentiaStore outputStore(@Nullable Direction side) {
        return side != null
                && role(side) == AdvancedBufferSideRole.RESERVE_OUTPUT
                && controller.state() == AdvancedBufferFlowController.State.RESERVE
                ? returned : supply;
    }

    private @Nullable String requestedAspect(EssentiaStore store,
            @Nullable Direction side) {
        if (level != null && side != null) {
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, side).orElse(null);
            String wanted = remote == null ? null
                    : remote.suctionType(side.getOpposite());
            if (wanted != null && store.amount(wanted) > 0) return wanted;
        }
        return first(store);
    }

    @Override
    public int addEssentia(String aspect, int amount, Direction side) {
        if (amount != 1 || !canInputFrom(side) || !hasRoom(aspect)) return 0;
        supply.add(aspect, 1);
        EssentiaSync.changed(this);
        return 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Supply", supply.save());
        tag.put("Returned", returned.save());
        byte[] encodedRoles = new byte[roles.length];
        for (int index = 0; index < roles.length; index++) {
            encodedRoles[index] = (byte) roles[index].ordinal();
        }
        tag.putByteArray("Roles", encodedRoles);
        tag.putString("ControllerState", controller.state().name());
        tag.putInt("ControllerTimer", controller.timer());
        tag.putInt("ControllerQuiet", controller.quietTicks());
        tag.putString("BlockedReason", blockedReasonKey);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        supply.load(tag.getCompound("Supply"));
        returned.load(tag.getCompound("Returned"));
        trimCapacity();
        byte[] encodedRoles = tag.getByteArray("Roles");
        if (encodedRoles.length == roles.length) {
            AdvancedBufferSideRole[] values = AdvancedBufferSideRole.values();
            for (int index = 0; index < roles.length; index++) {
                int role = encodedRoles[index];
                roles[index] = role >= 0 && role < values.length
                        ? values[role] : AdvancedBufferSideRole.BLOCKED;
            }
        }
        try {
            controller = new AdvancedBufferFlowController.Snapshot(
                    AdvancedBufferFlowController.State.valueOf(
                            tag.getString("ControllerState")),
                    tag.getInt("ControllerTimer"),
                    tag.getInt("ControllerQuiet"));
        } catch (IllegalArgumentException ignored) {
            controller = AdvancedBufferFlowController.Snapshot.idle();
        }
        String reason = tag.getString("BlockedReason");
        blockedReasonKey = reason.isBlank()
                ? "diagnostic.thaumcraftmodern.advanced_buffer.ok" : reason;
    }

    private void trimCapacity() {
        for (String aspect : new ArrayList<>(supply.view().keySet())) {
            int overflow = supply.amount(aspect) + returned.amount(aspect)
                    - CAPACITY_PER_ASPECT;
            if (overflow > 0) supply.remove(aspect,
                    Math.min(overflow, supply.amount(aspect)));
        }
        for (String aspect : new ArrayList<>(returned.view().keySet())) {
            int overflow = supply.amount(aspect) + returned.amount(aspect)
                    - CAPACITY_PER_ASPECT;
            if (overflow > 0) returned.remove(aspect,
                    Math.min(overflow, returned.amount(aspect)));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag == null) return;
        AdvancedBufferSideRole[] previousRoles = roles.clone();
        load(tag);
        if (level != null && level.isClientSide
                && !Arrays.equals(previousRoles, roles)) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state,
                    Block.UPDATE_CLIENTS);
        }
    }
}
