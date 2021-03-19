package com.feed_the_beast.mods.teamislands;

import com.feed_the_beast.mods.ftbteams.event.PlayerJoinedTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.PlayerLeftTeamEvent;
import com.feed_the_beast.mods.ftbteams.event.TeamDeletedEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TeamIslands.MOD_ID)
public class Events {
    /**
     * Upon a player joining a team, we assume that we're running
     */
    @SubscribeEvent
    public static void onPlayerJoinTeam(PlayerJoinedTeamEvent event) {
        if (!Config.general.isEnabled(event.getTeam().getManager().server))
            return;

        TeamIslands.LOGGER.info("Player joined Team");

        // Check for existing island
        if (true) {
//            Template template = event.getPlayer().getServer().getWorldPath(LevelResource.LEVEL_DATA_FILE).getSaveHandler().getStructureTemplateManager().getTemplate(universe.server, new ResourceLocation(TeamIslands.MOD_ID, "teamislands_island"));
        }
    }

    /**
     * Clear the players inventory upon leaving and reset their spawn chunk to the lobby.
     */
    @SubscribeEvent
    public static void onPlayerLeftTeam(PlayerLeftTeamEvent event) {
        if (!Config.general.isEnabled(event.getTeam().getManager().server))
            return;

        TeamIslands.LOGGER.info("Player left team");
    }

    /**
     * Upon deletion, validate against any existing islands and mark them as unused.
     */
    @SubscribeEvent
    public static void onTeamDeleted(TeamDeletedEvent event) {
        if (!Config.general.isEnabled(event.getTeam().getManager().server))
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
        System.out.println(event.getPlayer().getServer().getStructureManager().get(new ResourceLocation(TeamIslands.MOD_ID, "teamislands_island")));
    }
}
