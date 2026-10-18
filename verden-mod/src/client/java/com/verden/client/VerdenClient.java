package com.verden.client;

import com.verden.VerdenMod;
import com.verden.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class VerdenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.LICANTROPO, ctx ->
                new VerdenBipedRenderer<>(ctx, VerdenMod.id("textures/entity/licantropo.png"), 1.05f, 0.5f));
        EntityRendererRegistry.register(ModEntities.KUNORI, ctx ->
                new VerdenBipedRenderer<>(ctx, VerdenMod.id("textures/entity/encapuzado_kunori.png"), 1.0f, 0.5f));
        EntityRendererRegistry.register(ModEntities.AMALGAMA, ctx ->
                new VerdenBipedRenderer<>(ctx, VerdenMod.id("textures/entity/amalgama_koold.png"), 3.0f, 0.5f));
        EntityRendererRegistry.register(ModEntities.LICANTROPO_ADAPTADO, ctx ->
                new VerdenBipedRenderer<>(ctx, VerdenMod.id("textures/entity/licantropo_adaptado.png"), 1.15f, 0.5f));
        EntityRendererRegistry.register(ModEntities.SOLDADO_LEGIAO, ctx ->
                new VerdenBipedRenderer<>(ctx, VerdenMod.id("textures/entity/soldado_legiao.png"), 1.0f, 0.5f));
        EntityRendererRegistry.register(ModEntities.ZYTRA, ctx ->
                new VerdenBipedRenderer<>(ctx, VerdenMod.id("textures/entity/zytra_twin.png"), 1.0f, 0.5f));
        EntityRendererRegistry.register(ModEntities.HAIGOL, ctx ->
                new VerdenBipedRenderer<>(ctx, VerdenMod.id("textures/entity/haigol_twin.png"), 1.2f, 0.5f));
        EntityRendererRegistry.register(ModEntities.CONDOLYUS, CondolyusRenderer::new);
        EntityRendererRegistry.register(ModEntities.TENTACULO, TentaculoRenderer::new);
        EntityRendererRegistry.register(ModEntities.RINOY, RinoyRenderer::new);
    }
}
