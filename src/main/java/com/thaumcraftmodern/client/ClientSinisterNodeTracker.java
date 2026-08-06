package com.thaumcraftmodern.client;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.item.SinisterLodestoneItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;

/** Loaded-client-node equivalent of TC4 ItemCompassStone.sinisterNodes. */
public final class ClientSinisterNodeTracker {
    private static long sampledTick=Long.MIN_VALUE;private static final List<Vec3> NODES=new ArrayList<>();
    private ClientSinisterNodeTracker(){}
    public static boolean pointsAt(ClientLevel level,LivingEntity holder){refresh(level,holder);for(Vec3 node:NODES)if(SinisterLodestoneItem.isVisibleTo(holder,node))return true;return false;}
    private static void refresh(ClientLevel level,LivingEntity holder){if(sampledTick==level.getGameTime())return;sampledTick=level.getGameTime();NODES.clear();int cx=holder.chunkPosition().x,cz=holder.chunkPosition().z;
        for(int x=cx-16;x<=cx+16;x++)for(int z=cz-16;z<=cz+16;z++){
            var chunk=level.getChunkSource().getChunk(x,z,false);if(chunk==null)continue;
            for(var blockEntity:chunk.getBlockEntities().values())if(blockEntity instanceof AuraNodeBlockEntity node&&node.snapshotState().type()==AuraNodeType.DARK)NODES.add(node.getBlockPos().getCenter());
        }
    }
}
