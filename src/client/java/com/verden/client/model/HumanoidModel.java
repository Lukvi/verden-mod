package com.verden.client.model;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

/** Modelo humanoide com braços/pernas articulados, capa e saia opcionais, e suporte a item na mão. */
public class HumanoidModel<T extends MobEntity> extends SinglePartEntityModel<T> implements ModelWithArms {
    private static final float RAD = 0.017453292f;

    private final ModelPart root, head, rArm, lArm, rLeg, lLeg;
    private final ModelPart cape, skirt;

    public HumanoidModel(ModelPart root, BiFunction<ModelPart, String, ModelPart> find, Predicate<String> has) {
        this.root = root;
        this.head = find.apply(root, "head");
        this.rArm = find.apply(root, "r_arm");
        this.lArm = find.apply(root, "l_arm");
        this.rLeg = find.apply(root, "r_leg");
        this.lLeg = find.apply(root, "l_leg");
        this.cape = has.test("cape") ? find.apply(root, "cape") : null;
        this.skirt = has.test("skirt") ? find.apply(root, "skirt") : null;
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setArmAngle(Arm arm, MatrixStack matrices) {
        (arm == Arm.RIGHT ? rArm : lArm).rotate(matrices);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        for (ModelPart p : new ModelPart[]{head, rArm, lArm, rLeg, lLeg}) {
            p.resetTransform();
        }
        if (cape != null) {
            cape.resetTransform();
        }
        if (skirt != null) {
            skirt.resetTransform();
        }

        float swing = MathHelper.cos(limbAngle * 0.6662f) * 1.3f * limbDistance;
        rLeg.pitch += swing;
        lLeg.pitch -= swing;
        rArm.pitch -= swing * 0.8f;
        lArm.pitch += swing * 0.8f;
        head.yaw = headYaw * RAD;
        head.pitch = headPitch * RAD;

        // respiração leve
        rArm.roll += MathHelper.cos(animationProgress * 0.09f) * 0.03f + 0.03f;
        lArm.roll -= MathHelper.cos(animationProgress * 0.09f) * 0.03f + 0.03f;

        if (entity.isAttacking()) {
            rArm.pitch -= 0.7f;
            lArm.pitch -= 0.3f;
        }
        float s = entity.handSwingProgress;
        if (s > 0f) {
            rArm.pitch -= MathHelper.sin(s * (float) Math.PI) * 1.9f;
            rArm.yaw += MathHelper.sin(s * (float) Math.PI) * 0.4f;
        }

        if (cape != null) {
            cape.pitch = 0.08f + limbDistance * 0.5f + MathHelper.sin(animationProgress * 0.05f) * 0.03f;
        }
        if (skirt != null) {
            skirt.pitch = limbDistance * 0.18f * MathHelper.cos(limbAngle * 0.6662f) + MathHelper.sin(animationProgress * 0.05f) * 0.02f;
        }
    }
}
