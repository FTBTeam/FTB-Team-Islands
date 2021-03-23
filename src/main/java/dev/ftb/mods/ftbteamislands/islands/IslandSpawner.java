package dev.ftb.mods.ftbteamislands.islands;

import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import dev.ftb.mods.ftbteamislands.network.OpenSelectionScreenPacket;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class IslandSpawner {

    public static void spawnLobby(IslandsManager manager, MinecraftServer server, ServerLevel level, Team team) {
        // TODO: Support loading from config
        StructureTemplate template = level.getStructureManager().get(new ResourceLocation(FTBTeamIslands.MOD_ID, "default_lobby"));
        BlockPos centerOfWorld = new BlockPos(0, Config.islands.height.get(), 0);
        BoundingBox boundingBox = template.getBoundingBox(new StructurePlaceSettings(), centerOfWorld);

        template.placeInWorldChunk(level, centerOfWorld.offset(-boundingBox.x1 / 2, 0, -boundingBox.z1 / 2), new StructurePlaceSettings(), level.getRandom());

        Island lobby = new Island(
            new ChunkPos(centerOfWorld),
            centerOfWorld.above(2),
            "lobby",
            null,
            true,
            true
        );

        manager.setLobby(lobby);
        manager.setDirty();

        level.setDefaultSpawnPos(lobby.getSpawnPos(), 90f);
    }

    public static void spawnIsland(IslandsManager manager, ServerLevel level, Team team, ServerPlayer player) {
        if (player == null) {
            return;
        }

        if (manager.getAvailableIslands().size() == 0) {
            FTBTeamIslands.LOGGER.info("SPAWNING SINGLE PLAYER ISLAND");
            NetworkManager.sendTo(new OpenSelectionScreenPacket(), player);
        }
    }
}
