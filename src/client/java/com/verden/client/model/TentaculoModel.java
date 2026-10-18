package com.verden.client.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

/** Tentáculo segmentado que ondula. */
public class TentaculoModel<T extends MobEntity> extends SinglePartEntityModel<T> {
    private final ModelPart root;
    private final ModelPart[] segments = new ModelPart[5];

    public TentaculoModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < segments.length; i++) {
            segments[i] = TentaculoParts.find(root, "s" + i);
        }
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float agitation = entity.isAttacking() ? 2.2f : 1.0f;
        for (int i = 0; i < segments.length; i++) {
            ModelPart s = segments[i];
            s.resetTransform();
            s.pitch += MathHelper.sin(animationProgress * 0.12f * agitation + i * 0.7f) * 0.22f;
            s.roll += MathHelper.cos(animationProgress * 0.09f * agitation + i * 0.7f) * 0.18f;
        }
        segments[0].yaw += headYaw * 0.017453292f * 0.5f;
    }
}
