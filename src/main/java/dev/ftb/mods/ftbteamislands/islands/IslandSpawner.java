package dev.ftb.mods.ftbteamislands.islands;

import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.intergration.FTBChunks;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import dev.ftb.mods.ftbteamislands.network.OpenSelectionScreenPacket;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.fml.ModList;

import java.util.Iterator;

public class IslandSpawner {

    /**
     * Island spawner logic for the lobby
     */
    public static void spawnLobby(ServerLevel level) {
        BlockPos centerOfWorld = new BlockPos(0, Config.islands.height.get(), 0);
        Island lobby = spawnIslandFromTemplate(new ResourceLocation(FTBTeamIslands.MOD_ID, Config.lobby.lobbyIslandFile.get()), level, centerOfWorld);
        if (lobby == null) {
            FTBTeamIslands.LOGGER.error("Failed to create Lobby!");
            return;
        }

        IslandsManager.get().setLobby(lobby);
        level.setDefaultSpawnPos(lobby.getSpawnPos(), 90f);
    }

    /**
     * Island spawner logic for single player initial setup
     */
    public static void spawnIslandSinglePlayer(ServerLevel level, Team team, ServerPlayer player, MinecraftServer server) {
        if (player == null)
            return;

        IslandsManager manager = IslandsManager.get();
        if (manager.getAvailableIslands().size() > 0) {
            NetworkManager.sendTo(new OpenSelectionScreenPacket(), player);
            return;
        }

        // If there is only the default island, generate it and spawn the island.
        int index = Math.max(1, manager.getIslands().size());
        int distanceInRegions = Config.islands.distanceBetweenIslands.get();
        BlockPos spawnPos = new BlockPos(256 + ((index * distanceInRegions * 512) % 1024), Config.islands.height.get(), 256 + ((index * distanceInRegions * 512) / 1024));
        Island island = spawnIslandFromTemplate(new ResourceLocation(FTBTeamIslands.MOD_ID, "teamislands_island"), level, spawnPos);
        if (island == null) {
            FTBTeamIslands.LOGGER.error("Failed to create spawn!!");
            return;
        }

        manager.registerIsland(team, island);
        level.setDefaultSpawnPos(island.getSpawnPos(), 90f);
        island.teleportPlayerTo(player, server);

        if (ModList.get().isLoaded("ftbchunks")) {
            FTBChunks.claimChunks(player, server.getLevel(IslandsManager.getTargetIsland()), island.pos);
        }
    }

    public static Island spawnIslandFromTemplate(ResourceLocation structureLoc, ServerLevel level, BlockPos spawnPos) {
        StructureTemplate template = level.getStructureManager().get(structureLoc);
        if (template == null) {
            return null;
        }

        BoundingBox boundingBox = template.getBoundingBox(new StructurePlaceSettings(), BlockPos.ZERO);
        BoundingBox inWorldBoundingBox = template.getBoundingBox(new StructurePlaceSettings(), spawnPos.offset(-boundingBox.x1 / 2, 0, -boundingBox.z1 / 2));
        template.placeInWorldChunk(level, spawnPos.offset(-boundingBox.x1 / 2, 0, -boundingBox.z1 / 2), new StructurePlaceSettings(), level.getRandom());

        // TODO: find a better way of doing this!
        // Find the spawn pos and remove any remaining structure blocks
        BlockPos playerSpawnPoint = spawnPos.above(2);
        Iterator<BlockPos> iterator = BlockPos.betweenClosedStream(inWorldBoundingBox).iterator();
        while (iterator.hasNext()) {
            BlockPos next = iterator.next();
            if (level.getBlockState(next).getBlock() instanceof StructureBlock) {
                BlockEntity blockEntity = level.getBlockEntity(next);
                if (blockEntity instanceof StructureBlockEntity && ((StructureBlockEntity) blockEntity).getMetaData().startsWith("SPAWN_POINT")) {
                    playerSpawnPoint = next.mutable();
                }

                level.removeBlock(next, false);
            }
        }

        return new Island(
            new ChunkPos(spawnPos),
            playerSpawnPoint,
            null,
            true,
            true
        );
    }
}
