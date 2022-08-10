package dev.ftb.mods.ftbteamislands;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteamislands.islands.PrebuiltIslands;
import dev.ftb.mods.ftbteamislands.network.IslandSelectionPacket;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import dev.ftb.mods.ftbteamislands.screens.IslandDirectoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = FTBTeamIslands.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {
    public static PrebuiltIslands.PrebuiltIsland selectedIsland;

    public static void openSelectionGui(List<PrebuiltIslands> islands) {
        Minecraft.getInstance().setScreen(new IslandDirectoryScreen(islands, island -> NetworkManager.sendToServer(new IslandSelectionPacket(island.getStructureFileLocation(), island.yOffset()))));
    }

    @SubscribeEvent
    public static void screenEventPre(ScreenEvent.InitScreenEvent.Pre event) {
        if (IslandsManager.getAvailableIslands().size() == 0) {
            return;
        }

        if (event.getScreen() instanceof CreateWorldScreen && selectedIsland == null) {
            Minecraft.getInstance().setScreen(new IslandDirectoryScreen(IslandsManager.getAvailableIslands(), island -> {
                selectedIsland = island;
                Minecraft.getInstance().setScreen(CreateWorldScreen.createFresh(null));
            }));
        }
    }

    @SubscribeEvent
    public static void screenEventPost(ScreenEvent.InitScreenEvent.Post event) {
        Screen gui = event.getScreen();
        if (IslandsManager.getAvailableIslands().size() == 0 || !(gui instanceof CreateWorldScreen)) {
            return;
        }

        if (selectedIsland != null) {
            event.addListener(new Button(10, 74, 112, 20, new TextComponent("Select island"), (b) -> {
                selectedIsland = null;
                Minecraft.getInstance().setScreen(new IslandDirectoryScreen(IslandsManager.getAvailableIslands(), island -> {
                    selectedIsland = island;
                    Minecraft.getInstance().setScreen(CreateWorldScreen.createFresh(null));
                }));
            }));
        }
    }

    @SubscribeEvent
    public static void screenRender(ScreenEvent.DrawScreenEvent.Post event) {
        if (!(event.getScreen() instanceof CreateWorldScreen)) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        if (selectedIsland != null) {
            PoseStack matrixStack = event.getPoseStack();

            RenderSystem.setShaderTexture(0, selectedIsland.getImage());
            Screen.blit(matrixStack, 10, 10, 0f, 0f, 112, 64, 112, 64);
            Screen.fill(matrixStack, 10, 10, 10 + 112, 74, 0x93000000);

            matrixStack.pushPose();
            matrixStack.scale(.8F, .8F, .8F);
            matrixStack.translate((55 + 112) / 2f, 20f, 0);
            Screen.drawCenteredString(matrixStack, font, "Selected island", 0, 0, 0xD1D1D1);
            matrixStack.popPose();

            MultiLineLabel
                    .create(font, new TextComponent(selectedIsland.getName()), 100)
                    .renderCentered(matrixStack, (20 + 112) / 2, 28);
        }
    }
}
