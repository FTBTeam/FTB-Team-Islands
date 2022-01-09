//package dev.ftb.mods.ftbteamislands.intergration;
//
//import dev.ftb.mods.ftbchunks.data.ClaimResult;
//import dev.ftb.mods.ftbchunks.data.ClaimResults;
//import dev.ftb.mods.ftbchunks.data.FTBChunksAPI;
//import dev.ftb.mods.ftbteamislands.Config;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.level.ChunkPos;
//import net.minecraft.world.level.Level;
//
//import java.time.Instant;
//
//public class FTBChunks {
//
//    public static void claimChunks(ServerPlayer player, Level level, ChunkPos pos) {
//        int radius = Config.islands.autoClaimChunkRadius.get();
//
//        Instant time = Instant.now();
//        for (int x = pos.x - radius; x <= pos.x + radius; x++) {
//            for (int z = pos.z - radius; z <= pos.z + radius; z++) {
//                ClaimResult claimResult = FTBChunksAPI.claimAsPlayer(player, level.dimension(), new ChunkPos(x, z), false);
//                if (claimResult.isSuccess()) {
//                    claimResult.setClaimedTime(time.toEpochMilli());
//                }
//
//                if (claimResult == ClaimResults.NOT_ENOUGH_POWER || claimResult == ClaimResults.DIMENSION_FORBIDDEN) {
//                    return; // Stop dead if no chunks could be auto claimed
//                }
//            }
//        }
//
//        FTBChunksAPI.syncPlayer(player);
//    }
//}
