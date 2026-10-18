package com.verden.client.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

/** Rinoy: rinoceronte lanoso gigante. */
public class RinoyModel<T extends MobEntity> extends SinglePartEntityModel<T> {
    private static final float RAD = 0.017453292f;

    private final ModelPart root, body, head, fr, fl, br, bl, tail;

    public RinoyModel(ModelPart root) {
        this.root = root;
        this.body = RinoyParts.find(root, "body");
        this.head = RinoyParts.find(root, "head");
        this.fr = RinoyParts.find(root, "fr");
        this.fl = RinoyParts.find(root, "fl");
        this.br = RinoyParts.find(root, "br");
        this.bl = RinoyParts.find(root, "bl");
        this.tail = RinoyParts.find(root, "tail");
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        for (ModelPart p : new ModelPart[]{body, head, fr, fl, br, bl, tail}) {
            p.resetTransform();
        }
        float swing = MathHelper.cos(limbAngle * 0.6662f) * 0.9f * limbDistance;
        fr.pitch += swing;
        bl.pitch += swing;
        fl.pitch -= swing;
        br.pitch -= swing;
        head.yaw = headYaw * RAD * 0.7f;
        head.pitch += headPitch * RAD * 0.6f;
        body.pitch += MathHelper.sin(animationProgress * 0.07f) * 0.015f;
        body.roll += MathHelper.cos(limbAngle * 0.6662f) * 0.03f * limbDistance;
        tail.pitch += MathHelper.sin(animationProgress * 0.09f) * 0.08f;
        tail.yaw = MathHelper.cos(limbAngle * 0.6662f) * 0.2f * limbDistance;
    }
}
