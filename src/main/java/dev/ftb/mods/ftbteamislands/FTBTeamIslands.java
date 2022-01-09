package dev.ftb.mods.ftbteamislands;

import dev.ftb.mods.ftbteamislands.commands.*;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import dev.ftb.mods.ftbteams.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.event.TeamEvent;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;

@Mod(FTBTeamIslands.MOD_ID)
public class FTBTeamIslands {
    public static final String MOD_ID = "ftbteamislands";
    public static final Logger LOGGER = LogManager.getLogger("FTB Team Islands");

    public FTBTeamIslands() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(this::setup);

        TeamEvent.DELETED.register(Events::onTeamDeleted);
        TeamEvent.CREATED.register(Events::onTeamCreated);
        PlayerChangedTeamEvent.PLAYER_CHANGED.register(Events::onChangedTeamEvent);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        IslandsManager.createEmptyJson();
        NetworkManager.register();
        IslandsManager.findAndLoadPrebuilts();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(MOD_ID)
                .then(JumpToIslandCommand.register())
                .then(ListIslandsCommand.register())
                .then(LobbyCommand.register())
                .then(HomeCommand.register())
                .then(DeleteUnusedIslandsCommand.register())
                .then(CreateIslandCommand.register())
                .then(ReloadIslandsJsonCommand.register())
                .then(ChangeIslandSpawnPoint.register())
        );

        // Simpler command
        event.getDispatcher().register(
                Commands.literal("myisland").executes(HomeCommand::execute)
        );
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        IslandsManager.setup(event.getServer());
    }

    /**
     * Attempts to delete the islands after the server has shut down
     */
    @SubscribeEvent
    public void onServerShutdown(ServerStoppedEvent event) {
        IslandsManager.get().getIslandsToDelete().forEach(island -> {
            try {
                Files.deleteIfExists(island);
                FTBTeamIslands.LOGGER.error("Deleted {}", island.toString());
            } catch (IOException e) {
                FTBTeamIslands.LOGGER.error("Failed to delete {}", island.toString());
            }
        });
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        IslandsManager.get().saveNow();
    }
}
