package dev.ftb.mods.ftbteamislands.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbteamislands.ClientHandler;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.islands.PrebuiltIslands;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class IslandDirectoryScreen extends Screen {
    private final List<PrebuiltIslands> islands;
    public DirectoryList islandDirectoryList;

    private EditBox searchBox;
    private Button selectButton;
    private Consumer<PrebuiltIslands.PrebuiltIsland> onPrebuiltSelect;

    public IslandDirectoryScreen(List<PrebuiltIslands> islands, Consumer<PrebuiltIslands.PrebuiltIsland> onPrebuiltSelect) {
        super(TextComponent.EMPTY);
        this.islands = islands;
        this.onPrebuiltSelect = onPrebuiltSelect;

        ClientHandler.selectedIsland = null;
    }

    @Override
    protected void init() {
        super.init();

        this.islandDirectoryList = new DirectoryList(this.getMinecraft(), this.width, this.height, 80, this.height - 35, this.islands);
        this.searchBox = new EditBox(this.font, this.width / 2 - 160 / 2, 40, 160, 20, TextComponent.EMPTY);
        this.searchBox.setResponder(value -> this.islandDirectoryList.searchList(value));

        this.addButton(new Button(this.width / 2 - 130, this.height - 30, 100, 20, new TranslatableComponent("screens.ftbteamislands.close"), btn -> {
            this.onClose();
        }));

        this.addButton(this.selectButton = new Button(this.width / 2 - 20, this.height - 30, 150, 20, new TranslatableComponent("screens.ftbteamislands.select"), btn -> {
            if (this.islandDirectoryList.getSelected() == null) {
                return;
            }

            Minecraft.getInstance().setScreen(new IslandSelectScreen(this.islandDirectoryList.getSelected().islandDir, IslandDirectoryScreen.this.islands, this.onPrebuiltSelect));
        }));

        this.children.add(this.searchBox);
        this.children.add(this.islandDirectoryList);
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);

        this.islandDirectoryList.render(matrices, mouseX, mouseY, partialTicks);
        this.searchBox.render(matrices, mouseX, mouseY, partialTicks);

        super.render(matrices, mouseX, mouseY, partialTicks);

        String value = new TranslatableComponent("screens.ftbteamislands.select_island_category").getString();
        this.font.drawShadow(matrices, value, this.width / 2f - this.font.width(value) / 2f, 20, 0xFFFFFF);
    }

    @Override
    public void tick() {
        this.searchBox.tick();
    }

    public class DirectoryList extends AbstractSelectionList<DirectoryList.DirectoryEntry> {
        private final List<PrebuiltIslands> islands;

        public DirectoryList(Minecraft minecraft, int width, int height, int top, int bottom, List<PrebuiltIslands> entries) {
            super(minecraft, width, height, top, bottom, 50); // 30 = item height

            this.islands = entries;
            this.children().addAll(entries.stream().map(DirectoryEntry::new).collect(Collectors.toList()));
        }

        @Override
        public int getRowWidth() {
            return 340;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + 170;
        }

        public void searchList(String value) {
            this.children().clear();

            String lowerValue = value.toLowerCase();
            if (lowerValue.equals("")) {
                this.children().addAll(this.islands.stream()
                    .map(DirectoryEntry::new)
                    .collect(Collectors.toList())
                );
                return;
            }

            this.children().addAll(this.islands.stream()
                .filter(island -> island.getName().toLowerCase().contains(lowerValue) || island.getDesc().toLowerCase().contains(lowerValue))
                .map(DirectoryEntry::new)
                .collect(Collectors.toList())
            );
        }

        @Override
        public void setSelected(@Nullable IslandDirectoryScreen.DirectoryList.DirectoryEntry entry) {
            IslandDirectoryScreen.this.selectButton.active = entry != null;

            super.setSelected(entry);
        }

        public class DirectoryEntry extends AbstractSelectionList.Entry<DirectoryList.DirectoryEntry> {
            private final ResourceLocation fileIcon = new ResourceLocation(FTBTeamIslands.MOD_ID, "textures/screens/foldericon.png");
            private final PrebuiltIslands islandDir;
            private long lastClickTime;

            public DirectoryEntry(PrebuiltIslands islandDir) {
                this.islandDir = islandDir;
            }

            @Override
            public boolean mouseClicked(double x, double y, int partialTick) {
                DirectoryList.this.setSelected(this);

                if (Util.getMillis() - this.lastClickTime < 250L) {
                    Minecraft.getInstance().setScreen(new IslandSelectScreen(this.islandDir, IslandDirectoryScreen.this.islands, IslandDirectoryScreen.this.onPrebuiltSelect));
                    return true;
                } else {
                    this.lastClickTime = Util.getMillis();
                    return false;
                }
            }

            @Override
            public void render(PoseStack matrices, int entryId, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean bl, float partialTicks) {
                Font font = Minecraft.getInstance().font;

                int startX = left + 50;
                font.drawShadow(matrices, this.islandDir.getName(), startX, top + 8, 0xFFFFFF);
                font.drawShadow(matrices, new TranslatableComponent("screens.ftbteamislands.by", this.islandDir.getAuthor()), startX + font.width(this.islandDir.getName()) + 10, top + 8, 0xD3D3D3);
                font.drawShadow(matrices, this.islandDir.getDesc(), startX, top + 24, 0xFFFFFF);

                Minecraft.getInstance().textureManager.bind(this.fileIcon);
                blit(matrices, left + 5, top + 8, 0f, 0f, 32, 32, 32, 32);

                String islandCount = String.valueOf(this.islandDir.getIslands().size());
                font.drawShadow(matrices, islandCount, left + 22 - font.width(islandCount) / 2f, top + 22, 0xFFFFFF);
            }
        }
    }
}
