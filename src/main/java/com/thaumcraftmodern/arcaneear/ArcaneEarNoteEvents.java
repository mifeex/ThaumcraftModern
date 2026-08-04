package com.thaumcraftmodern.arcaneear;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.ArcaneEarBlockEntity;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.event.level.NoteBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-side equivalent of TC4's per-world note-block event queue. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class ArcaneEarNoteEvents {
    public static final double HEARING_RANGE_SQUARED = 64.0D * 64.0D;
    private static final WeakHashMap<ServerLevel, Set<BlockPos>> LOADED_EARS =
            new WeakHashMap<>();

    private ArcaneEarNoteEvents() {
    }

    public static void register(ArcaneEarBlockEntity ear) {
        if (ear.getLevel() instanceof ServerLevel level) {
            LOADED_EARS.computeIfAbsent(level, ignored -> new HashSet<>())
                    .add(ear.getBlockPos().immutable());
        }
    }

    public static void unregister(ArcaneEarBlockEntity ear) {
        if (ear.getLevel() instanceof ServerLevel level) {
            Set<BlockPos> positions = LOADED_EARS.get(level);
            if (positions != null) {
                positions.remove(ear.getBlockPos());
                if (positions.isEmpty()) {
                    LOADED_EARS.remove(level);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onNotePlayed(NoteBlockEvent.Play event) {
        if (event.getLevel() instanceof ServerLevel level) {
            dispatch(
                    level,
                    event.getPos(),
                    classicTone(event.getInstrument()),
                    event.getVanillaNoteId()
            );
        }
    }

    public static void dispatch(
            ServerLevel level,
            BlockPos source,
            int tone,
            int note
    ) {
        Set<BlockPos> positions = LOADED_EARS.get(level);
        if (positions == null) {
            return;
        }
        Iterator<BlockPos> iterator = positions.iterator();
        while (iterator.hasNext()) {
            BlockPos earPosition = iterator.next();
            if (!level.isLoaded(earPosition)) {
                continue;
            }
            if (!(level.getBlockEntity(earPosition)
                    instanceof ArcaneEarBlockEntity ear)) {
                iterator.remove();
                continue;
            }
            if (ear.matches(tone, note)
                    && earPosition.distSqr(source) <= HEARING_RANGE_SQUARED) {
                ear.hearNote();
            }
        }
    }

    /** TC4 only had harp, bass drum, snare, hat and bass. */
    public static int classicTone(NoteBlockInstrument instrument) {
        return switch (instrument) {
            case BASEDRUM -> 1;
            case SNARE -> 2;
            case HAT -> 3;
            case BASS -> 4;
            default -> 0;
        };
    }

    public static NoteBlockInstrument instrument(int tone) {
        return switch (tone) {
            case 1 -> NoteBlockInstrument.BASEDRUM;
            case 2 -> NoteBlockInstrument.SNARE;
            case 3 -> NoteBlockInstrument.HAT;
            case 4 -> NoteBlockInstrument.BASS;
            default -> NoteBlockInstrument.HARP;
        };
    }
}
