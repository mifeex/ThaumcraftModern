package com.thaumcraftmodern.alchemy;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.BathSaltsItem;
import com.thaumcraftmodern.item.EssentiaCrystalItem;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.scan.ScanTargetType;
import com.thaumcraftmodern.world.block.LiquidDeathBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClassicAlchemyEvents {
    private ClassicAlchemyEvents() {
    }

    @SubscribeEvent
    public static void shortenBathSaltLife(ItemTossEvent event) {
        if (event.getEntity().getItem().getItem() instanceof BathSaltsItem) {
            event.getEntity().lifespan = BathSaltsItem.WATER_CONVERSION_TICKS;
        }
    }

    @SubscribeEvent
    public static void shortenEveryBathSaltEntityLife(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity item
                && item.getItem().getItem() instanceof BathSaltsItem) {
            item.lifespan = BathSaltsItem.WATER_CONVERSION_TICKS;
        }
    }

    @SubscribeEvent
    public static void convertWater(ItemExpireEvent event) {
        ItemEntity item = event.getEntity();
        if (!(item.getItem().getItem() instanceof BathSaltsItem)
                || !(item.level() instanceof ServerLevel level)) return;
        var pos = item.blockPosition();
        if (level.getBlockState(pos).is(Blocks.WATER)
                && level.getFluidState(pos).isSource()) {
            level.setBlock(pos, ModBlocks.PURIFYING_FLUID.get().defaultBlockState(), 3);
        }
    }

    @SubscribeEvent
    public static void crystallizeDissolvedEssence(LivingDropsEvent event) {
        if (!event.getSource().is(LiquidDeathBlock.DISSOLVE)
                || !(event.getEntity().level() instanceof ServerLevel level)) return;
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();
        ScanRegistry.findHistorical(ScanTargetType.ENTITY, entityId).ifPresent(definition -> {
            for (var aspect : definition.aspects()) {
                if (level.random.nextBoolean()) continue;
                int size = Math.max(1,
                        (1 + level.random.nextInt(aspect.amount())) / 2);
                var crystal = EssentiaCrystalItem.create(
                        ModItems.ESSENTIA_CRYSTAL.get(), aspect.aspectId());
                crystal.setCount(size);
                event.getDrops().add(new ItemEntity(level,
                        event.getEntity().getX(),
                        event.getEntity().getY() + event.getEntity().getEyeHeight(),
                        event.getEntity().getZ(), crystal));
            }
        });
    }
}
