package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * Recreates the original TC4 LayerBipedArmor presentation for the three
 * Crimson Cult ranks while keeping equipment and combat state server-owned.
 */
public final class CrimsonCultArmorLayer extends RenderLayer<
        LegacyThaumcraftMob,
        HumanoidModel<LegacyThaumcraftMob>> {
    private static final Map<LegacyMobKind, ResourceLocation> TEXTURES = Map.of(
            LegacyMobKind.CRIMSON_KNIGHT, texture("cultist_plate_armor.png"),
            LegacyMobKind.CRIMSON_INQUISITOR, texture("inquisitor_plate_armor.png"),
            LegacyMobKind.CRIMSON_CLERIC, texture("cultist_robe_armor.png"),
            LegacyMobKind.CRIMSON_PRAETOR, texture("cultist_leader_armor.png")
    );
    private static final ResourceLocation BOOTS =
            texture("cultistboots.png");
    private final Map<LegacyMobKind, CrimsonCultArmorModel> armorModels;
    private final CrimsonCultArmorModel bootsModel;

    public CrimsonCultArmorLayer(
            RenderLayerParent<
                    LegacyThaumcraftMob,
                    HumanoidModel<LegacyThaumcraftMob>> parent,
            CrimsonCultArmorModel knightModel,
            CrimsonCultArmorModel clericModel,
            CrimsonCultArmorModel praetorModel,
            CrimsonCultArmorModel bootsModel
    ) {
        super(parent);
        this.armorModels = Map.of(
            LegacyMobKind.CRIMSON_KNIGHT, knightModel,
            LegacyMobKind.CRIMSON_INQUISITOR, knightModel,
            LegacyMobKind.CRIMSON_CLERIC, clericModel,
                LegacyMobKind.CRIMSON_PRAETOR, praetorModel
        );
        this.bootsModel = bootsModel;
    }

    @Override
    public void render(
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            LegacyThaumcraftMob entity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        CrimsonCultArmorModel armor = armorModels.get(entity.kind());
        ResourceLocation armorTexture = TEXTURES.get(entity.kind());
        if (armor == null || armorTexture == null) {
            return;
        }

        prepare(armor, entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch);
        renderModel(armor, armorTexture, pose, buffers, packedLight);

        prepare(bootsModel, entity, limbSwing, limbSwingAmount, ageInTicks,
                netHeadYaw, headPitch);
        renderModel(bootsModel, BOOTS, pose, buffers, packedLight);
    }

    private void prepare(
            CrimsonCultArmorModel model,
            LegacyThaumcraftMob entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        getParentModel().copyPropertiesTo(model);
        model.setupAnim(
                entity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch
        );
    }

    private static void renderModel(
            CrimsonCultArmorModel model,
            ResourceLocation texture,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight
    ) {
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.armorCutoutNoCull(texture)
        );
        model.renderToBuffer(
                pose,
                vertices,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID,
                "textures/entity/models/" + name
        );
    }
}
