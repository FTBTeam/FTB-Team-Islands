package dev.ftb.mods.ftbteamislands;

import dev.ftb.mods.ftbteamislands.screens.IslandDirectoryScreen;
import net.minecraft.client.Minecraft;

public class ClientHandler {

    public static void openSelectionGui() {
        Minecraft.getInstance().setScreen(new IslandDirectoryScreen());
    }
}
