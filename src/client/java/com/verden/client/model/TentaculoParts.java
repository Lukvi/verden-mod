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

public final class TentaculoParts {
    public static final int TEX_W = 64;
    public static final int TEX_H = 64;
    public static final float HEIGHT_PX = 30.0f;
    private static final Map<String, String> PATHS = new HashMap<>();
    private TentaculoParts() {}

    static {
        PATHS.put("s0", "s0");
        PATHS.put("s1", "s0/s1");
        PATHS.put("s2", "s0/s1/s2");
        PATHS.put("s3", "s0/s1/s2/s3");
        PATHS.put("s4", "s0/s1/s2/s3/s4");
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
        ModelPartData p_s0 = root.addChild("s0", ModelPartBuilder.create().uv(0, 0).cuboid(-3.0f, -6.0f, -3.0f, 6.0f, 6.0f, 6.0f), ModelTransform.of(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_s1 = p_s0.addChild("s1", ModelPartBuilder.create().uv(24, 0).cuboid(-2.5f, -6.0f, -2.5f, 5.0f, 6.0f, 5.0f), ModelTransform.of(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_s2 = p_s1.addChild("s2", ModelPartBuilder.create().uv(44, 0).cuboid(-2.0f, -6.0f, -2.0f, 4.0f, 6.0f, 4.0f), ModelTransform.of(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_s3 = p_s2.addChild("s3", ModelPartBuilder.create().uv(0, 12).cuboid(-2.0f, -6.0f, -2.0f, 4.0f, 6.0f, 4.0f), ModelTransform.of(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        ModelPartData p_s4 = p_s3.addChild("s4", ModelPartBuilder.create().uv(16, 12).cuboid(-1.5f, -6.0f, -1.5f, 3.0f, 6.0f, 3.0f), ModelTransform.of(0.0f, -6.0f, 0.0f, 0.0f, 0.0f, 0.0f));
        return TexturedModelData.of(md, TEX_W, TEX_H);
    }
}
