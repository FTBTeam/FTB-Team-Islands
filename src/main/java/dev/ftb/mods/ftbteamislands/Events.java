package dev.ftb.mods.ftbteamislands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandSpawner;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.*;
import dev.ftb.mods.ftbteams.event.PlayerChangedTeamEvent;
import dev.ftb.mods.ftbteams.event.TeamCreatedEvent;
import dev.ftb.mods.ftbteams.event.TeamEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = FTBTeamIslands.MOD_ID)
public class Events {
    /**
     * If the player is logging in and they've been kicked from a team, we will check their flag and
     * remove their inventory if we need to then reset the flag.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerTeam internalPlayerTeam = TeamManager.INSTANCE.getInternalPlayerTeam(event.getPlayer().getUUID());
        if (internalPlayerTeam == null) {
            return;
        }

        boolean removeInventory = internalPlayerTeam.getExtraData().getBoolean("removeInventory");
        if (removeInventory) {
            event.getPlayer().getInventory().clearContent();
            internalPlayerTeam.getExtraData().putBoolean("removeInventory", false);
            internalPlayerTeam.save();
        }
    }

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
        if (!islandsManager.getLobby().isPresent() && (server.isDedicatedServer() || (IslandsManager.getAvailableIslands().size() > 0 && ClientHandler.selectedIsland == null && IslandsManager.get().getIslandsEverCreated() == 1))) {
            IslandSpawner.spawnLobby(level, event.getCreator());
        }

        // If single player and no prebuilts and no islands have been created
        if (!server.isDedicatedServer() && IslandsManager.get().getIslandsEverCreated() == 1 && team.getType() == TeamType.PLAYER) {
            try {
                Pair<Integer, PartyTeam> partyTeam = TeamManager.INSTANCE.createParty(event.getCreator(), "");

                // If the player has created the world using the gui, spawn that island by default.
                if (ClientHandler.selectedIsland != null) {
                    IslandSpawner.spawnIsland(
                        ClientHandler.selectedIsland.getStructureFileLocation(),
                        server.getLevel(IslandsManager.getTargetIsland()),
                        partyTeam.getValue(),
                        event.getCreator(),
                        server,
                        ClientHandler.selectedIsland.yOffset()
                    );
                    ClientHandler.selectedIsland = null;
                } else if (IslandsManager.getAvailableIslands().size() == 0) {
                    // Logically this shouldn't be able to happen but you never know who might override our override.
                    // Only spawn a truely default island on single player spawn if the selected island is null and there is no prebuilt islands
                    IslandSpawner.spawnIsland(
                        new ResourceLocation(Config.islands.defaultIslandResource.get()),
                        server.getLevel(IslandsManager.getTargetIsland()),
                        partyTeam.getValue(),
                        event.getCreator(),
                        server,
                        Config.islands.defaultIslandResourceYOffset.get()
                    );
                }
            } catch (CommandSyntaxException ignored) {
            }
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

        //        // If the player left their own team and their team has an island, mark their old island as unused.
        //        // NOTE: this isn't used atm due to the PARTY requirement.
        Optional<Team> previousTeam = event.getPreviousTeam();
        //        previousTeam.ifPresent(e -> {
        //            IslandsManager.get().getIsland(e).ifPresent(island -> MinecraftForge.EVENT_BUS.post(new FTBTeamIslandsEvents.IslandLeft(e, island, event.getPlayer())));
        //            if (e.getType() != TeamType.PLAYER || e.getMembers().size() > 0 || !IslandsManager.get().getIsland(e).isPresent()) {
        //                FTBTeamIslands.LOGGER.warn("FAILED TO MARK UNUSED WITH {} {} {}", e.getType() != TeamType.PLAYER, e.getMembers().size() > 0, !IslandsManager.get().getIsland(e).isPresent());
        //                return;
        //            }
        //
        //            FTBTeamIslands.LOGGER.warn("REMOVING CLAIM UUID {}", e.getId());
        //            IslandsManager.get().markUnclaimed(e.getId());
        //        });

        // Don't act if this is their first team.
        ServerPlayer player = event.getPlayer();
        if (!previousTeam.isPresent() || player == null) {
            if (player == null) {
                // Flag the player so we can remove inventory when the join the game again.
                PlayerTeam internalPlayerTeam = TeamManager.INSTANCE.getInternalPlayerTeam(event.getPlayerId());
                internalPlayerTeam.getExtraData().putBoolean("removeInventory", true);
                internalPlayerTeam.save();
            }
            return;
        }

        // Clear the inventory if the player leaves their team (island)
        if (Config.general.clearInvWhenTeamLeft.get()) {
            player.getInventory().clearContent();
        }

        if (previousTeam.get().getType() == TeamType.PARTY && event.getTeam().getType() == TeamType.PLAYER) {
            IslandsManager.get().getLobby().ifPresent(e -> e.teleportPlayerTo(player, server));
        }
    }

    /**
     * Upon deletion, validate against any existing islands and mark them as unused.
     */
    public static void onTeamDeleted(TeamEvent event) {
        Team team = event.getTeam();
        if (!IslandsManager.isEnabled(team.manager.getServer())) {
            return;
        }

        IslandsManager.get().getIsland(team).ifPresent(island -> MinecraftForge.EVENT_BUS.post(new FTBTeamIslandsEvents.IslandMarkForDeletion(team, island)));
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
        if (((ServerPlayer) event.getPlayer()).getRespawnPosition() != null) {
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
