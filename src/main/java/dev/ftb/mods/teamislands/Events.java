package dev.ftb.mods.teamislands;

import com.feed_the_beast.mods.ftbteams.data.Team;
import com.feed_the_beast.mods.ftbteams.data.TeamManager;
import com.feed_the_beast.mods.ftbteams.event.PlayerChangedTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import dev.ftb.mods.teamislands.islands.Island;
import dev.ftb.mods.teamislands.islands.IslandsManager;
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
     * Clear the players inventory upon leaving and reset their spawn chunk to the lobby.
     */
    public static void onChangedTeamEvent(PlayerChangedTeamEvent event) {
        Team team = event.getTeam();
        MinecraftServer server = team.manager.getServer();

        // Don't run if the mod is disabled
        if (!Config.general.isEnabled(server))
            return;

        ServerLevel level = team.manager.getServer().getLevel(IslandsManager.getTargetIsland());
        IslandsManager islandsManager = IslandsManager.get(level);
        if (islandsManager == null || level == null) {
            return;
        }

        // No lobby? Spawn one :D
        if (islandsManager.getLobby() == null) {
            // TODO: Support loading from config
            StructureTemplate template = level.getStructureManager().get(new ResourceLocation(TeamIslands.MOD_ID, "default_lobby"));
            BlockPos centerOfWorld = new BlockPos(0, Config.islands.height.get(), 0);
            BoundingBox boundingBox = template.getBoundingBox(new StructurePlaceSettings(), centerOfWorld);

            template.placeInWorldChunk(level, centerOfWorld.offset(-boundingBox.x1 / 2, 0, -boundingBox.z1 / 2), new StructurePlaceSettings(), level.getRandom());

            islandsManager.setLobby(new Island(
                new ChunkPos(centerOfWorld),
                centerOfWorld.above(2),
                "lobby",
                null,
                true,
                true
            ));

            islandsManager.setDirty();

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

        IslandsManager islandsManager = IslandsManager.get(event.getPlayer().getServer().getLevel(Level.OVERWORLD));
        BlockPos respawnPos = event.getPlayer().getSleepingPos()
            .orElse(islandsManager.getIsland(playerTeam)
                .map(Island::getSpawnPos)
                .orElse(islandsManager.getLobby() == null ? new BlockPos(0, Config.islands.height.get(), 0) : islandsManager.getLobby().spawnPos));

        event.getPlayer().teleportTo(respawnPos.getX() + .5D, respawnPos.getY() + 2, respawnPos.getZ() + .5D);
    }
}
