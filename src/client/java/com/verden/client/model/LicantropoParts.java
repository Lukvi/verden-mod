// GERADO por tools/model_gen.py - nao edite a mao.
package com.verden.client.model;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

public final class LicantropoParts {
    public static final int TEX_W = 128;
    public static final int TEX_H = 128;
    public static final float HEIGHT_PX = 37.0492520241347f;
    private static final Map<String, String> PATHS = new HashMap<>();
    private LicantropoParts() {}

    static {
        PATHS.put("hips", "hips");
        PATHS.put("body", "hips/body");
        PATHS.put("head", "hips/body/head");
        PATHS.put("jaw", "hips/body/head/jaw");
        PATHS.put("r_arm", "hips/body/r_arm");
        PATHS.put("r_forearm", "hips/body/r_arm/r_forearm");
        PATHS.put("r_hand", "hips/body/r_arm/r_forearm/r_hand");
        PATHS.put("r_leg", "hips/r_leg");
        PATHS.put("r_shin", "hips/r_leg/r_shin");
        PATHS.put("r_foot", "hips/r_leg/r_shin/r_foot");
        PATHS.put("l_arm", "hips/body/l_arm");
        PATHS.put("l_forearm", "hips/body/l_arm/l_forearm");
        PATHS.put("l_hand", "hips/body/l_arm/l_forearm/l_hand");
        PATHS.put("l_leg", "hips/l_leg");
        PATHS.put("l_shin", "hips/l_leg/l_shin");
        PATHS.put("l_foot", "hips/l_leg/l_shin/l_foot");
        PATHS.put("tail", "hips/tail");
    }

    public static boolean has(String name) { return PATHS.containsKey(name); }

    public static ModelPart find(ModelPart root, String name) {
        ModelPart p = root;
        for (String s : PATHS.get(name).split("/")) { p = p.getChild(s); }
        return p;
    }

    public static TexturedModelData create() {
        ModelData md = new ModelData();
        ModelPartData root = md.getRoot();
        ModelPartData p_hips = root.addChild("hips", ModelPartBuilder.create().uv(0, 0).cuboid(-4.5f, -2.0f, -3.5f, 9.0f, 8.0f, 8.0f), ModelTransform.of(0.0f, 6.676209788408279f, 2.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_body = p_hips.addChild("body", ModelPartBuilder.create().uv(34, 0).cuboid(-5.0f, -6.0f, -4.0f, 10.0f, 7.0f, 8.0f).uv(70, 0).cuboid(-7.5f, -17.0f, -5.0f, 15.0f, 12.0f, 10.0f), ModelTransform.of(0.0f, -1.0f, -1.0f, 0.5f, 0.0f, 0.0f));
        ModelPartData p_head = p_body.addChild("head", ModelPartBuilder.create().uv(0, 22).cuboid(-4.0f, -4.0f, -6.0f, 8.0f, 8.0f, 7.0f).uv(30, 22).cuboid(-2.5f, -0.5f, -12.0f, 5.0f, 3.0f, 6.0f).uv(52, 22).cuboid(-4.5f, -7.0f, -2.0f, 2.0f, 4.0f, 2.0f).uv(52, 22).mirrored(true).cuboid(2.5f, -7.0f, -2.0f, 2.0f, 4.0f, 2.0f).mirrored(false), ModelTransform.of(0.0f, -15.0f, -3.0f, -0.5f, 0.0f, 0.0f));
        ModelPartData p_jaw = p_head.addChild("jaw", ModelPartBuilder.create().uv(60, 22).cuboid(-2.0f, 0.0f, -6.0f, 4.0f, 2.0f, 6.0f), ModelTransform.of(0.0f, 2.5f, -6.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_r_arm = p_body.addChild("r_arm", ModelPartBuilder.create().uv(80, 22).cuboid(-2.5f, -1.0f, -2.5f, 5.0f, 10.0f, 5.0f), ModelTransform.of(-9.5f, -13.0f, 0.0f, -0.6f, 0.0f, -0.06f));
        ModelPartData p_r_forearm = p_r_arm.addChild("r_forearm", ModelPartBuilder.create().uv(100, 22).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 4.0f), ModelTransform.of(0.0f, 9.0f, 0.0f, -0.3f, 0.0f, 0.0f));
        ModelPartData p_r_hand = p_r_forearm.addChild("r_hand", ModelPartBuilder.create().uv(0, 37).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 3.0f, 5.0f).uv(20, 37).cuboid(-2.0f, 3.0f, -3.0f, 1.0f, 3.0f, 1.0f).uv(24, 37).cuboid(-0.5f, 3.0f, -3.0f, 1.0f, 3.0f, 1.0f).uv(28, 37).cuboid(1.0f, 3.0f, -3.0f, 1.0f, 3.0f, 1.0f), ModelTransform.of(0.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_r_leg = p_hips.addChild("r_leg", ModelPartBuilder.create().uv(32, 37).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 8.0f, 5.0f), ModelTransform.of(-3.5f, 2.0f, 1.0f, -0.5f, 0.0f, 0.0f));
        ModelPartData p_r_shin = p_r_leg.addChild("r_shin", ModelPartBuilder.create().uv(52, 37).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f), ModelTransform.of(0.0f, 8.0f, 0.0f, 0.95f, 0.0f, 0.0f));
        ModelPartData p_r_foot = p_r_shin.addChild("r_foot", ModelPartBuilder.create().uv(68, 37).cuboid(-2.0f, 0.0f, -5.0f, 4.0f, 2.0f, 6.0f).uv(88, 37).cuboid(-2.0f, 0.0f, -7.0f, 4.0f, 1.0f, 2.0f), ModelTransform.of(0.0f, 7.0f, 0.0f, -0.45f, 0.0f, 0.0f));
        ModelPartData p_l_arm = p_body.addChild("l_arm", ModelPartBuilder.create().uv(80, 22).mirrored(true).cuboid(-2.5f, -1.0f, -2.5f, 5.0f, 10.0f, 5.0f).mirrored(false), ModelTransform.of(9.5f, -13.0f, 0.0f, -0.6f, 0.0f, 0.06f));
        ModelPartData p_l_forearm = p_l_arm.addChild("l_forearm", ModelPartBuilder.create().uv(100, 22).mirrored(true).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 4.0f).mirrored(false), ModelTransform.of(0.0f, 9.0f, 0.0f, -0.3f, 0.0f, 0.0f));
        ModelPartData p_l_hand = p_l_forearm.addChild("l_hand", ModelPartBuilder.create().uv(0, 37).mirrored(true).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 3.0f, 5.0f).mirrored(false).uv(20, 37).mirrored(true).cuboid(-2.0f, 3.0f, -3.0f, 1.0f, 3.0f, 1.0f).mirrored(false).uv(24, 37).mirrored(true).cuboid(-0.5f, 3.0f, -3.0f, 1.0f, 3.0f, 1.0f).mirrored(false).uv(28, 37).mirrored(true).cuboid(1.0f, 3.0f, -3.0f, 1.0f, 3.0f, 1.0f).mirrored(false), ModelTransform.of(0.0f, 10.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_l_leg = p_hips.addChild("l_leg", ModelPartBuilder.create().uv(32, 37).mirrored(true).cuboid(-2.5f, 0.0f, -2.5f, 5.0f, 8.0f, 5.0f).mirrored(false), ModelTransform.of(3.5f, 2.0f, 1.0f, -0.5f, 0.0f, 0.0f));
        ModelPartData p_l_shin = p_l_leg.addChild("l_shin", ModelPartBuilder.create().uv(52, 37).mirrored(true).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 7.0f, 4.0f).mirrored(false), ModelTransform.of(0.0f, 8.0f, 0.0f, 0.95f, 0.0f, 0.0f));
        ModelPartData p_l_foot = p_l_shin.addChild("l_foot", ModelPartBuilder.create().uv(68, 37).mirrored(true).cuboid(-2.0f, 0.0f, -5.0f, 4.0f, 2.0f, 6.0f).mirrored(false).uv(88, 37).mirrored(true).cuboid(-2.0f, 0.0f, -7.0f, 4.0f, 1.0f, 2.0f).mirrored(false), ModelTransform.of(0.0f, 7.0f, 0.0f, -0.45f, 0.0f, 0.0f));
        ModelPartData p_tail = p_hips.addChild("tail", ModelPartBuilder.create().uv(0, 50).cuboid(-2.0f, -1.5f, 0.0f, 4.0f, 4.0f, 11.0f), ModelTransform.of(0.0f, -0.5f, 4.0f, -0.35f, 0.0f, 0.0f));
        return TexturedModelData.of(md, TEX_W, TEX_H);
    }
}
