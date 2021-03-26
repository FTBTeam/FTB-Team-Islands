package dev.ftb.mods.ftbteamislands;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final General general = new General();
    public static final Lobby lobby = new Lobby();
    public static final Islands islands = new Islands();
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();

    public static class General {
        public final ForgeConfigSpec.BooleanValue enableSinglePlayer;
        public final ForgeConfigSpec.BooleanValue enableMultiplayer;
        public final ForgeConfigSpec.BooleanValue enableMyIslandCommand;
        public final ForgeConfigSpec.BooleanValue clearInvWhenTeamLeft;
        public final ForgeConfigSpec.ConfigValue<String> targetIslandLevel;

        General() {
            SERVER_BUILDER.push("general");

            this.enableSinglePlayer = SERVER_BUILDER.define("enableSingleplayer", false);
            this.enableMultiplayer = SERVER_BUILDER.define("enableMultiplayer", true);
            this.clearInvWhenTeamLeft = SERVER_BUILDER.define("clearInvWhenTeamLeft", true);
            this.enableMyIslandCommand = SERVER_BUILDER.define("enableMyIslandCommand", true);
            this.targetIslandLevel = SERVER_BUILDER.define("targetIslandLevel", "minecraft:overworld");

            SERVER_BUILDER.pop();
        }
    }

    public static class Lobby {
        public final ForgeConfigSpec.BooleanValue autoTeleportToIsland;
        public final ForgeConfigSpec.ConfigValue<String> lobbyIslandFile;

        Lobby() {
            SERVER_BUILDER.push("lobby");

            this.autoTeleportToIsland = SERVER_BUILDER
                .comment("Auto-teleports player to their island once they join a team.")
                .define("autoTeleportToIsland", true);

            this.lobbyIslandFile = SERVER_BUILDER
                .comment("The lobby island spawned automatically on server ")
                .define("lobbyStructureFile", "default_lobby");

            SERVER_BUILDER.pop();
        }
    }

    public static class Islands {
        public final ForgeConfigSpec.IntValue height;
        public final ForgeConfigSpec.IntValue autoClaimChunkRadius;
        public final ForgeConfigSpec.BooleanValue selectIslands;
        public final ForgeConfigSpec.IntValue distanceBetweenIslands;

        Islands() {
            SERVER_BUILDER.push("general");

            this.height = SERVER_BUILDER
                .comment("Height at which the islands will generate.", "-1 = auto, on top of highest block in world")
                .defineInRange("height", 80, -1, 255);

            this.autoClaimChunkRadius = SERVER_BUILDER
                .comment("Radius of the chunks to automatically claim if FTBUtilities is installed.", "-1 = disabled", "0 = 1x1", "1 = 3x3", "4 = 9x9")
                .defineInRange("autoClaimChunkRadius", 4, -1, 100);

            this.selectIslands = SERVER_BUILDER
                .comment("Allow selection of the island type, if set to false, then islands will be randomized.")
                .define("selectIslands", true);

            this.distanceBetweenIslands = SERVER_BUILDER
                .comment("Distance put between new islands in regions, 1 being a single region")
                .defineInRange("distanceBetweenIslandsInRegions", 3, 3, 100);

            SERVER_BUILDER.pop();
        }
    }
}
