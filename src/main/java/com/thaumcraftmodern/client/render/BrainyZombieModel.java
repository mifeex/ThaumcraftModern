package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Zombie arm animation over the vanilla zombie geometry used by TC4.
 */
public final class BrainyZombieModel
        extends AbstractZombieModel<LegacyThaumcraftMob> {
    public BrainyZombieModel(ModelPart root) {
        super(root);
    }

    @Override
    public boolean isAggressive(LegacyThaumcraftMob entity) {
        return entity.isAggressive();
    }
}
