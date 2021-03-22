package dev.ftb.mods.ftbteamislands.intergration;

import com.feed_the_beast.mods.ftbchunks.api.ChunkDimPos;
import com.feed_the_beast.mods.ftbchunks.impl.FTBChunksAPIImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class FTBChunks {

    public static void claimChunks(ServerPlayer player, Level level, BlockPos pos) {
        FTBChunksAPIImpl.INSTANCE.getManager().getData(player).claim(player.createCommandSourceStack(), new ChunkDimPos(level, pos), false);
    }
}
