package com.verden.client;

import com.verden.client.model.HumanoidModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** Renderizador dos humanoides do Verden: modelo próprio, brilho opcional e itens nas mãos. */
public class VerdenHumanoidRenderer<T extends MobEntity> extends MobEntityRenderer<T, HumanoidModel<T>> {
    private final Identifier texture;
    private final float scale;

    public VerdenHumanoidRenderer(EntityRendererFactory.Context ctx, HumanoidModel<T> model, float shadow,
                                  Identifier texture, @Nullable Identifier glowTexture, float scale) {
        super(ctx, model, shadow * scale);
        this.texture = texture;
        this.scale = scale;
        this.addFeature(new HeldItemFeatureRenderer<T, HumanoidModel<T>>(this, ctx.getHeldItemRenderer()));
        if (glowTexture != null) {
            this.addFeature(new VerdenModelRenderer.GlowFeature<T, HumanoidModel<T>>(this, glowTexture));
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
}
