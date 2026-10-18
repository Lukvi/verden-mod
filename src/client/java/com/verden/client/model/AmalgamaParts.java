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

public final class AmalgamaParts {
    public static final int TEX_W = 128;
    public static final int TEX_H = 128;
    public static final float HEIGHT_PX = 42.76175442215465f;
    private static final Map<String, String> PATHS = new HashMap<>();
    private AmalgamaParts() {}

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
        PATHS.put("core", "hips/body/core");
        PATHS.put("bt0a", "hips/body/bt0a");
        PATHS.put("bt0b", "hips/body/bt0a/bt0b");
        PATHS.put("bt0c", "hips/body/bt0a/bt0b/bt0c");
        PATHS.put("bt1a", "hips/body/bt1a");
        PATHS.put("bt1b", "hips/body/bt1a/bt1b");
        PATHS.put("bt1c", "hips/body/bt1a/bt1b/bt1c");
        PATHS.put("bt2a", "hips/body/bt2a");
        PATHS.put("bt2b", "hips/body/bt2a/bt2b");
        PATHS.put("bt2c", "hips/body/bt2a/bt2b/bt2c");
        PATHS.put("bt3a", "hips/body/bt3a");
        PATHS.put("bt3b", "hips/body/bt3a/bt3b");
        PATHS.put("bt3c", "hips/body/bt3a/bt3b/bt3c");
        PATHS.put("jt0a", "hips/body/head/jt0a");
        PATHS.put("jt0b", "hips/body/head/jt0a/jt0b");
        PATHS.put("jt1a", "hips/body/head/jt1a");
        PATHS.put("jt1b", "hips/body/head/jt1a/jt1b");
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
        ModelPartData p_core = p_body.addChild("core", ModelPartBuilder.create().uv(30, 50).cuboid(-3.0f, -3.0f, -1.5f, 6.0f, 6.0f, 2.0f), ModelTransform.of(0.0f, -9.0f, -5.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_bt0a = p_body.addChild("bt0a", ModelPartBuilder.create().uv(46, 50).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 9.0f, 3.0f), ModelTransform.of(-4.0f, -13.0f, 5.0f, 1.2f, 0.0f, 0.25f));
        ModelPartData p_bt0b = p_bt0a.addChild("bt0b", ModelPartBuilder.create().uv(58, 50).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.of(0.0f, 9.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_bt0c = p_bt0b.addChild("bt0c", ModelPartBuilder.create().uv(66, 50).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(0.0f, 8.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_bt1a = p_body.addChild("bt1a", ModelPartBuilder.create().uv(74, 50).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 9.0f, 3.0f), ModelTransform.of(4.0f, -13.0f, 5.0f, 1.2f, 0.0f, -0.25f));
        ModelPartData p_bt1b = p_bt1a.addChild("bt1b", ModelPartBuilder.create().uv(86, 50).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.of(0.0f, 9.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_bt1c = p_bt1b.addChild("bt1c", ModelPartBuilder.create().uv(94, 50).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(0.0f, 8.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_bt2a = p_body.addChild("bt2a", ModelPartBuilder.create().uv(102, 50).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 9.0f, 3.0f), ModelTransform.of(-8.0f, -9.0f, 4.0f, 1.2f, 0.0f, 0.25f));
        ModelPartData p_bt2b = p_bt2a.addChild("bt2b", ModelPartBuilder.create().uv(114, 50).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.of(0.0f, 9.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_bt2c = p_bt2b.addChild("bt2c", ModelPartBuilder.create().uv(0, 65).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(0.0f, 8.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_bt3a = p_body.addChild("bt3a", ModelPartBuilder.create().uv(8, 65).cuboid(-1.5f, 0.0f, -1.5f, 3.0f, 9.0f, 3.0f), ModelTransform.of(8.0f, -9.0f, 4.0f, 1.2f, 0.0f, -0.25f));
        ModelPartData p_bt3b = p_bt3a.addChild("bt3b", ModelPartBuilder.create().uv(20, 65).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 8.0f, 2.0f), ModelTransform.of(0.0f, 9.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_bt3c = p_bt3b.addChild("bt3c", ModelPartBuilder.create().uv(28, 65).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(0.0f, 8.0f, 0.0f, 0.35f, 0.0f, 0.0f));
        ModelPartData p_jt0a = p_head.addChild("jt0a", ModelPartBuilder.create().uv(36, 65).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 6.0f, 2.0f), ModelTransform.of(-2.0f, 1.0f, -10.0f, 0.1f, 0.0f, 0.0f));
        ModelPartData p_jt0b = p_jt0a.addChild("jt0b", ModelPartBuilder.create().uv(44, 65).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f), ModelTransform.of(0.0f, 6.0f, 0.0f, 0.2f, 0.0f, 0.0f));
        ModelPartData p_jt1a = p_head.addChild("jt1a", ModelPartBuilder.create().uv(52, 65).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 6.0f, 2.0f), ModelTransform.of(2.0f, 1.0f, -10.0f, 0.1f, 0.0f, 0.0f));
        ModelPartData p_jt1b = p_jt1a.addChild("jt1b", ModelPartBuilder.create().uv(60, 65).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 5.0f, 2.0f), ModelTransform.of(0.0f, 6.0f, 0.0f, 0.2f, 0.0f, 0.0f));
        return TexturedModelData.of(md, TEX_W, TEX_H);
    }
}
