package dev.ftb.mods.ftbteamislands.screens;

import dev.ftb.mods.ftbteamislands.islands.PrebuiltIslands;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import java.util.List;

public class IslandDirectoryScreen extends Screen {
    public IslandDirectoryScreen(List<PrebuiltIslands> islands) {
        super(TextComponent.EMPTY);

        System.out.println(islands);
    }
}
