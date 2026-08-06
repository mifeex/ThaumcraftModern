package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Generic TC4 VisNetHandler-style entry point for non-node machines. */
public final class VisMachineAccess {
    private VisMachineAccess(){}
    public static int consumeNearest(ServerLevel level,BlockPos origin,PrimalAspect aspect,int amount){
        VisNetworkNodeBlockEntity nearest=null; double distance=Double.MAX_VALUE;
        for(BlockPos cursor:BlockPos.betweenClosed(origin.offset(-VisNetworkNodeBlockEntity.RANGE,-VisNetworkNodeBlockEntity.RANGE,-VisNetworkNodeBlockEntity.RANGE),origin.offset(VisNetworkNodeBlockEntity.RANGE,VisNetworkNodeBlockEntity.RANGE,VisNetworkNodeBlockEntity.RANGE))){
            if(!(level.getBlockEntity(cursor) instanceof VisNetworkNodeBlockEntity node)
                    || !node.hasRouteToSource(new java.util.HashSet<>())) continue;
            double next=cursor.distSqr(origin);
            if(next<=VisNetworkNodeBlockEntity.RANGE*VisNetworkNodeBlockEntity.RANGE && next<distance){nearest=node;distance=next;}
        }
        return nearest==null?0:nearest.consumeVis(aspect,amount);
    }
}
