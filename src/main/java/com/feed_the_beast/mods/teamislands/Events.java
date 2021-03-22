package com.feed_the_beast.mods.teamislands;

import com.feed_the_beast.mods.ftbteams.data.Team;
import com.feed_the_beast.mods.ftbteams.data.TeamManager;
import com.feed_the_beast.mods.ftbteams.event.PlayerChangedTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamCreatedEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import com.feed_the_beast.mods.teamislands.islands.Island;
import com.feed_the_beast.mods.teamislands.islands.IslandsSave;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TeamIslands.MOD_ID)
public class Events {
    /**
     * Upon a player joining a team, we assume that we're running
     */
    public static void onTeamCreated(TeamCreatedEvent event) {
//        Team team = event.getTeam();
//        MinecraftServer server = team.manager.getServer();
//
//        if (!Config.general.isEnabled(server))
//            return;
//
//        TeamIslands.LOGGER.info("Player joined Team");
//
//        // Single player logic
//        IslandsSave islandsSave = IslandsSave.get(Objects.requireNonNull(event.getTeam().getOwnerPlayer()).level);
//
//        int index = islandsSave.getIslands().size() + 1;
//        int distanceInRegions = Config.islands.distanceBetweenIslands.get();
//        ChunkPos chunkPos = new ChunkPos(256 + ((index * distanceInRegions * 512) % 1024), 256 + ((index * distanceInRegions * 512) / 1024));
//
//        // Get or create a new island for the player, it's most likely going to be create.
//        Island island = islandsSave.getIsland(team).orElse(new Island(
//            chunkPos,
//            chunkPos.getWorldPosition(),
//            "template",
//            team.getOwnerPlayer().getUUID()
//        ));
//
//        StructureTemplate template = team.getOwnerPlayer().getServer().getStructureManager().getOrCreate(new ResourceLocation(TeamIslands.MOD_ID, "teamislands_island"));
//        template.placeInWorldChunk((ServerLevelAccessor) team.getOwnerPlayer().level, team.getOwnerPlayer().blockPosition(), new StructurePlaceSettings(), new Random());
//        //        server.getStructureManager();
//
//        //        if (!server.isDedicatedServer()) {
//        //            if (island.isPresent()) {
//        //                event.getPlayer().tel;
//        //            }
//        //            return;
//        //        }
//
//        // MP logic
    }

    /**
     * Clear the players inventory upon leaving and reset their spawn chunk to the lobby.
     */
    public static void onChangedTeamEvent(PlayerChangedTeamEvent event) {
        Team team = event.getTeam();
        MinecraftServer server = team.manager.getServer();

        // Don't run if the mod is disabled
        if (!Config.general.isEnabled(server))
            return;

        ServerLevel level = team.manager.getServer().getLevel(Level.OVERWORLD);
        IslandsSave islandsSave = IslandsSave.get(level);
        if (islandsSave == null || level == null) {
            return;
        }

        // No lobby? Spawn one :D
        if (islandsSave.getLobby() == null) {
            System.out.println("Attempting to spawn lobby");
            // TODO: Support loading from config
            StructureTemplate template = level.getStructureManager().get(new ResourceLocation(TeamIslands.MOD_ID, "default_lobby"));
            BlockPos centerOfWorld = new BlockPos(0, Config.islands.height.get(), 0);
            BoundingBox boundingBox = template.getBoundingBox(new StructurePlaceSettings(), centerOfWorld);

            template.placeInWorldChunk(level, centerOfWorld.offset(-boundingBox.x1 / 2, 0, -boundingBox.z1 / 2), new StructurePlaceSettings(), level.getRandom());

            islandsSave.setLobby(new Island(
                new ChunkPos(centerOfWorld),
                centerOfWorld.above(2),
                "lobby",
                null,
                true,
                true
            ));

            islandsSave.setDirty();

            ServerPlayer ownerPlayer = team.getOwnerPlayer();
            if (ownerPlayer == null) {
                return;
            }

            if (ownerPlayer.level.dimension() != level.dimension()) {
                ownerPlayer.changeDimension(level);
            }

            ownerPlayer.teleportTo(centerOfWorld.getX(), centerOfWorld.getY() + 4, centerOfWorld.getZ());
        }
    }

    /**
     * Upon deletion, validate against any existing islands and mark them as unused.
     */
    public static void onTeamDeleted(TeamDeletedEvent event) {
        if (!Config.general.isEnabled(event.getTeam().manager.getServer()))
            return;

        TeamIslands.LOGGER.info("Team deleted");
    }

    /**
     * Upon player death, check if they have a valid island or a valid bed, respawn the player
     * on that island or on that bed. Do basic safety checking as well, lava etc.
     */
    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer().getServer() == null || !Config.general.isEnabled(event.getPlayer().getServer()))
            return;

        TeamIslands.LOGGER.info("Player death / respawned");
        Team playerTeam = TeamManager.INSTANCE.getPlayerTeam(event.getPlayer().getUUID());
        if (playerTeam == null)
            return;

        IslandsSave islandsSave = IslandsSave.get(event.getPlayer().getServer().getLevel(Level.OVERWORLD));
        BlockPos respawnPos = event.getPlayer().getSleepingPos()
            .orElse(islandsSave.getIsland(playerTeam)
                .map(Island::getSpawnPos)
                .orElse(islandsSave.getLobby() == null ? new BlockPos(0, Config.islands.height.get(), 0) : islandsSave.getLobby().spawnPos));

        event.getPlayer().teleportTo(respawnPos.getX() + .5D, respawnPos.getY() + 2, respawnPos.getZ() + .5D);
    }
}
