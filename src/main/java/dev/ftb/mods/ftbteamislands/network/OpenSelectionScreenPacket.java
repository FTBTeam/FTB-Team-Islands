package dev.ftb.mods.ftbteamislands.network;

import dev.ftb.mods.ftbteamislands.ClientHandler;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// TODO: check if this is the right way to open a stand gui on the player
public class OpenSelectionScreenPacket {

    public OpenSelectionScreenPacket() {}

    public void encode(FriendlyByteBuf buffer) {}

    public static OpenSelectionScreenPacket decode(FriendlyByteBuf buffer) {
        return new OpenSelectionScreenPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ClientHandler.openSelectionGui();
                return;
            }

            FTBTeamIslands.LOGGER.error("Open gui packet sent to server!");
        });
        ctx.setPacketHandled(true);
    }
}
