package com.feed_the_beast.mods.teamislands;

import com.feed_the_beast.mods.ftbteams.event.PlayerChangedTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamCreatedEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import com.feed_the_beast.mods.teamislands.commands.*;
import com.feed_the_beast.mods.teamislands.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.stream.Collectors;

@Mod(TeamIslands.MOD_ID)
public class TeamIslands {
    public static final String MOD_ID = "teamislands";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final DeferredRegister<ForgeWorldType> WORLD_TYPES = DeferredRegister.create(ForgeRegistries.WORLD_TYPES, MOD_ID);
    public static final RegistryObject<ForgeWorldType> VOID_WORLD_TYPE = WORLD_TYPES.register("void", () ->
        new ForgeWorldType((biomeRegistry, dimensionSettingsRegistry, seed) -> new VoidChunkGenerator(biomeRegistry))
    );

    public TeamIslands() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::enqueueIMC);
        eventBus.addListener(this::processIMC);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        WORLD_TYPES.register(eventBus);

        TeamCreatedEvent.EVENT.register(Events::onTeamCreated);
        TeamDeletedEvent.EVENT.register(Events::onTeamDeleted);
        PlayerChangedTeamEvent.EVENT.register(Events::onChangedTeamEvent);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_CONFIG);

        NetworkManager.register();
    }

    private void clientSetup(final FMLClientSetupEvent event) {

    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream()
            .map(m -> m.getMessageSupplier().get())
            .collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        event.getServer().getCommands().getDispatcher().register(
            Commands.literal(MOD_ID)
                .then(JumpToIslandCommand.register())
                .then(ListIslandsCommand.register())
                .then(LobbyCommand.register())
                .then(MyIslandCommand.register())
                .then(DeleteUnusedIslandsCommand.register())
        );
    }

    // TODO: Remove, helps with breakpoints when Intellij and Linux aren't getting along
    public static boolean debuggerReleaseControl() {
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        return true;
    }
}
