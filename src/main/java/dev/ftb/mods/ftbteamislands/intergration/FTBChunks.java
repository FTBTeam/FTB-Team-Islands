package dev.ftb.mods.ftbteamislands.intergration;

import dev.ftb.mods.ftbchunks.data.ClaimResult;
import dev.ftb.mods.ftbchunks.data.ClaimResults;
import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
import dev.ftb.mods.ftbteamislands.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.time.Instant;

public class FTBChunks {

    public static void claimChunks(ServerPlayer player, Level level, ChunkPos pos) {
        int claimRadius = Config.islands.autoClaimChunkRadius.get();
        int startX = pos.x - claimRadius / 2;
        int startZ = pos.z - claimRadius / 2;

        Instant time = Instant.now();

        for (int x = startX; x < startX + claimRadius; x++) {
            for (int z = startZ; z < startZ + claimRadius; z++) {
                ClaimResult claimResult = FTBChunksAPI.claimAsPlayer(player, level.dimension(), new ChunkPos(x, z), false);
                if (claimResult.isSuccess()) {
                    claimResult.setClaimedTime(time);
                }

                if (claimResult == ClaimResults.NOT_ENOUGH_POWER || claimResult == ClaimResults.DIMENSION_FORBIDDEN) {
                    return; // Stop dead if no chunks could be auto claimed
                }
            }
        }

        FTBChunksAPI.syncPlayer(player);
    }
}
