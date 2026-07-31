package com.thaumcraftmodern.item;

import com.thaumcraftmodern.essentia.WardedJarContents;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WardedJarItemTest {
    @Test
    void preservesExactAspectAmountAndFilterInBlockEntityTag() {
        CompoundTag payload = new CompoundTag();
        payload.putString("Aspect", "ignis");
        payload.putInt("Amount", 37);
        payload.putString("AspectFilter", "ignis");
        payload.putInt("Facing", Direction.WEST.ordinal());

        WardedJarContents contents = WardedJarContents.read(payload).orElseThrow();

        assertEquals("ignis", contents.aspect());
        assertEquals(37, contents.amount());
        assertEquals("ignis", contents.filter());
        assertEquals(Direction.WEST, contents.filterFacing());
        assertEquals(37, payload.getInt("Amount"));
    }
}
