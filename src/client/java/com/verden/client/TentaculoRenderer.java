package com.verden.client;

import com.verden.VerdenMod;
import com.verden.entity.TentaculoEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SilverfishEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class TentaculoRenderer extends MobEntityRenderer<TentaculoEntity, SilverfishEntityModel<TentaculoEntity>> {
    private static final Identifier TEXTURE = VerdenMod.id("textures/entity/tentaculo.png");

    public TentaculoRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new SilverfishEntityModel<>(ctx.getPart(EntityModelLayers.SILVERFISH)), 0.6f);
    }

    @Override
    public Identifier getTexture(TentaculoEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(TentaculoEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(2.5f, 2.5f, 2.5f);
    }
}
