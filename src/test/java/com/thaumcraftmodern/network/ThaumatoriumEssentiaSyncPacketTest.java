package com.thaumcraftmodern.network;

import com.thaumcraftmodern.network.packet.ThaumatoriumEssentiaSyncPacket;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ThaumatoriumEssentiaSyncPacketTest {
    @Test
    void roundTripsControllerPositionAndReservedEssentia() {
        CompoundTag essentia = new CompoundTag();
        essentia.putInt("marker", 3);
        ThaumatoriumEssentiaSyncPacket original =
                new ThaumatoriumEssentiaSyncPacket(
                        new BlockPos(12, 64, -9),
                        essentia
                );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ThaumatoriumEssentiaSyncPacket.encode(original, buffer);
        ThaumatoriumEssentiaSyncPacket decoded =
                ThaumatoriumEssentiaSyncPacket.decode(buffer);

        assertEquals(original.position(), decoded.position());
        assertEquals(3, decoded.essentia().getInt("marker"));
    }
}
