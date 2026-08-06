package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    private ModTags() { }
    public static final class Items {
        public static final TagKey<Item> RUNIC_AUGMENTABLE = TagKey.create(
                Registries.ITEM, new ResourceLocation(ThaumcraftModern.MOD_ID,
                        "runic_augmentable"));
        private Items() { }
    }
}
