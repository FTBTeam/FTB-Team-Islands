package dev.ftb.mods.ftbteamislands;

import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandSpawner;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import dev.ftb.mods.ftbteams.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.event.TeamDeletedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = FTBTeamIslands.MOD_ID)
public class Events {
    public static void onTeamCreated(TeamCreatedEvent event) {

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

        ServerLevel level = team.manager.getServer().getLevel(IslandsManager.getTargetIsland());
        IslandsManager islandsManager = IslandsManager.get(level);
        if (islandsManager == null || level == null) {
            return;
        }

        // If we're a server, attempt to spawn a lobby
        if (server.isDedicatedServer()) {
            FTBTeamIslands.LOGGER.info("Attempting to spawn lobby");
            if (islandsManager.getLobby() == null)
                IslandSpawner.spawnLobby(islandsManager, team.manager.getServer(), level, team);

            return;
        }

        // If we're in single player, attempt to spawn an island if we don't already have one
        Optional<Island> island = islandsManager.getIsland(team);
        if (island.isPresent()) {
            return;
        }

        IslandSpawner.spawnIsland(islandsManager, level, team, team.getOnlineMembers().get(0));
    }

    /**
     * Upon deletion, validate against any existing islands and mark them as unused.
     */
    public static void onTeamDeleted(TeamDeletedEvent event) {
        if (!Config.general.isEnabled(event.getTeam().manager.getServer()))
            return;

        FTBTeamIslands.LOGGER.info("Team deleted");
    }

    /**
     * Upon player death, check if they have a valid island or a valid bed, respawn the player
     * on that island or on that bed. Do basic safety checking as well, lava etc.
     */
    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer().getServer() == null || !Config.general.isEnabled(event.getPlayer().getServer()))
            return;

        FTBTeamIslands.LOGGER.info("Player death / respawned");
        Team playerTeam = TeamManager.INSTANCE.getPlayerTeam(event.getPlayer().getUUID());
        if (playerTeam == null)
            return;

        IslandsManager islandsManager = IslandsManager.get(event.getPlayer().getServer().getLevel(IslandsManager.getTargetIsland()));
        BlockPos respawnPos = event.getPlayer().getSleepingPos()
            .orElse(islandsManager.getIsland(playerTeam)
                .map(Island::getSpawnPos)
                .orElse(islandsManager.getLobby() == null ? new BlockPos(0, Config.islands.height.get(), 0) : islandsManager.getLobby().spawnPos));

        event.getPlayer().teleportTo(respawnPos.getX() + .5D, respawnPos.getY() + 2, respawnPos.getZ() + .5D);
    }
}
