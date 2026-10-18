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

public final class RinoyParts {
    public static final int TEX_W = 128;
    public static final int TEX_H = 128;
    public static final float HEIGHT_PX = 34.0f;
    private static final Map<String, String> PATHS = new HashMap<>();
    private RinoyParts() {}

    static {
        PATHS.put("body", "body");
        PATHS.put("hump", "body/hump");
        PATHS.put("head", "body/head");
        PATHS.put("fr", "body/fr");
        PATHS.put("fl", "body/fl");
        PATHS.put("br", "body/br");
        PATHS.put("bl", "body/bl");
        PATHS.put("tail", "body/tail");
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
        ModelPartData p_body = root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-8.0f, -8.0f, -13.0f, 16.0f, 16.0f, 26.0f), ModelTransform.of(0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_hump = p_body.addChild("hump", ModelPartBuilder.create().uv(84, 0).cuboid(-6.0f, -5.0f, -5.0f, 12.0f, 5.0f, 10.0f), ModelTransform.of(0.0f, -8.0f, -3.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_head = p_body.addChild("head", ModelPartBuilder.create().uv(0, 42).cuboid(-5.0f, -5.0f, -8.0f, 10.0f, 10.0f, 9.0f).uv(38, 42).cuboid(-4.0f, 0.0f, -16.0f, 8.0f, 5.0f, 8.0f).uv(70, 42).cuboid(-3.0f, 5.0f, -9.0f, 6.0f, 6.0f, 4.0f).uv(90, 42).cuboid(-1.5f, -5.0f, -14.0f, 3.0f, 5.0f, 3.0f).uv(102, 42).cuboid(-1.0f, -3.0f, -8.0f, 2.0f, 3.0f, 2.0f).uv(110, 42).cuboid(-8.0f, -6.0f, -2.0f, 3.0f, 4.0f, 1.0f).uv(110, 42).mirrored(true).cuboid(5.0f, -6.0f, -2.0f, 3.0f, 4.0f, 1.0f).mirrored(false), ModelTransform.of(0.0f, -3.0f, -12.0f, 0.3f, 0.0f, 0.0f));
        ModelPartData p_fr = p_body.addChild("fr", ModelPartBuilder.create().uv(0, 61).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 10.0f, 6.0f).uv(24, 61).cuboid(-3.0f, 10.0f, -3.0f, 6.0f, 3.0f, 6.0f), ModelTransform.of(-6.5f, 8.0f, -8.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_fl = p_body.addChild("fl", ModelPartBuilder.create().uv(0, 61).mirrored(true).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 10.0f, 6.0f).mirrored(false).uv(24, 61).mirrored(true).cuboid(-3.0f, 10.0f, -3.0f, 6.0f, 3.0f, 6.0f).mirrored(false), ModelTransform.of(6.5f, 8.0f, -8.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_br = p_body.addChild("br", ModelPartBuilder.create().uv(48, 61).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 10.0f, 6.0f).uv(72, 61).cuboid(-3.0f, 10.0f, -3.0f, 6.0f, 3.0f, 6.0f), ModelTransform.of(-6.5f, 8.0f, 8.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_bl = p_body.addChild("bl", ModelPartBuilder.create().uv(48, 61).mirrored(true).cuboid(-3.0f, 0.0f, -3.0f, 6.0f, 10.0f, 6.0f).mirrored(false).uv(72, 61).mirrored(true).cuboid(-3.0f, 10.0f, -3.0f, 6.0f, 3.0f, 6.0f).mirrored(false), ModelTransform.of(6.5f, 8.0f, 8.5f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_tail = p_body.addChild("tail", ModelPartBuilder.create().uv(96, 61).cuboid(-1.0f, 0.0f, -1.0f, 2.0f, 9.0f, 2.0f).uv(104, 61).cuboid(-1.5f, 8.0f, -1.5f, 3.0f, 3.0f, 3.0f), ModelTransform.of(0.0f, -4.0f, 13.0f, 0.5f, 0.0f, 0.0f));
        return TexturedModelData.of(md, TEX_W, TEX_H);
    }
}
