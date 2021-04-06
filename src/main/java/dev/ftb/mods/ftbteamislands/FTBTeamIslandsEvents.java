package dev.ftb.mods.ftbteamislands;

import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

public class FTBTeamIslandsEvents {

    private static class IslandEvent extends Event {
        public Team team;
        public Island island;

        public IslandEvent(Team team, Island island) {
            this.team = team;
            this.island = island;
        }
    }

    public static class IslandJoined extends IslandEvent {
        ServerPlayer player;

        public IslandJoined(Team team, Island island, ServerPlayer player) {
            super(team, island);
            this.player = player;
        }
    }

    public static class IslandLeft extends IslandJoined {
        public IslandLeft(Team team, Island island, ServerPlayer player) {
            super(team, island, player);
        }
    }

    public static class FirstTeleportTo extends IslandJoined {
        public FirstTeleportTo(Team team, Island island, ServerPlayer player) {
            super(team, island, player);
        }
    }

    public static class IslandMarkForDeletion extends IslandEvent {
        public IslandMarkForDeletion(Team team, Island island) {
            super(team, island);
        }
    }

    public static class IslandCreated extends IslandEvent {
        public IslandCreated(Team team, Island island) {
            super(team, island);
        }
    }
}