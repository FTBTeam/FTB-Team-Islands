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
        if (!IslandsManager.isEnabled(server)) {
            return;
        }

        ServerLevel level = team.manager.getServer().getLevel(IslandsManager.getTargetIsland());
        IslandsManager islandsManager = IslandsManager.get();
        if (islandsManager == null || level == null) {
            return;
        }

        // If we're a server, attempt to spawn a lobby
        // Bypass lobby spawning if we're spawning into a single player world and there is only a single island
        if (!islandsManager.getLobby().isPresent()) {
            IslandSpawner.spawnLobby(level, event.getCreator());
        }
    }

    /**
     * Clear the players inventory upon leaving and reset their spawn chunk to the lobby.
     * <p>
     * Player team -> Party team = Left player team
     * If all party team members leave, party team is removed.
     */
    public static void onChangedTeamEvent(PlayerChangedTeamEvent event) {
        Team team = event.getTeam();
        MinecraftServer server = team.manager.getServer();

        // Don't run if the mod is disabled
        if (!IslandsManager.isEnabled(server)) {
            return;
        }

        // If the player left their own team and their team has an island, mark their old island as unused.
        // NOTE: this isn't used atm due to the PARTY requirement.
        Optional<Team> previousTeam = event.getPreviousTeam();
        previousTeam.ifPresent(e -> {
            if (e.getType() != TeamType.PLAYER || e.getMembers().size() > 0 || !IslandsManager.get().getIsland(e).isPresent()) {
                return;
            }

            IslandsManager.get().markUnclaimed(e.getId());
        });

        // Don't act if this is their first team.
        ServerPlayer player = event.getPlayer();
        if (!previousTeam.isPresent() || player == null) {
            return;
        }

        // Clear the inventory if the player leaves their team (island)
        if (Config.general.clearInvWhenTeamLeft.get()) {
            player.inventory.clearContent();
        }

        if (previousTeam.get().getType() == TeamType.PARTY && event.getTeam().getType() == TeamType.PLAYER) {
            IslandsManager.get().getLobby().ifPresent(e -> e.teleportPlayerTo(player, server));
        }
    }

    /**
     * Upon deletion, validate against any existing islands and mark them as unused.
     */
    public static void onTeamDeleted(TeamDeletedEvent event) {
        Team team = event.getTeam();
        if (!IslandsManager.isEnabled(team.manager.getServer())) {
            return;
        }

        IslandsManager.get().markUnclaimed(team.getId());
    }

    /**
     * Upon player death, check if they have a valid island or a valid bed, respawn the player
     * on that island or on that bed. Do basic safety checking as well, lava etc.
     */
    @SubscribeEvent
    public static void onPlayerDeath(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getPlayer().getServer() == null || !IslandsManager.isEnabled(event.getPlayer().getServer())) {
            return;
        }

        Team playerTeam = TeamManager.INSTANCE.getPlayerTeam(event.getPlayer().getUUID());
        if (playerTeam == null) {
            return;
        }

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
