package dev.ftb.mods.teamislands.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class IslandSelectionPacket {
    BlockPos pos;

    public IslandSelectionPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {

        });
        ctx.setPacketHandled(true);
    }
}
