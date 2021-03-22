package dev.ftb.mods.ftbteamislands;

import com.feed_the_beast.mods.ftbteams.event.PlayerChangedTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import dev.ftb.mods.ftbteamislands.commands.*;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod(FTBTeamIslands.MOD_ID)
public class FTBTeamIslands {
    public static final String MOD_ID = "ftbteamislands";
    public static final Logger LOGGER = LogManager.getLogger("FTB Team Islands");

    public FTBTeamIslands() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::enqueueIMC);
        eventBus.addListener(this::processIMC);

        MinecraftForge.EVENT_BUS.register(this);

        PlayerChangedTeamEvent.EVENT.register(Events::onChangedTeamEvent);
        TeamDeletedEvent.EVENT.register(Events::onTeamDeleted);
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
//        LOGGER.info("Got IMC {}", event.getIMCStream()
//            .map(m -> m.getMessageSupplier().get())
//            .collect(Collectors.toList()));
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
