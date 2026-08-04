package com.thaumcraftmodern.visnet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

final class VisNetwork {
    private VisNetwork() {
    }

    static @Nullable BlockPos findParent(
            ServerLevel level,
            VisNetworkNodeBlockEntity child
    ) {
        BlockPos origin = child.getBlockPos();
        double nearest = Double.MAX_VALUE;
        BlockPos result = null;
        for (BlockPos cursor : BlockPos.betweenClosed(
                origin.offset(-VisNetworkNodeBlockEntity.RANGE,
                        -VisNetworkNodeBlockEntity.RANGE,
                        -VisNetworkNodeBlockEntity.RANGE),
                origin.offset(VisNetworkNodeBlockEntity.RANGE,
                        VisNetworkNodeBlockEntity.RANGE,
                        VisNetworkNodeBlockEntity.RANGE))) {
            if (cursor.equals(origin)
                    || !(level.getBlockEntity(cursor)
                    instanceof VisNetworkNodeBlockEntity candidate)
                    || !attunementsMatch(child, candidate)
                    // A candidate whose route already passes through this
                    // child is a descendant, not a valid parent. Without the
                    // pre-seeded visited set two relays periodically select
                    // each other during the 40-tick rebuild, lose the route,
                    // and reconnect on the following rebuild.
                    || !candidate.hasRouteToSource(
                    new HashSet<>(Set.of(origin)))) {
                continue;
            }
            double distance = cursor.distSqr(origin);
            if (distance > VisNetworkNodeBlockEntity.RANGE
                    * VisNetworkNodeBlockEntity.RANGE
                    || distance >= nearest) {
                continue;
            }
            nearest = distance;
            result = cursor.immutable();
        }
        return result;
    }

    private static boolean attunementsMatch(
            VisNetworkNodeBlockEntity first,
            VisNetworkNodeBlockEntity second
    ) {
        return first.attunement() == -1 || second.attunement() == -1
                || first.attunement() == second.attunement();
    }

}
