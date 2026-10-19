package com.verden.client;

import com.verden.VerdenMod;
import com.verden.client.model.AmalgamaParts;
import com.verden.client.model.CondolyusModel;
import com.verden.client.model.HaigolParts;
import com.verden.client.model.HumanoidModel;
import com.verden.client.model.KunoriParts;
import com.verden.client.model.LegiaoParts;
import com.verden.client.model.ZytraParts;
import com.verden.client.model.CondolyusParts;
import com.verden.client.model.LicantropoModel;
import com.verden.client.model.LicantropoParts;
import com.verden.client.model.RinoyModel;
import com.verden.client.model.RinoyParts;
import com.verden.client.model.TentaculoModel;
import com.verden.client.model.TentaculoParts;
import com.verden.client.model.VerdenModelLayers;
import com.verden.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.EntityType;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.mob.MobEntity;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public class VerdenClient implements ClientModInitializer {
    private static Identifier tex(String name) {
        return VerdenMod.id("textures/entity/" + name + ".png");
    }

    /** Escala para que o modelo (em pixels) tenha a altura da caixa de colisão da entidade. */
    private static float fit(EntityType<?> type, float heightPx) {
        return type.getHeight() * 16f / heightPx;
    }

    @Override
    public void onInitializeClient() {
        VerdenHud.register();
        EntityRendererRegistry.register(ModEntities.NPC, VerdenNpcRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.LICANTROPO, LicantropoParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.AMALGAMA, AmalgamaParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.RINOY, RinoyParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.CONDOLYUS, CondolyusParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.TENTACULO, TentaculoParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.KUNORI, KunoriParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.LEGIAO, LegiaoParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.ZYTRA, ZytraParts::create);
        EntityModelLayerRegistry.registerModelLayer(VerdenModelLayers.HAIGOL, HaigolParts::create);

        // Criaturas com modelo próprio
        EntityRendererRegistry.register(ModEntities.LICANTROPO, ctx -> lycan(ctx, ModEntities.LICANTROPO, "licantropo"));
        EntityRendererRegistry.register(ModEntities.LICANTROPO_ADAPTADO, ctx -> lycan(ctx, ModEntities.LICANTROPO_ADAPTADO, "licantropo_adaptado"));
        EntityRendererRegistry.register(ModEntities.AMALGAMA, ctx -> boss(ctx, ModEntities.AMALGAMA));
        EntityRendererRegistry.register(ModEntities.RINOY, ctx -> rinoy(ctx, ModEntities.RINOY));
        EntityRendererRegistry.register(ModEntities.CONDOLYUS, ctx -> condolyus(ctx, ModEntities.CONDOLYUS));
        EntityRendererRegistry.register(ModEntities.TENTACULO, ctx -> tentaculo(ctx, ModEntities.TENTACULO));

        // Humanoides
        EntityRendererRegistry.register(ModEntities.KUNORI, ctx -> human(ctx, ModEntities.KUNORI, VerdenModelLayers.KUNORI,
                KunoriParts::find, KunoriParts::has, KunoriParts.HEIGHT_PX, "encapuzado_kunori", true));
        EntityRendererRegistry.register(ModEntities.SOLDADO_LEGIAO, ctx -> human(ctx, ModEntities.SOLDADO_LEGIAO, VerdenModelLayers.LEGIAO,
                LegiaoParts::find, LegiaoParts::has, LegiaoParts.HEIGHT_PX, "soldado_legiao", false));
        EntityRendererRegistry.register(ModEntities.ZYTRA, ctx -> human(ctx, ModEntities.ZYTRA, VerdenModelLayers.ZYTRA,
                ZytraParts::find, ZytraParts::has, ZytraParts.HEIGHT_PX, "zytra_twin", true));
        EntityRendererRegistry.register(ModEntities.HAIGOL, ctx -> human(ctx, ModEntities.HAIGOL, VerdenModelLayers.HAIGOL,
                HaigolParts::find, HaigolParts::has, HaigolParts.HEIGHT_PX, "haigol_twin", true));
    }

    private static <T extends MobEntity> EntityRenderer<T> human(EntityRendererFactory.Context ctx, EntityType<T> type,
            EntityModelLayer layer, BiFunction<ModelPart, String, ModelPart> find, Predicate<String> has,
            float heightPx, String texture, boolean glow) {
        HumanoidModel<T> model = new HumanoidModel<T>(ctx.getPart(layer), find, has);
        return new VerdenHumanoidRenderer<T>(ctx, model, 0.5f, tex(texture), glow ? tex(texture + "_glow") : null,
                fit(type, heightPx));
    }

    private static <T extends MobEntity> EntityRenderer<T> lycan(EntityRendererFactory.Context ctx, EntityType<T> type, String texture) {
        LicantropoModel<T> model = new LicantropoModel<T>(ctx.getPart(VerdenModelLayers.LICANTROPO), LicantropoParts::find, false);
        return new VerdenModelRenderer<T, LicantropoModel<T>>(ctx, model, 0.6f, tex(texture), tex(texture + "_glow"),
                fit(type, LicantropoParts.HEIGHT_PX));
    }

    private static <T extends MobEntity> EntityRenderer<T> boss(EntityRendererFactory.Context ctx, EntityType<T> type) {
        LicantropoModel<T> model = new LicantropoModel<T>(ctx.getPart(VerdenModelLayers.AMALGAMA), AmalgamaParts::find, true);
        return new VerdenModelRenderer<T, LicantropoModel<T>>(ctx, model, 0.6f, tex("amalgama_koold"), tex("amalgama_koold_glow"),
                fit(type, AmalgamaParts.HEIGHT_PX));
    }

    private static <T extends MobEntity> EntityRenderer<T> rinoy(EntityRendererFactory.Context ctx, EntityType<T> type) {
        RinoyModel<T> model = new RinoyModel<T>(ctx.getPart(VerdenModelLayers.RINOY));
        return new VerdenModelRenderer<T, RinoyModel<T>>(ctx, model, 0.9f, tex("rinoy"), null,
                fit(type, RinoyParts.HEIGHT_PX));
    }

    private static <T extends MobEntity> EntityRenderer<T> condolyus(EntityRendererFactory.Context ctx, EntityType<T> type) {
        CondolyusModel<T> model = new CondolyusModel<T>(ctx.getPart(VerdenModelLayers.CONDOLYUS));
        return new VerdenModelRenderer<T, CondolyusModel<T>>(ctx, model, 0.4f, tex("condolyus"), tex("condolyus_glow"),
                fit(type, CondolyusParts.HEIGHT_PX));
    }

    private static <T extends MobEntity> EntityRenderer<T> tentaculo(EntityRendererFactory.Context ctx, EntityType<T> type) {
        TentaculoModel<T> model = new TentaculoModel<T>(ctx.getPart(VerdenModelLayers.TENTACULO));
        return new VerdenModelRenderer<T, TentaculoModel<T>>(ctx, model, 0.5f, tex("tentaculo"), null,
                fit(type, TentaculoParts.HEIGHT_PX));
    }
}
