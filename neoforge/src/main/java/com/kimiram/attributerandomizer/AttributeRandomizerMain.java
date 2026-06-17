package com.kimiram.attributerandomizer;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import static com.kimiram.attributerandomizer.Constants.MOD_ID;
import static com.kimiram.attributerandomizer.ModCommands.modVersion;

@Mod(value = MOD_ID)
@EventBusSubscriber(modid = MOD_ID)
public class AttributeRandomizerMain {
    @SubscribeEvent
    public static void init(FMLDedicatedServerSetupEvent event) {
        modVersion = ModList.get().getModContainerById(MOD_ID).get().getModInfo().getVersion().toString();

        Config.file = FMLPaths.GAMEDIR.get().resolve("config").resolve("attribute-randomizer.json");

        Config.readConfig();

        if (AttributeRandomizer.isEnabled) {
            AttributeRandomizer.updateDoChange();
        }
    }

    @SubscribeEvent
    public static void onStop(GameShuttingDownEvent event) {
        Config.writeConfig();
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        AttributeRandomizer.onTick(event.getServer());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        AttributeRandomizer.onDeath(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ModCommands.registerCommands(event.getDispatcher());
    }
}
