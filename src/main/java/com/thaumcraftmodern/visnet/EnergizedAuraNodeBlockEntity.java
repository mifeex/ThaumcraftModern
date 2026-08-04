package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** TC4 energized-node source: a square-root primal projection refreshed every tick. */
public final class EnergizedAuraNodeBlockEntity
        extends VisNetworkNodeBlockEntity {
    private AuraNodeState original = defaultState();
    private final EnumMap<PrimalAspect, Integer> visBase =
            new EnumMap<>(PrimalAspect.class);
    private final EnumMap<PrimalAspect, Integer> vis =
            new EnumMap<>(PrimalAspect.class);

    public EnergizedAuraNodeBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ENERGIZED_AURA_NODE.get(), position, state);
        setupNode();
    }

    public void initialize(AuraNodeState state) {
        original = state.copy();
        setupNode();
        sync();
    }

    public AuraNodeState originalState() {
        return original.copy();
    }

    /**
     * TC4 renders an energized node from TileNodeEnergized.auraBase, not from
     * the ordinary node's drained current pool. Keep {@link #originalState()}
     * untouched for the exact empty-node restoration path.
     */
    public AuraNodeState displayState() {
        return displayState(original);
    }

    static AuraNodeState displayState(AuraNodeState source) {
        AuraNodeState.Snapshot snapshot = source.snapshot();
        return AuraNodeState.withAspects(
                snapshot.nodeId(),
                snapshot.type(),
                snapshot.modifier(),
                snapshot.aspectsMaximum(),
                snapshot.aspectsMaximum(),
                snapshot.revision()
        );
    }

    public Map<PrimalAspect, Integer> visBase() {
        return Map.copyOf(visBase);
    }

    @Override
    protected void serverNetworkTick() {
        if (original.type() == AuraNodeType.UNSTABLE
                && level != null && level.random.nextInt(500) == 1) {
            visBase.clear();
        }
        if (visBase.isEmpty()) {
            setupNode();
        }
        vis.clear();
        vis.putAll(visBase);
    }

    private void setupNode() {
        EnumMap<PrimalAspect, Integer> reduced =
                new EnumMap<>(PrimalAspect.class);
        original.snapshot().aspectsMaximum().forEach((aspect, amount) ->
                reduce(aspect, amount, reduced));
        visBase.clear();
        reduced.forEach((aspect, rawAmount) -> {
            int amount = energizedStrength(rawAmount, original.modifier());
            if (original.type() == AuraNodeType.UNSTABLE && level != null) {
                amount += level.random.nextInt(5) - 2;
            }
            if (amount > 0) {
                visBase.put(aspect, amount);
            }
        });
        vis.clear();
        vis.putAll(visBase);
    }

    static int energizedStrength(
            int primalAmount,
            AuraNodeModifier modifier
    ) {
        float modified = switch (modifier) {
            case BRIGHT -> primalAmount * 1.2F;
            case PALE -> primalAmount * 0.8F;
            case FADING -> primalAmount * 0.5F;
            case NORMAL -> primalAmount;
        };
        return (int) Math.floor(Math.sqrt(modified));
    }

    private static void reduce(
            String aspectId,
            int amount,
            EnumMap<PrimalAspect, Integer> output
    ) {
        if (amount <= 0) {
            return;
        }
        try {
            output.merge(PrimalAspect.fromId(aspectId), amount, Integer::sum);
            return;
        } catch (IllegalArgumentException ignored) {
            // Compound aspect; recurse through its exact runtime definition.
        }
        AspectDefinition definition = AspectRegistryRuntime.find(aspectId)
                .orElse(null);
        if (definition == null || definition.components().size() != 2) {
            return;
        }
        reduce(definition.components().get(0), amount, output);
        reduce(definition.components().get(1), amount, output);
    }

    @Override
    public boolean isSource() {
        return true;
    }

    @Override
    protected int consumeSource(PrimalAspect aspect, int amount) {
        int consumed = Math.min(vis.getOrDefault(aspect, 0), amount);
        if (consumed > 0) {
            vis.put(aspect, vis.get(aspect) - consumed);
        }
        return consumed;
    }

    @Override
    protected int availableSource(PrimalAspect aspect) {
        return vis.getOrDefault(aspect, 0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("OriginalNode", com.thaumcraftmodern.aura.AuraNodeCodec.encode(original));
        CompoundTag base = new CompoundTag();
        visBase.forEach((aspect, amount) -> base.putInt(aspect.id(), amount));
        tag.put("EnergizedBase", base);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OriginalNode")) {
            original = com.thaumcraftmodern.aura.AuraNodeCodec.decode(
                    tag.getCompound("OriginalNode"));
        }
        visBase.clear();
        CompoundTag base = tag.getCompound("EnergizedBase");
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            if (base.contains(aspect.id())) {
                visBase.put(aspect, base.getInt(aspect.id()));
            }
        }
        if (visBase.isEmpty()) {
            setupNode();
        }
        vis.clear();
        vis.putAll(visBase);
    }

    private static AuraNodeState defaultState() {
        EnumMap<PrimalAspect, Integer> aspects =
                new EnumMap<>(PrimalAspect.class);
        PrimalAspect.ordered().forEach(aspect -> aspects.put(aspect, 20));
        return new AuraNodeState(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                aspects,
                aspects,
                0
        );
    }
}
