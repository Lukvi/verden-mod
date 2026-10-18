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

public final class CondolyusParts {
    public static final int TEX_W = 64;
    public static final int TEX_H = 64;
    public static final float HEIGHT_PX = 15.57235632567597f;
    private static final Map<String, String> PATHS = new HashMap<>();
    private CondolyusParts() {}

    static {
        PATHS.put("body", "body");
        PATHS.put("head", "body/head");
        PATHS.put("fr", "body/fr");
        PATHS.put("fl", "body/fl");
        PATHS.put("br", "body/br");
        PATHS.put("bl", "body/bl");
        PATHS.put("tail1", "body/tail1");
        PATHS.put("tail2", "body/tail1/tail2");
        PATHS.put("tail3", "body/tail1/tail2/tail3");
        PATHS.put("plate0", "body/plate0");
        PATHS.put("plate1", "body/plate1");
        PATHS.put("plate2", "body/plate2");
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
        ModelPartData p_body = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -3.0f, -5.0f, 6.0f, 5.0f, 10.0f), ModelTransform.of(0.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_head = p_body.addChild("head", ModelPartBuilder.create().uv(32, 0).cuboid(-2.5f, -2.5f, -5.0f, 5.0f, 5.0f, 5.0f).uv(52, 0).cuboid(-1.5f, 0.0f, -8.0f, 3.0f, 2.0f, 3.0f).uv(0, 15).cuboid(-3.5f, -5.5f, -1.0f, 1.0f, 3.0f, 2.0f).uv(0, 15).mirrored(true).cuboid(2.5f, -5.5f, -1.0f, 1.0f, 3.0f, 2.0f).mirrored(false), ModelTransform.of(0.0f, -1.0f, -5.0f, 0.1f, 0.0f, 0.0f));
        ModelPartData p_fr = p_body.addChild("fr", ModelPartBuilder.create().uv(6, 15).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(-2.0f, 2.0f, -3.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_fl = p_body.addChild("fl", ModelPartBuilder.create().uv(6, 15).mirrored(true).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f).mirrored(false), ModelTransform.of(2.0f, 2.0f, -3.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_br = p_body.addChild("br", ModelPartBuilder.create().uv(14, 15).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f), ModelTransform.of(-2.0f, 2.0f, 3.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_bl = p_body.addChild("bl", ModelPartBuilder.create().uv(14, 15).mirrored(true).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 7.0f, 2.0f).mirrored(false), ModelTransform.of(2.0f, 2.0f, 3.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_tail1 = p_body.addChild("tail1", ModelPartBuilder.create().uv(22, 15).cuboid(-1.5f, -1.5f, 0.0f, 3.0f, 3.0f, 5.0f), ModelTransform.of(0.0f, -1.0f, 5.0f, -0.2f, 0.0f, 0.0f));
        ModelPartData p_tail2 = p_tail1.addChild("tail2", ModelPartBuilder.create().uv(38, 15).cuboid(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 5.0f), ModelTransform.of(0.0f, 0.0f, 5.0f, -0.2f, 0.0f, 0.0f));
        ModelPartData p_tail3 = p_tail2.addChild("tail3", ModelPartBuilder.create().uv(52, 15).cuboid(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 2.0f), ModelTransform.of(0.0f, 0.0f, 5.0f, -0.2f, 0.0f, 0.0f));
        ModelPartData p_plate0 = p_body.addChild("plate0", ModelPartBuilder.create().uv(0, 24).cuboid(-0.5f, -3.0f, -1.0f, 1.0f, 3.0f, 2.0f), ModelTransform.of(0.0f, -3.0f, -3.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_plate1 = p_body.addChild("plate1", ModelPartBuilder.create().uv(6, 24).cuboid(-0.5f, -3.0f, -1.0f, 1.0f, 3.0f, 2.0f), ModelTransform.of(0.0f, -3.0f, -0.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_plate2 = p_body.addChild("plate2", ModelPartBuilder.create().uv(12, 24).cuboid(-0.5f, -3.0f, -1.0f, 1.0f, 3.0f, 2.0f), ModelTransform.of(0.0f, -3.0f, 2.5f, 0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(md, TEX_W, TEX_H);
    }
}
