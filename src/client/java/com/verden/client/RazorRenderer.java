package com.verden.client;

import com.verden.VerdenMod;
import com.verden.entity.RazorKitsujiEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Razor Kitsuji: fase 1 elegante (terno e cartola), fase 2 com o Vestígio exposto no peito. */
public class RazorRenderer extends BipedEntityRenderer<RazorKitsujiEntity, PlayerEntityModel<RazorKitsujiEntity>> {
    public RazorRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(RazorKitsujiEntity entity) {
        return VerdenMod.id("textures/entity/razor_kitsuji" + (entity.getPhase() == 2 ? "_2" : "") + ".png");
    }

    @Override
    protected void scale(RazorKitsujiEntity entity, MatrixStack matrices, float amount) {
        float s = entity.getPhase() == 2 ? 1.2f : 1.1f;
        matrices.scale(s, s, s);
    }
}
