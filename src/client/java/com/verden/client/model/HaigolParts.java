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

public final class HaigolParts {
    public static final int TEX_W = 128;
    public static final int TEX_H = 128;
    public static final float HEIGHT_PX = 36.0f;
    private static final Map<String, String> PATHS = new HashMap<>();
    private HaigolParts() {}

    static {
        PATHS.put("head", "head");
        PATHS.put("body", "body");
        PATHS.put("r_arm", "r_arm");
        PATHS.put("l_arm", "l_arm");
        PATHS.put("r_leg", "r_leg");
        PATHS.put("l_leg", "l_leg");
        PATHS.put("cape", "body/cape");
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
        ModelPartData p_head = root.addChild("head", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f).uv(32, 0).cuboid(-5.0f, -9.0f, -5.0f, 10.0f, 9.0f, 10.0f).uv(72, 0).cuboid(-1.0f, -12.0f, -5.0f, 2.0f, 3.0f, 10.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_body = root.addChild("body", ModelPartBuilder.create().uv(96, 0).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f).uv(0, 19).cuboid(-5.0f, 0.0f, -3.0f, 10.0f, 12.0f, 6.0f), ModelTransform.of(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_r_arm = root.addChild("r_arm", ModelPartBuilder.create().uv(32, 19).cuboid(-3.0f, -2.0f, -2.0f, 4.0f, 5.0f, 4.0f).uv(48, 19).cuboid(-3.5f, 3.0f, -2.5f, 5.0f, 5.0f, 5.0f).uv(68, 19).cuboid(-3.5f, 8.0f, -2.5f, 5.0f, 2.0f, 5.0f).uv(88, 19).cuboid(-5.0f, -4.0f, -4.0f, 8.0f, 5.0f, 8.0f).uv(120, 19).cuboid(-3.0f, -8.0f, -1.0f, 2.0f, 4.0f, 2.0f), ModelTransform.of(-5.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_l_arm = root.addChild("l_arm", ModelPartBuilder.create().uv(32, 19).mirrored(true).cuboid(-1.0f, -2.0f, -2.0f, 4.0f, 5.0f, 4.0f).mirrored(false).uv(48, 19).mirrored(true).cuboid(-1.5f, 3.0f, -2.5f, 5.0f, 5.0f, 5.0f).mirrored(false).uv(68, 19).mirrored(true).cuboid(-1.5f, 8.0f, -2.5f, 5.0f, 2.0f, 5.0f).mirrored(false).uv(88, 19).mirrored(true).cuboid(-3.0f, -4.0f, -4.0f, 8.0f, 5.0f, 8.0f).mirrored(false).uv(120, 19).mirrored(true).cuboid(1.0f, -8.0f, -1.0f, 2.0f, 4.0f, 2.0f).mirrored(false), ModelTransform.of(5.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_r_leg = root.addChild("r_leg", ModelPartBuilder.create().uv(0, 37).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f).uv(16, 37).cuboid(-2.5f, 3.0f, -2.5f, 5.0f, 9.0f, 5.0f), ModelTransform.of(-1.9f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_l_leg = root.addChild("l_leg", ModelPartBuilder.create().uv(0, 37).mirrored(true).cuboid(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f).mirrored(false).uv(16, 37).mirrored(true).cuboid(-2.5f, 3.0f, -2.5f, 5.0f, 9.0f, 5.0f).mirrored(false), ModelTransform.of(1.9f, 12.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_cape = p_body.addChild("cape", ModelPartBuilder.create().uv(36, 37).cuboid(-5.0f, 0.0f, 0.0f, 10.0f, 20.0f, 1.0f), ModelTransform.of(0.0f, 0.0f, 2.5f, 0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(md, TEX_W, TEX_H);
    }
}
