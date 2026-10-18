package com.verden.client;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** Renderizador das criaturas com modelo próprio: textura, escala e camada emissiva (brilha no escuro). */
public class VerdenModelRenderer<T extends MobEntity, M extends EntityModel<T>> extends MobEntityRenderer<T, M> {
    private final Identifier texture;
    private final float scale;

    public VerdenModelRenderer(EntityRendererFactory.Context ctx, M model, float shadow, Identifier texture,
                               @Nullable Identifier glowTexture, float scale) {
        super(ctx, model, shadow * scale);
        this.texture = texture;
        this.scale = scale;
        if (glowTexture != null) {
            this.addFeature(new GlowFeature<T, M>(this, glowTexture));
        }
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture;
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        matrices.scale(scale, scale, scale);
    }

    /** Camada que desenha a textura "_glow" com brilho total, independente da luz do ambiente. */
    public static class GlowFeature<T extends Entity, M extends EntityModel<T>> extends EyesFeatureRenderer<T, M> {
        private final RenderLayer layer;

        public GlowFeature(FeatureRendererContext<T, M> context, Identifier glowTexture) {
            super(context);
            this.layer = RenderLayer.getEyes(glowTexture);
        }

        @Override
        public RenderLayer getEyesTexture() {
            return layer;
        }
    }
}
