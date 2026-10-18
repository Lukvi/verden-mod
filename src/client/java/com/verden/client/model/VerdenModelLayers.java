package com.verden.client.model;

import com.verden.VerdenMod;
import net.minecraft.client.render.entity.model.EntityModelLayer;

/** Camadas de modelo próprias do Verden. */
public final class VerdenModelLayers {
    private VerdenModelLayers() {}

    public static final EntityModelLayer LICANTROPO = layer("licantropo");
    public static final EntityModelLayer AMALGAMA = layer("amalgama_koold");
    public static final EntityModelLayer RINOY = layer("rinoy");
    public static final EntityModelLayer CONDOLYUS = layer("condolyus");
    public static final EntityModelLayer TENTACULO = layer("tentaculo");
    public static final EntityModelLayer KUNORI = layer("encapuzado_kunori");
    public static final EntityModelLayer LEGIAO = layer("soldado_legiao");
    public static final EntityModelLayer ZYTRA = layer("zytra_twin");
    public static final EntityModelLayer HAIGOL = layer("haigol_twin");

    private static EntityModelLayer layer(String name) {
        return new EntityModelLayer(VerdenMod.id(name), "main");
    }
}
