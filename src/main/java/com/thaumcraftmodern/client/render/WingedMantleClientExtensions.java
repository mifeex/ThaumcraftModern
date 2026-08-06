package com.thaumcraftmodern.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.EnumMap;

public final class WingedMantleClientExtensions {
    private WingedMantleClientExtensions() { }

    public static IClientItemExtensions create() {
        return new IClientItemExtensions() {
            private final EnumMap<EquipmentSlot, WingedMantleArmorModel> models =
                    new EnumMap<>(EquipmentSlot.class);

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                    HumanoidModel<?> defaultModel) {
                WingedMantleArmorModel model = models.computeIfAbsent(slot,
                        ignored -> new WingedMantleArmorModel(
                            Minecraft.getInstance().getEntityModels()
                                    .bakeLayer(WingedMantleArmorModel.LAYER)));
                copyPose(defaultModel, model);
                model.configureForSlot(slot);
                return model;
            }
        };
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyPose(HumanoidModel<?> source,
                                 WingedMantleArmorModel target) {
        ((HumanoidModel) source).copyPropertiesTo(target);
    }
}
