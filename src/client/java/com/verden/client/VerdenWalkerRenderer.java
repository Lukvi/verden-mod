package com.verden.client;

import com.verden.VerdenMod;
import com.verden.entity.VerdenWalkerEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/** Viajantes de Verden: modelo de jogador, uma textura por tipo, com armaduras visíveis. */
public class VerdenWalkerRenderer extends BipedEntityRenderer<VerdenWalkerEntity, PlayerEntityModel<VerdenWalkerEntity>> {
    public VerdenWalkerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.addFeature(new ArmorFeatureRenderer<>(this,
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR)),
                new BipedEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR)),
                ctx.getModelManager()));
    }

    @Override
    public Identifier getTexture(VerdenWalkerEntity entity) {
        return VerdenMod.id("textures/entity/walker/" + entity.getKind().id + ".png");
    }
}
