package dev.ftb.mods.ftbteamislands.network;

import dev.ftb.mods.ftbteamislands.ClientHandler;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.islands.PrebuiltIslands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// TODO: check if this is the right way to open a stand gui on the player

/**
 * @implNote this might be a bit much if the list is a lot bigger so a good solution
 *           might be to only send the main list, then ask for the specific islands
 *           after the directory has been selected.
 */
public class OpenSelectionScreenPacket {
    private final List<PrebuiltIslands> islands;

    public OpenSelectionScreenPacket(List<PrebuiltIslands> islands) {
        this.islands = islands;
    }

    public void encode(FriendlyByteBuf buffer) {
        CompoundTag compound = new CompoundTag();
        ListTag list = new ListTag();
        this.islands.forEach(island -> list.add(island.write()));
        compound.put("islands", list);

        buffer.writeNbt(compound);
    }

    public static OpenSelectionScreenPacket decode(FriendlyByteBuf buffer) {
        CompoundTag compoundTag = buffer.readNbt();
        if (compoundTag == null) {
            return new OpenSelectionScreenPacket(new ArrayList<>());
        }

        ListTag islands = compoundTag.getList("islands", Tag.TAG_COMPOUND);
        return new OpenSelectionScreenPacket(
            islands.stream().map(island -> PrebuiltIslands.read((CompoundTag) island)).collect(Collectors.toList())
        );
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                ClientHandler.openSelectionGui(islands);
                return;
            }

            FTBTeamIslands.LOGGER.error("Open gui packet sent to server!");
        });
        ctx.setPacketHandled(true);
    }
}
