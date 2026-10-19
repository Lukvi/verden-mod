package com.verden.client;

import com.verden.VerdenMod;
import com.verden.entity.VerdenNpcEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

/** NPCs da história: modelo de jogador com uma textura por personagem. */
public class VerdenNpcRenderer extends BipedEntityRenderer<VerdenNpcEntity, PlayerEntityModel<VerdenNpcEntity>> {
    public VerdenNpcRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public Identifier getTexture(VerdenNpcEntity entity) {
        return VerdenMod.id("textures/entity/npc/" + entity.getRole().id + ".png");
    }
}
