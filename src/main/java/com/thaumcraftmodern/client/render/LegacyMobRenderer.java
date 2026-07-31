package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class LegacyMobRenderer extends HumanoidMobRenderer<
        LegacyThaumcraftMob,
        HumanoidModel<LegacyThaumcraftMob>> {
    public LegacyMobRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                0.5F
        );
        addLayer(new CrimsonCultArmorLayer(
                this,
                new CrimsonCultArmorModel(
                        context.bakeLayer(CrimsonCultArmorModel.KNIGHT_LAYER)
                ),
                new CrimsonCultArmorModel(
                        context.bakeLayer(CrimsonCultArmorModel.CLERIC_LAYER)
                ),
                new CrimsonCultArmorModel(
                        context.bakeLayer(CrimsonCultArmorModel.PRAETOR_LAYER)
                ),
                new CrimsonCultArmorModel(
                        context.bakeLayer(CrimsonCultArmorModel.BOOTS_LAYER)
                ),
                new CrimsonCultArmorModel(
                        context.bakeLayer(
                                CrimsonCultArmorModel.ARM_UNDERLAY_LAYER
                        )
                )
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        if (entity.kind() == LegacyMobKind.PECH) {
            String texture = switch (entity.pechType()) {
                case 1 -> "pech_thaum.png";
                case 2 -> "pech_stalker.png";
                default -> "pech_forage.png";
            };
            return new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/entity/models/" + texture
            );
        }
        return entity.kind().texture();
    }

    @Override
    protected void scale(
            LegacyThaumcraftMob entity,
            PoseStack pose,
            float partialTick
    ) {
        float scale = switch (entity.kind()) {
            case ELDRITCH_CONSTRUCT -> 1.65F;
            case ELDRITCH_WARDEN, CRIMSON_PRAETOR -> 1.25F;
            case TAINT_TENDRIL -> 0.6F;
            case GIANT_TAINTACLE -> 2.4F;
            case TAINTED_CHICKEN -> 0.55F;
            default -> 1.0F;
        };
        pose.scale(scale, scale, scale);
    }
}
