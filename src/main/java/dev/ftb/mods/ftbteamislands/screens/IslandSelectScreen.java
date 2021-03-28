package dev.ftb.mods.ftbteamislands.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.islands.PrebuiltIslands;
import dev.ftb.mods.ftbteamislands.network.IslandSelectionPacket;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class IslandSelectScreen extends Screen {
    // Store this so we don't have to ask the server for it again in-case we need to go back.
    private final List<PrebuiltIslands> previousScreenData;
    private final PrebuiltIslands selectedIslandDir;

    private IslandList islandList;
    private EditBox searchBox;
    private Button createButton;

    public IslandSelectScreen(PrebuiltIslands selected, List<PrebuiltIslands> previousScreenData) {
        super(TextComponent.EMPTY);

        this.selectedIslandDir = selected;
        this.previousScreenData = previousScreenData;
    }

    @Override
    protected void init() {
        super.init();

        this.islandList = new IslandList(this.getMinecraft(), this.width, this.height, 80, this.height - 40, this.selectedIslandDir);
        this.searchBox = new EditBox(this.font, this.width / 2 - 160 / 2, 40, 160, 20, TextComponent.EMPTY);
        this.searchBox.setResponder(this.islandList::searchList);

        this.addButton(new Button(this.width / 2 - 130, this.height - 30, 100, 20, new TranslatableComponent("screens.ftbteamislands.back"), btn ->
            Minecraft.getInstance().setScreen(new IslandDirectoryScreen(this.previousScreenData))));

        this.addButton(this.createButton = new Button(this.width / 2 - 20, this.height - 30, 150, 20, new TranslatableComponent("screens.ftbteamislands.create"), btn -> {
            if (this.islandList.getSelected() == null) {
                return;
            }

            this.onClose();
            NetworkManager.sendToServer(new IslandSelectionPacket(this.islandList.getSelected().islandDir.getStructureFileLocation(), this.islandList.getSelected().islandDir.yOffset()));
        }));

        this.createButton.active = false;

        this.children.add(this.searchBox);
        this.children.add(this.islandList);

        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(matrices);

        this.islandList.render(matrices, mouseX, mouseY, partialTick);
        super.render(matrices, mouseX, mouseY, partialTick);
        this.searchBox.render(matrices, mouseX, mouseY, partialTick);

        String value = new TranslatableComponent("screens.ftbteamislands.select_island").getString();
        this.font.drawShadow(matrices, value, this.width / 2f - this.font.width(value) / 2f, 20, 0xFFFFFF);
    }

    public class IslandList extends AbstractSelectionList<IslandList.Entry> {
        private final PrebuiltIslands islands;

        public IslandList(Minecraft minecraft, int width, int height, int top, int bottom, PrebuiltIslands entries) {
            super(minecraft, width, height, top, bottom, 50); // 30 = item height

            this.islands = entries;
            this.children().addAll(entries.getIslands().stream().map(IslandList.Entry::new).collect(Collectors.toList()));
        }

        @Override
        public int getRowWidth() {
            return 340;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 170;
        }

        @Override
        public void setSelected(@Nullable IslandSelectScreen.IslandList.Entry entry) {
            IslandSelectScreen.this.createButton.active = entry != null;
            super.setSelected(entry);
        }

        public void searchList(String value) {
            this.children().clear();

            String lowerValue = value.toLowerCase();
            if (lowerValue.equals("")) {
                this.children().addAll(this.islands.getIslands().stream()
                    .map(IslandList.Entry::new)
                    .collect(Collectors.toList())
                );
                return;
            }

            this.children().addAll(this.islands.getIslands().stream()
                .filter(island -> island.getName().toLowerCase().contains(lowerValue) || island.getDesc().toLowerCase().contains(lowerValue))
                .map(IslandList.Entry::new)
                .collect(Collectors.toList())
            );
        }

        public class Entry extends AbstractSelectionList.Entry<IslandList.Entry> {
            private final PrebuiltIslands.PrebuiltIsland islandDir;
            private long lastClickTime;

            public Entry(PrebuiltIslands.PrebuiltIsland island) {
                this.islandDir = island;
            }

            @Override
            public boolean mouseClicked(double x, double y, int partialTick) {
                IslandList.this.setSelected(this);

                if (Util.getMillis() - this.lastClickTime < 250L) {
                    IslandSelectScreen.this.onClose();
                    NetworkManager.sendToServer(new IslandSelectionPacket(this.islandDir.getStructureFileLocation(), this.islandDir.yOffset()));
                    return true;
                } else {
                    this.lastClickTime = Util.getMillis();
                    return false;
                }
            }

            @Override
            public void render(PoseStack matrices, int entryId, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean bl, float partialTicks) {
                Font font = Minecraft.getInstance().font;

                int startX = left + 80;
                font.drawShadow(matrices, this.islandDir.getName(), startX, top + 10, 0xFFFFFF);
                font.drawShadow(matrices, this.islandDir.getDesc(), startX, top + 26, 0xFFFFFF);

                try {
                    Minecraft.getInstance().textureManager.bind(this.islandDir.getImage());
                    blit(matrices, left + 7, top + 7, 0f, 0f, 56, 32, 56, 32);
                } catch (Exception ex) {
                    FTBTeamIslands.LOGGER.warn("{} not found in resources", this.islandDir.getImage());
                }
            }
        }
    }
}
