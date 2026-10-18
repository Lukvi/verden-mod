package com.verden.client;

import com.verden.VerdenMod;
import com.verden.entity.RinoyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.HorseEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class RinoyRenderer extends MobEntityRenderer<RinoyEntity, HorseEntityModel<RinoyEntity>> {
    private static final Identifier TEXTURE = VerdenMod.id("textures/entity/rinoy.png");

    public RinoyRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new HorseEntityModel<>(ctx.getPart(EntityModelLayers.HORSE)), 0.9f);
    }

    @Override
    public Identifier getTexture(RinoyEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(RinoyEntity entity, MatrixStack matrices, float amount) {
        matrices.scale(1.6f, 1.6f, 1.6f);
    }
}
