package dev.ftb.mods.ftbteamislands.network;

import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.islands.IslandSpawner;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class IslandSelectionPacket {
    String islandNbtFile;
    int yOffset;

    public IslandSelectionPacket(String islandNbtFile, int yOffset) {
        this.islandNbtFile = islandNbtFile;
        this.yOffset = yOffset;
    }

    public static IslandSelectionPacket decode(FriendlyByteBuf buffer) {
        return new IslandSelectionPacket(buffer.readUtf(Short.MAX_VALUE), buffer.readInt());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.islandNbtFile);
        buffer.writeInt(this.yOffset);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                FTBTeamIslands.LOGGER.error("Selection packet fired on incorrect side");
                return;
            }

            ServerPlayer player = ctx.getSender();
            if (player == null || player.getServer() == null) {
                return;
            }

            player.displayClientMessage(new TranslatableComponent("commands.ftbteamislands.response.island_creating"), false);
            IslandSpawner.spawnIsland(
                this.islandNbtFile,
                player.getServer().getLevel(IslandsManager.getTargetIsland()),
                TeamManager.INSTANCE.getPlayerTeam(player),
                player,
                player.getServer(),
                this.yOffset
            );
            player.displayClientMessage(new TranslatableComponent("commands.ftbteamislands.response.island_creating_finished"), false);
        });
        ctx.setPacketHandled(true);
    }
}
