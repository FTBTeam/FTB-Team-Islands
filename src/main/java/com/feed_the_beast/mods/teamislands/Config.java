package com.feed_the_beast.mods.teamislands;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final General general = new General();
    public static final Lobby lobby = new Lobby();
    public static final Islands islands = new Islands();

    public static class General {
        public final ForgeConfigSpec.BooleanValue enableSinglePlayer;
        public final ForgeConfigSpec.BooleanValue enableMultiplayer;
        public final ForgeConfigSpec.BooleanValue enableMyIslandCommand;
        public final ForgeConfigSpec.BooleanValue clearInvWhenTeamLeft;
//        public final ForgeConfigSpec.ConfigValue<String> voidWorldTypeId;

        General() {
            SERVER_BUILDER.push("general");

            enableSinglePlayer = SERVER_BUILDER.define("enableSingleplayer", false);
            enableMultiplayer = SERVER_BUILDER.define("enableMultiplayer", true);
            clearInvWhenTeamLeft = SERVER_BUILDER.define("clearInvWhenTeamLeft", true);
            enableMyIslandCommand = SERVER_BUILDER.define("enableMyIslandCommand", true);

//            voidWorldTypeId = SERVER_BUILDER.define("voidWorldTypeId", "void");

            SERVER_BUILDER.pop();
        }

        /**
         * TODO: Make sure this works with the new config system
         */
        public boolean isEnabled(MinecraftServer server) {
            return server.isDedicatedServer() ? this.enableMultiplayer.get() : this.enableMultiplayer.get();
        }
    }

    public static class Lobby {
        public final ForgeConfigSpec.BooleanValue autoTeleportToIsland;

        Lobby() {
            SERVER_BUILDER.push("lobby");

            autoTeleportToIsland = SERVER_BUILDER
                .comment("Auto-teleports player to their island once they join a team.")
                .define("autoTeleportToIsland", true);

            SERVER_BUILDER.pop();
        }
    }

    public static class Islands {
        public final ForgeConfigSpec.IntValue height;
//        public final ForgeConfigSpec.ConfigValue<String[]> structureFiles;
        public final ForgeConfigSpec.IntValue autoClaimChunkRadius;
        public final ForgeConfigSpec.BooleanValue selectIslands;
        public final ForgeConfigSpec.IntValue distanceBetweenIslands;

        Islands() {
            SERVER_BUILDER.push("general");

            height = SERVER_BUILDER
                .comment("Height at which the islands will generate.", "-1 = auto, on top of highest block in world")
                .defineInRange("height", 80, -1, 255);

//            structureFiles = SERVER_BUILDER
//                .comment("Structure files will be loaded from config/x file.", "If not set, builtin island will be used.")
//                .worldRestart()
//                .define("structureFiles", new String[]{});

            autoClaimChunkRadius = SERVER_BUILDER
                .comment("Radius of the chunks to automatically claim if FTBUtilities is installed.", "-1 = disabled", "0 = 1x1", "1 = 3x3", "4 = 9x9")
                .defineInRange("autoClaimChunkRadius", 4, -1, 100);

            selectIslands = SERVER_BUILDER
                .comment("Allow selection of the island type, if set to false, then islands will be randomized.")
                .define("selectIslands", true);

            distanceBetweenIslands = SERVER_BUILDER
                .comment("Distance put between new islands in regions, 1 being a single region")
                .defineInRange("distanceBetweenIslandsInRegions", 1, 1, 100);

            SERVER_BUILDER.pop();
        }
    }

    public static final ForgeConfigSpec SERVER_CONFIG = SERVER_BUILDER.build();
    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();
}
