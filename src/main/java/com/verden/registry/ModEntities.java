package com.verden.registry;

import com.verden.VerdenMod;
import com.verden.entity.AmalgamaKooldEntity;
import com.verden.entity.CondolyusEntity;
import com.verden.entity.HaigolEntity;
import com.verden.entity.KunoriEntity;
import com.verden.entity.LicantropoAdaptadoEntity;
import com.verden.entity.LicantropoEntity;
import com.verden.entity.RinoyEntity;
import com.verden.entity.SoldadoLegiaoEntity;
import com.verden.entity.TentaculoEntity;
import com.verden.entity.VerdenNpcEntity;
import com.verden.entity.ZytraEntity;
import net.minecraft.entity.mob.MobEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<LicantropoEntity> LICANTROPO = reg("licantropo",
            EntityType.Builder.create(LicantropoEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));

    public static final EntityType<KunoriEntity> KUNORI = reg("encapuzado_kunori",
            EntityType.Builder.create(KunoriEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));

    public static final EntityType<TentaculoEntity> TENTACULO = reg("tentaculo",
            EntityType.Builder.create(TentaculoEntity::new, SpawnGroup.MONSTER).dimensions(0.8f, 1.8f).maxTrackingRange(8));

    public static final EntityType<AmalgamaKooldEntity> AMALGAMA = reg("amalgama_koold",
            EntityType.Builder.create(AmalgamaKooldEntity::new, SpawnGroup.MONSTER).dimensions(3.6f, 9.5f).maxTrackingRange(24));

    public static final EntityType<RinoyEntity> RINOY = reg("rinoy",
            EntityType.Builder.create(RinoyEntity::new, SpawnGroup.CREATURE).dimensions(1.9f, 2.6f).maxTrackingRange(10));

    public static final EntityType<LicantropoAdaptadoEntity> LICANTROPO_ADAPTADO = reg("licantropo_adaptado",
            EntityType.Builder.create(LicantropoAdaptadoEntity::new, SpawnGroup.MONSTER).dimensions(0.7f, 2.1f).maxTrackingRange(8));

    public static final EntityType<SoldadoLegiaoEntity> SOLDADO_LEGIAO = reg("soldado_legiao",
            EntityType.Builder.create(SoldadoLegiaoEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(8));

    public static final EntityType<ZytraEntity> ZYTRA = reg("zytra_twin",
            EntityType.Builder.create(ZytraEntity::new, SpawnGroup.MONSTER).dimensions(0.6f, 1.95f).maxTrackingRange(12));

    public static final EntityType<HaigolEntity> HAIGOL = reg("haigol_twin",
            EntityType.Builder.create(HaigolEntity::new, SpawnGroup.MONSTER).dimensions(0.72f, 2.34f).maxTrackingRange(12));

    public static final EntityType<CondolyusEntity> CONDOLYUS = reg("condolyus",
            EntityType.Builder.create(CondolyusEntity::new, SpawnGroup.CREATURE).dimensions(0.6f, 0.8f).maxTrackingRange(8));

    public static final EntityType<VerdenNpcEntity> NPC = reg("npc",
            EntityType.Builder.create(VerdenNpcEntity::new, SpawnGroup.MISC).dimensions(0.6f, 1.8f).maxTrackingRange(10));

    private static <T extends Entity> EntityType<T> reg(String name, EntityType.Builder<T> builder) {
        return Registry.register(Registries.ENTITY_TYPE, VerdenMod.id(name), builder.build(name));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(LICANTROPO, LicantropoEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(KUNORI, KunoriEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(TENTACULO, TentaculoEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(AMALGAMA, AmalgamaKooldEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(RINOY, RinoyEntity.createAttributes());

        FabricDefaultAttributeRegistry.register(LICANTROPO_ADAPTADO, LicantropoAdaptadoEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SOLDADO_LEGIAO, SoldadoLegiaoEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ZYTRA, ZytraEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HAIGOL, HaigolEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(CONDOLYUS, CondolyusEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(NPC, VerdenNpcEntity.createAttributes());

        SpawnRestriction.register(LICANTROPO_ADAPTADO, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, LICANTROPO_ADAPTADO, 2, 1, 2);

        SpawnRestriction.register(CONDOLYUS, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canMobSpawn);
        BiomeModifications.addSpawn(BiomeSelectors.foundInTheNether(), SpawnGroup.CREATURE, CONDOLYUS, 4, 1, 2);

        SpawnRestriction.register(LICANTROPO, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        SpawnRestriction.register(KUNORI, SpawnLocationTypes.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);

        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, LICANTROPO, 6, 1, 3);
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, KUNORI, 3, 1, 2);
    }
}
