package com.verden.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;

/** Modelo do Licantropo (e da Amálgama de Koold, que acrescenta tentáculos e núcleo). */
public class LicantropoModel<T extends MobEntity> extends SinglePartEntityModel<T> {
    private static final float RAD = 0.017453292f;

    private final ModelPart root;
    private final ModelPart body, head, jaw, tail;
    private final ModelPart rArm, lArm, rForearm, lForearm;
    private final ModelPart rLeg, lLeg, rShin, lShin;
    private final List<ModelPart> tentacles = new ArrayList<>();

    public LicantropoModel(ModelPart root, BiFunction<ModelPart, String, ModelPart> find, boolean boss) {
        this.root = root;
        this.body = find.apply(root, "body");
        this.head = find.apply(root, "head");
        this.jaw = find.apply(root, "jaw");
        this.tail = find.apply(root, "tail");
        this.rArm = find.apply(root, "r_arm");
        this.lArm = find.apply(root, "l_arm");
        this.rForearm = find.apply(root, "r_forearm");
        this.lForearm = find.apply(root, "l_forearm");
        this.rLeg = find.apply(root, "r_leg");
        this.lLeg = find.apply(root, "l_leg");
        this.rShin = find.apply(root, "r_shin");
        this.lShin = find.apply(root, "l_shin");
        if (boss) {
            for (int n = 0; n < 4; n++) {
                for (String s : new String[]{"a", "b", "c"}) {
                    tentacles.add(find.apply(root, "bt" + n + s));
                }
            }
            for (int n = 0; n < 2; n++) {
                for (String s : new String[]{"a", "b"}) {
                    tentacles.add(find.apply(root, "jt" + n + s));
                }
            }
        }
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        for (ModelPart p : new ModelPart[]{body, head, jaw, tail, rArm, lArm, rForearm, lForearm, rLeg, lLeg, rShin, lShin}) {
            p.resetTransform();
        }
        for (ModelPart p : tentacles) {
            p.resetTransform();
        }

        float swing = MathHelper.cos(limbAngle * 0.6662f) * 1.1f * limbDistance;
        rLeg.pitch += swing;
        lLeg.pitch -= swing;
        rShin.pitch += Math.max(0f, -swing) * 0.7f;
        lShin.pitch += Math.max(0f, swing) * 0.7f;
        rArm.pitch -= swing * 0.8f;
        lArm.pitch += swing * 0.8f;

        head.yaw = headYaw * RAD * 0.8f;
        head.pitch += headPitch * RAD * 0.8f;
        body.pitch += MathHelper.sin(animationProgress * 0.08f) * 0.03f;
        tail.yaw = MathHelper.cos(limbAngle * 0.6662f) * 0.25f * limbDistance + MathHelper.sin(animationProgress * 0.1f) * 0.06f;

        if (entity.isAttacking()) {
            rArm.pitch -= 1.0f;
            lArm.pitch -= 1.0f;
            rForearm.pitch -= 0.5f;
            lForearm.pitch -= 0.5f;
            jaw.pitch += 0.55f + MathHelper.sin(animationProgress * 0.6f) * 0.1f;
        } else {
            jaw.pitch += 0.08f + MathHelper.sin(animationProgress * 0.12f) * 0.03f;
        }

        for (int i = 0; i < tentacles.size(); i++) {
            ModelPart t = tentacles.get(i);
            t.pitch += MathHelper.sin(animationProgress * 0.1f + i * 0.7f) * 0.15f;
            t.roll += MathHelper.cos(animationProgress * 0.08f + i) * 0.1f;
        }
    }
}
