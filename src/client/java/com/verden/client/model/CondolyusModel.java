package com.verden.client.model;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

/** Condolyus: pequeno lagarto de fogo com placas brilhantes nas costas. */
public class CondolyusModel<T extends MobEntity> extends SinglePartEntityModel<T> {
    private static final float RAD = 0.017453292f;

    private final ModelPart root, body, head, fr, fl, br, bl, tail1, tail2, tail3;

    public CondolyusModel(ModelPart root) {
        this.root = root;
        this.body = CondolyusParts.find(root, "body");
        this.head = CondolyusParts.find(root, "head");
        this.fr = CondolyusParts.find(root, "fr");
        this.fl = CondolyusParts.find(root, "fl");
        this.br = CondolyusParts.find(root, "br");
        this.bl = CondolyusParts.find(root, "bl");
        this.tail1 = CondolyusParts.find(root, "tail1");
        this.tail2 = CondolyusParts.find(root, "tail2");
        this.tail3 = CondolyusParts.find(root, "tail3");
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        for (ModelPart p : new ModelPart[]{body, head, fr, fl, br, bl, tail1, tail2, tail3}) {
            p.resetTransform();
        }
        float swing = MathHelper.cos(limbAngle * 0.8f) * 1.2f * limbDistance;
        fr.pitch += swing;
        bl.pitch += swing;
        fl.pitch -= swing;
        br.pitch -= swing;
        head.yaw = headYaw * RAD * 0.8f;
        head.pitch += headPitch * RAD * 0.8f;
        float wag = MathHelper.sin(animationProgress * 0.25f) * 0.25f + MathHelper.cos(limbAngle * 0.8f) * 0.3f * limbDistance;
        tail1.yaw = wag;
        tail2.yaw = wag * 1.2f;
        tail3.yaw = wag * 1.4f;
        body.pitch += MathHelper.sin(animationProgress * 0.12f) * 0.02f;
    }
}
