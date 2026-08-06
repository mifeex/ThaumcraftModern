package com.thaumcraftmodern.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class BootsTravellerItem extends ArmorItem {
    public static final double STEP_HEIGHT_ADDITION = 0.4D;
    public static final double JUMP_VELOCITY_ADDITION = 0.275D;
    private static final UUID STEP_HEIGHT_UUID = UUID.fromString(
            "a4563f8b-75d7-49fa-a59a-410b6f0af349"
    );

    public BootsTravellerItem(Properties properties) {
        super(TravellerArmorMaterial.INSTANCE, Type.BOOTS, properties);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (!player.getAbilities().flying && player.zza > 0.0F) {
            if (player.onGround()) {
                float bonus = player.isInWater() ? 0.055F / 4.0F : 0.055F;
                player.moveRelative(bonus, new net.minecraft.world.phys.Vec3(0.0D, 0.0D, 1.0D));
            } else {
                player.moveRelative(0.05F, new net.minecraft.world.phys.Vec3(0.0D, 0.0D, 1.0D));
            }
        }
        if (player.fallDistance > 0.0F) player.fallDistance = Math.max(0.0F, player.fallDistance - 0.25F);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(
            EquipmentSlot slot
    ) {
        if (slot != EquipmentSlot.FEET) {
            return super.getDefaultAttributeModifiers(slot);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> modifiers =
                ImmutableMultimap.builder();
        modifiers.putAll(super.getDefaultAttributeModifiers(slot));
        modifiers.put(
                ForgeMod.STEP_HEIGHT_ADDITION.get(),
                new AttributeModifier(
                        STEP_HEIGHT_UUID,
                        "Boots of the Traveler step height",
                        STEP_HEIGHT_ADDITION,
                        AttributeModifier.Operation.ADDITION
                )
        );
        return modifiers.build();
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (!(boots.getItem() instanceof BootsTravellerItem)) return;
        player.setDeltaMovement(
                player.getDeltaMovement().add(0.0D, JUMP_VELOCITY_ADDITION, 0.0D)
        );
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return ThaumcraftModern.MOD_ID + ":textures/models/bootstraveler.png";
    }
}
