package com.verden.client;

import com.verden.VerdenMod;
import com.verden.entity.CondolyusEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

/** Condolyus usa o modelo quadrúpede vanilla em escala pequena com textura de escamas incandescentes. */
public class CondolyusRenderer extends MobEntityRenderer<CondolyusEntity, PigEntityModel<CondolyusEntity>> {
    private static final Identifier TEXTURE = VerdenMod.id("textures/entity/condolyus.png");

    public CondolyusRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PigEntityModel<>(ctx.getPart(EntityModelLayers.PIG)), 0.3f);
    }

    @Override
    public Identifier getTexture(CondolyusEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CondolyusEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(0.5f, 0.5f, 0.5f);
    }
}
