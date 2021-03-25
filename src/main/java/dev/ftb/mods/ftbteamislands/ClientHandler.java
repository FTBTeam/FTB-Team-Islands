package dev.ftb.mods.ftbteamislands;

import dev.ftb.mods.ftbteamislands.islands.PrebuiltIslands;
import dev.ftb.mods.ftbteamislands.screens.IslandDirectoryScreen;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ClientHandler {

    public static void openSelectionGui(List<PrebuiltIslands> islands) {
        Minecraft.getInstance().setScreen(new IslandDirectoryScreen(islands));
    }
}
