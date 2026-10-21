package com.verden;

import com.verden.event.ModEvents;
import com.verden.registry.ModArmor;
import com.verden.registry.ModEntities;
import com.verden.registry.ModSounds;
import com.verden.world.ModWorldgen;
import com.verden.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerdenMod implements ModInitializer {
    public static final String MOD_ID = "verden";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModSounds.register();
        ModArmor.register();
        ModEntities.register();
        ModItems.register();
        ModEvents.register();
        ModWorldgen.register();
        com.verden.world.Population.register();
        com.verden.event.RazorEvents.register();
        LOGGER.info("Verden carregado. Que os ventos não soprem contra você.");
    }
}
