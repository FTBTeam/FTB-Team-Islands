package com.feed_the_beast.mods.teamislands;

import com.feed_the_beast.mods.ftbteams.data.Team;
import com.feed_the_beast.mods.ftbteams.event.PlayerChangedTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamCreatedEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import com.feed_the_beast.mods.teamislands.islands.Island;
import com.feed_the_beast.mods.teamislands.islands.IslandsSave;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = TeamIslands.MOD_ID)
public class Events {
    /**
     * Upon a player joining a team, we assume that we're running
     */
    public static void onTeamCreated(TeamCreatedEvent event) {
        Team team = event.getTeam();
        MinecraftServer server = team.manager.getServer();

        if (!Config.general.isEnabled(server))
            return;

        TeamIslands.LOGGER.info("Player joined Team");

        // Single player logic
        IslandsSave islandsSave = IslandsSave.get(Objects.requireNonNull(event.getTeam().getOwnerPlayer()).level);
        Optional<Island> island = islandsSave.getIsland(team);

//        server.getStructureManager();

//        if (!server.isDedicatedServer()) {
//            if (island.isPresent()) {
//                event.getPlayer().tel;
//            }
//            return;
//        }

        // MP logic
    }

    /**
     * Clear the players inventory upon leaving and reset their spawn chunk to the lobby.
     */
    public static void onChangedTeamEvent(PlayerChangedTeamEvent event) {
        if (!Config.general.isEnabled(event.getTeam().manager.getServer()))
            return;

        TeamIslands.LOGGER.info("Player left team");
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
    }
}
