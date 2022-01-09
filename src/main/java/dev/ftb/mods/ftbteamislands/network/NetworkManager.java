package dev.ftb.mods.ftbteamislands.network;

import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
    private static final String PROTOCOL_VERSION = Integer.toString(1);

    public static final SimpleChannel OUR_CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(FTBTeamIslands.MOD_ID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void register() {
        int index = 0;
        OUR_CHANNEL.registerMessage(index++, OpenSelectionScreenPacket.class, OpenSelectionScreenPacket::encode, OpenSelectionScreenPacket::decode, OpenSelectionScreenPacket::handle);
        OUR_CHANNEL.registerMessage(index++, IslandSelectionPacket.class, IslandSelectionPacket::encode, IslandSelectionPacket::decode, IslandSelectionPacket::handle);
    }

    /**
     * Sends a packet a given client
     */
    public static void sendTo(Object msg, ServerPlayer player) {
        if (player instanceof FakePlayer)
            return;

        OUR_CHANNEL.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Sends a packet to the server.
     */
    public static void sendToServer(Object msg) {
        OUR_CHANNEL.sendToServer(msg);
    }

    /**
     * Sends a packet to a target
     */
    public static void send(Object msg, PacketDistributor.PacketTarget target) {
        OUR_CHANNEL.send(target, msg);
    }
}
