package dev.ftb.mods.ftbteamislands;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final General general;
    public static final Lobby lobby;
    public static final Islands islands;
    public static final ForgeConfigSpec COMMON_CONFIG;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER;

    // Don't judge me! It's because of auto formatting moving the order around!
    static {
        COMMON_BUILDER = new ForgeConfigSpec.Builder();

        general = new General();
        lobby = new Lobby();
        islands = new Islands();

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static class General {
        public final ForgeConfigSpec.BooleanValue creationTimeout;
        public final ForgeConfigSpec.BooleanValue enableSinglePlayer;
        public final ForgeConfigSpec.BooleanValue forceSinglePlayerIslandSelection;
        public final ForgeConfigSpec.BooleanValue enableMultiplayer;
        public final ForgeConfigSpec.BooleanValue enableMyIslandCommand;
        public final ForgeConfigSpec.BooleanValue clearInvWhenTeamLeft;
        public final ForgeConfigSpec.ConfigValue<String> targetIslandLevel;

        General() {
            COMMON_BUILDER.push("general");

            this.creationTimeout = COMMON_BUILDER
                .comment("Enables a 5 minute timeout on the `ftbteamislands create` command")
                .define("creationTimeout", true);

            this.enableSinglePlayer = COMMON_BUILDER.define("enableSingleplayer", true);
            this.forceSinglePlayerIslandSelection = COMMON_BUILDER.define("forceSinglePlayerIslandSelection", true);
            this.enableMultiplayer = COMMON_BUILDER.define("enableMultiplayer", true);
            this.clearInvWhenTeamLeft = COMMON_BUILDER.define("clearInvWhenTeamLeft", true);
            this.enableMyIslandCommand = COMMON_BUILDER.define("enableMyIslandCommand", true);
            this.targetIslandLevel = COMMON_BUILDER.define("targetIslandLevel", "minecraft:overworld");

            COMMON_BUILDER.pop();
        }
    }

    public static class Lobby {
        public final ForgeConfigSpec.BooleanValue autoTeleportToIsland;
        public final ForgeConfigSpec.ConfigValue<String> lobbyIslandFile;

        Lobby() {
            COMMON_BUILDER.push("lobby");

            this.autoTeleportToIsland = COMMON_BUILDER
                .comment("Auto-teleports player to their island once they join a team.")
                .define("autoTeleportToIsland", true);

            this.lobbyIslandFile = COMMON_BUILDER
                .comment("The lobby island spawned automatically on servers.", "Must be resource location and within the structures folder of data")
                .define("lobbyStructureFile", "ftbteamislands:default_lobby");

            COMMON_BUILDER.pop();
        }
    }

    public static class Islands {
        public final ForgeConfigSpec.IntValue height;
        public final ForgeConfigSpec.IntValue autoClaimChunkRadius;
        public final ForgeConfigSpec.BooleanValue selectIslands;
        public final ForgeConfigSpec.IntValue distanceBetweenIslands;
        public final ForgeConfigSpec.ConfigValue<String> defaultIslandResource;
        public final ForgeConfigSpec.IntValue defaultIslandResourceYOffset;

        Islands() {
            COMMON_BUILDER.push("islands");

            this.height = COMMON_BUILDER
                .comment("Height at which the islands will generate.", "-1 = auto, on top of highest block in world")
                .defineInRange("height", 80, -1, 255);

            this.autoClaimChunkRadius = COMMON_BUILDER
                .comment("Radius of the chunks to automatically claim if FTB Chunks is installed.", "-1 = disabled", "0 = 1x1", "1 = 3x3", "4 = 9x9")
                .defineInRange("autoClaimChunkRadius", 4, -1, 100);

            this.selectIslands = COMMON_BUILDER
                .comment("Allow selection of the island type, if set to false, then islands will be randomized.")
                .define("selectIslands", true);

            this.distanceBetweenIslands = COMMON_BUILDER
                .comment("Distance put between new islands in regions, 1 being a single region")
                .defineInRange("distanceBetweenIslandsInRegions", 3, 3, 100);

            this.defaultIslandResource = COMMON_BUILDER
                .comment("The default island.", "Must be resource location and within the structures folder of data")
                .define("defaultIslands", "ftbteamislands:teamislands_island");

            this.defaultIslandResourceYOffset = COMMON_BUILDER
                .comment("The default islands spawning Y offset")
                .defineInRange("defaultIslandResourceYOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

            COMMON_BUILDER.pop();
        }
    }
}
