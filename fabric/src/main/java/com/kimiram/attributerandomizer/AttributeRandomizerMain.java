package com.kimiram.attributerandomizer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

import static com.kimiram.attributerandomizer.Constants.MOD_ID;
import static com.kimiram.attributerandomizer.ModCommands.modVersion;

public class AttributeRandomizerMain implements ModInitializer {
    @Override
    public void onInitialize() {
        modVersion = FabricLoader.getInstance().getModContainer(MOD_ID)
                .get().getMetadata().getVersion().getFriendlyString();

        Config.file = FabricLoader.getInstance().getConfigDir().resolve("attribute-randomizer.json");

        Config.readConfig();

        if (AttributeRandomizer.isEnabled) {
            AttributeRandomizer.updateDoChange();
        }

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> Config.writeConfig());

        CommandRegistrationCallback.EVENT.register(((dispatcher, context, selection) -> ModCommands.registerCommands(dispatcher)));

        ServerTickEvents.END_SERVER_TICK.register(AttributeRandomizer::onTick);

        ServerLivingEntityEvents.AFTER_DEATH.register(AttributeRandomizer::onDeath);
    }
}
