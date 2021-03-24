package dev.ftb.mods.ftbteamislands.intergration;

import com.feed_the_beast.mods.ftbchunks.api.ChunkDimPos;
import com.feed_the_beast.mods.ftbchunks.api.ClaimedChunkPlayerData;
import com.feed_the_beast.mods.ftbchunks.impl.FTBChunksAPIImpl;
import dev.ftb.mods.ftbteamislands.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class FTBChunks {

    public static void claimChunks(ServerPlayer player, Level level, ChunkPos pos) {
        ClaimedChunkPlayerData data = FTBChunksAPIImpl.INSTANCE.getManager().getData(player);
        int claimRadius = Config.islands.autoClaimChunkRadius.get();
        int startX = pos.x - claimRadius / 2;
        int startZ = pos.z - claimRadius / 2;

        for (int x = startX; x < startX + claimRadius / 2; x ++) {
            for (int z = startZ; z < startZ + claimRadius / 2; z ++) {
                System.out.println(new ChunkPos(x, z).getMinBlockX());
                System.out.println(data.claim(player.createCommandSourceStack(), new ChunkDimPos(level.dimension(), new ChunkPos(x, z)), false).isSuccess());
            }
        }
    }
}
