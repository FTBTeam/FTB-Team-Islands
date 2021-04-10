package dev.ftb.mods.ftbteamislands;

import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteamislands.islands.PrebuiltIslands;
import dev.ftb.mods.ftbteamislands.network.IslandSelectionPacket;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import dev.ftb.mods.ftbteamislands.screens.IslandDirectoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = FTBTeamIslands.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    public static PrebuiltIslands.PrebuiltIsland selectedIsland;
    private static List<LevelSummary> levels;

    public static void openSelectionGui(List<PrebuiltIslands> islands) {
        Minecraft.getInstance().setScreen(new IslandDirectoryScreen(islands, island -> NetworkManager.sendToServer(new IslandSelectionPacket(island.getStructureFileLocation(), island.yOffset())), null));
    }

    @SubscribeEvent
    public static void screenEvent(GuiScreenEvent.InitGuiEvent.Post event) {
        if (IslandsManager.getAvailableIslands().size() == 0) {
            return;
        }

        if (event.getGui() instanceof TitleScreen) {
            replaceSingleplayerButton(event);
        }

        if (event.getGui() instanceof SelectWorldScreen) {
            replaceCreateWorldButton(event);
        }
    }

    @SubscribeEvent
    public static void screenRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getGui() instanceof CreateWorldScreen)) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        if (selectedIsland != null) {
            font.drawShadow(event.getMatrixStack(), "Selected island", 0, 0, 0xFFFFFF);
            font.drawShadow(event.getMatrixStack(), selectedIsland.getName(), 0, 20, 0xFFFFFF);
        }
    }

    private static void replaceCreateWorldButton(GuiScreenEvent.InitGuiEvent.Post event) {
        TranslatableComponent component = new TranslatableComponent("selectWorld.create");
        event.getWidgetList().stream()
            .filter(widget -> widget.getMessage().equals(component))
            .findFirst()
            .ifPresent(btn -> {
                event.addWidget(new Button(btn.x, btn.y, btn.getWidth(), btn.getHeight(), component, b -> Minecraft.getInstance().setScreen(new IslandDirectoryScreen(IslandsManager.getAvailableIslands(), island -> {
                    selectedIsland = island;
                    Minecraft.getInstance().setScreen(CreateWorldScreen.create(event.getGui()));
                }, event.getGui()))));
                event.removeWidget(btn);
            });
    }

    private static void replaceSingleplayerButton(GuiScreenEvent.InitGuiEvent.Post event) {
        LevelStorageSource lv = Minecraft.getInstance().getLevelSource();
        try {
            levels = lv.getLevelList();
        } catch (LevelStorageException var7) {
            FTBTeamIslands.LOGGER.error("Couldn't load level list", var7);
            Minecraft.getInstance().setScreen(new ErrorScreen(new TranslatableComponent("selectWorld.unable_to_load"), new TextComponent(var7.getMessage())));
            return;
        }

        // Replace the single player button with our own
        final TranslatableComponent component = new TranslatableComponent("menu.singleplayer");
        event.getWidgetList().stream()
            .filter(widget -> widget.getMessage().equals(component))
            .findFirst()
            .ifPresent(btn -> {
                event.addWidget(new Button(btn.x, btn.y, btn.getWidth(), btn.getHeight(), component, b -> {
                    if (levels.size() > 0) {
                        Minecraft.getInstance().setScreen(new SelectWorldScreen(event.getGui()));
                        return;
                    }

                    Minecraft.getInstance().setScreen(new IslandDirectoryScreen(IslandsManager.getAvailableIslands(), island -> {
                        selectedIsland = island;
                        Minecraft.getInstance().setScreen(CreateWorldScreen.create(null));
                    }, event.getGui()));
                }));
                event.removeWidget(btn);
            });
    }
}
