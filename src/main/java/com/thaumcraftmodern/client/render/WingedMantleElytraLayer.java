package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.WingedMantleArmorItem;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** Exact vanilla Elytra render path for the Winged Mantle chest piece. */
public final class WingedMantleElytraLayer
        extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/entity/winged_mantle_elytra.png");

    private final ElytraModel<AbstractClientPlayer> model;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public WingedMantleElytraLayer(
            RenderLayerParent<AbstractClientPlayer,
                    PlayerModel<AbstractClientPlayer>> parent,
            EntityModelSet models) {
        super(parent);
        ModelPart root = models.bakeLayer(ModelLayers.ELYTRA);
        model = new ElytraModel<>(root);
        leftWing = root.getChild("left_wing");
        rightWing = root.getChild("right_wing");
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof WingedMantleArmorItem armor)
                || armor.getType() != ArmorItem.Type.CHESTPLATE) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        getParentModel().copyPropertiesTo(model);
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch);

        // Vanilla Elytra is static while walking. Preserve its base pose but
        // add the requested symmetric cloth movement for this mantle.
        if (!player.isFallFlying() && !player.isCrouching()) {
            float movement = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
            float swing = Mth.sin(limbSwing * 0.6662F) * 0.18F * movement;
            leftWing.zRot += swing;
            rightWing.zRot -= swing;
            leftWing.xRot += Math.abs(swing) * 0.15F;
            rightWing.xRot = leftWing.xRot;
        }

        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(
                buffers, RenderType.armorCutoutNoCull(TEXTURE), false,
                chest.hasFoil());
        model.renderToBuffer(poseStack, consumer, packedLight,
                OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
}
