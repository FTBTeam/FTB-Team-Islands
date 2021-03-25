package dev.ftb.mods.ftbteamislands;

import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandSpawner;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import dev.ftb.mods.ftbteams.data.TeamType;
import dev.ftb.mods.ftbteams.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.event.TeamDeletedEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = FTBTeamIslands.MOD_ID)
public class Events {
    public static void onTeamCreated(TeamCreatedEvent event) {
        Team team = event.getTeam();
        MinecraftServer server = team.manager.getServer();

        // Don't run if the mod is disabled
        if (!Config.general.isEnabled(server))
            return;

        ServerLevel level = team.manager.getServer().getLevel(IslandsManager.getTargetIsland());
        IslandsManager islandsManager = IslandsManager.get();
        if (islandsManager == null || level == null)
            return;

        // If we're a server, attempt to spawn a lobby
        // Bypass lobby spawning if we're spawning into a single player world and there is only a single island
        if (!islandsManager.getLobby().isPresent() && (server.isDedicatedServer() || IslandsManager.get().getAvailableIslands().size() > 0))
            IslandSpawner.spawnLobby(level);

        // If we're in single player, attempt to spawn an island if we don't already have one
        Optional<Island> island = islandsManager.getIsland(team);
        if (island.isPresent() || server.isDedicatedServer())
            return;

        IslandSpawner.spawnIslandSinglePlayer(level, team, event.getCreator(), server);
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
    }

    /**
     * Upon deletion, validate against any existing islands and mark them as unused.
     */
    public static void onTeamDeleted(TeamDeletedEvent event) {
        Team team = event.getTeam();
        if (!Config.general.isEnabled(team.manager.getServer()) || event.getTeam().getType() != TeamType.PARTY)
            return;

        // Flag unused
        IslandsManager.get().markUnclaimed(team.getId());
    }

    /**
     * Upon player death, check if they have a valid island or a valid bed, respawn the player
     * on that island or on that bed. Do basic safety checking as well, lava etc.
     */
    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer().getServer() == null || !Config.general.isEnabled(event.getPlayer().getServer()))
            return;

        Team playerTeam = TeamManager.INSTANCE.getPlayerTeam(event.getPlayer().getUUID());
        if (playerTeam == null)
            return;

        // Handle custom respawn logic
        IslandsManager islandsManager = IslandsManager.get();
        if (event.getPlayer().getSleepingPos().isPresent()) {
            // If the player already has a sleeping position, just use it and return.
            return;
        }

        Island island = islandsManager.getIsland(playerTeam).orElse(islandsManager.getLobby().orElse(null));
        if (island == null) {
            FTBTeamIslands.LOGGER.info("No island or lobby found!");
            return; // Default back to the stand handling. No lobby or Island found!
        }

        island.teleportPlayerTo((ServerPlayer) event.getPlayer(), event.getPlayer().getServer());
    }
}
