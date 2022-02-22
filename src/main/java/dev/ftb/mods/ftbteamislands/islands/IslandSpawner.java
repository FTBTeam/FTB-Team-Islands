package dev.ftb.mods.ftbteamislands.islands;

import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.FTBTeamIslandsEvents;
import dev.ftb.mods.ftbteamislands.intergration.FTBChunks;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;

public class IslandSpawner {

    /**
     * Island spawner logic for the lobby
     */
    public static void spawnLobby(ServerLevel level, ServerPlayer creator) {
        boolean result = new Worker(level, new ResourceLocation(Config.lobby.lobbyIslandFile.get()))
            .setSpawnAt(new BlockPos(256, Config.islands.height.get(), 256))
            .setGlobalSpawns(true)
            .claimChunks(false)
            .onCreation(island -> IslandsManager.get().setLobby(island))
            .create(level.getServer(), creator);

        if (!result) {
            FTBTeamIslands.LOGGER.error("Failed to spawn island!");
        }
    }

    /**
     * Island spawner logic for single player initial setup
     */
    public static void spawnIsland(Worker worker, Team team, ServerPlayer player, MinecraftServer server, int yOffset) {
        if (player == null) {
            return;
        }

        int index = Math.max(1, IslandsManager.get().getIslandsEverCreated());
        int distanceInRegions = Config.islands.distanceBetweenIslands.get();

        Pair<Integer, Integer> spiralLoc = calculateSpiral(index);

        boolean result = worker
            .setSpawnAt(new BlockPos(256 + spiralLoc.getLeft() * distanceInRegions * 512, Config.islands.height.get(), 256 + spiralLoc.getRight() * distanceInRegions * 512))
            .setGlobalSpawns(!IslandsManager.get().getLobby().isPresent() && IslandsManager.get().getIslands().size() == 0)
            .claimChunks(true)
            .yOffset(yOffset)
            .onCreation((island -> {
                IslandsManager.get().registerIsland(team, island);
                MinecraftForge.EVENT_BUS.post(new FTBTeamIslandsEvents.IslandJoined(team, island, player));
            }))
            .create(server, player);

        if (!result) {
            FTBTeamIslands.LOGGER.error("Failed to spawn island!");
        }
    }

    public static void spawnIsland(String islandName, ServerLevel level, Team team, ServerPlayer player, MinecraftServer server, int yOffset) {
        try {
            InputStream file = new FileInputStream(server.getServerDirectory().getAbsolutePath() + IslandsManager.PREBUILT_ISLANDS_PATH + "structures/" + islandName);
            CompoundTag compoundTag = NbtIo.readCompressed(file);
            if (compoundTag == null) {
                FTBTeamIslands.LOGGER.error("Failed to read `{}` island", islandName);
                return;
            }

            spawnIsland(new Worker(level, compoundTag), team, player, server, yOffset);
        } catch (IOException e) {
            FTBTeamIslands.LOGGER.error("Failed to find `{}` island in the prebuilt structures folder `{}/structures`", islandName, IslandsManager.PREBUILT_ISLANDS_PATH);
        }
    }

    public static void spawnIsland(ResourceLocation islandName, ServerLevel level, Team team, ServerPlayer player, MinecraftServer server, int yOffset) {
        spawnIsland(new Worker(level, islandName), team, player, server, yOffset);
    }

    // Slightly modified version of https://stackoverflow.com/a/45333503/6543961
    private static Pair<Integer, Integer> calculateSpiral(int index) {
        if (index == 0) {
            return Pair.of(0, 0);
        }

        // current position (x, z) and how much of current segment we passed
        int x = 0, z = 0;

        int dx = 0, dz = 1;
        int segmentLength = 1, segmentPassed = 0;

        for (int n = 0; n < index; n++) {
            x += dx;
            z += dz;
            segmentPassed++;

            if (segmentPassed == segmentLength) {
                segmentPassed = 0;

                // 'rotate' directions
                int buffer = dz;
                dz = -dx;
                dx = buffer;

                // increase segment length if necessary
                if (dx == 0) {
                    segmentLength++;
                }
            }
        }

        return Pair.of(x, z);
    }

    public static class Worker {
        private final StructureTemplate template;
        private final ServerLevel level;

        private BlockPos spawnAt = BlockPos.ZERO;
        private boolean setsGlobalSpawn = false;
        private boolean claimChunks = true;
        private Consumer<Island> onCreation = island -> {
        };
        private int yOffset = 0;

        public Worker(ServerLevel level, ResourceLocation nbtLocation) {
            this.template = level.getStructureManager().get(nbtLocation).get();
            this.level = level;
        }

        public Worker(ServerLevel level, CompoundTag templateCompound) {
            this.template = level.getStructureManager().readStructure(templateCompound);
            this.level = level;
        }

        public Worker setSpawnAt(BlockPos pos) {
            this.spawnAt = pos;
            return this;
        }

        public Worker setGlobalSpawns(boolean setGlobalSpawn) {
            this.setsGlobalSpawn = setGlobalSpawn;
            return this;
        }

        public Worker yOffset(int yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Worker claimChunks(boolean claimChunks) {
            this.claimChunks = claimChunks;
            return this;
        }

        public Worker onCreation(Consumer<Island> consumer) {
            this.onCreation = consumer;
            return this;
        }

        public boolean create(MinecraftServer server, ServerPlayer player) {
            // Fail if the template failed to resolve
            if (this.template == null) {
                return false;
            }

            // Create a ZERO based bounding box and an in-world bounding box for future selections
            BoundingBox boundingBox = this.template.getBoundingBox(new StructurePlaceSettings(), BlockPos.ZERO);
            BoundingBox inWorldBoundingBox = this.template.getBoundingBox(new StructurePlaceSettings(), this.spawnAt.offset(-boundingBox.maxX() / 2, this.yOffset, -boundingBox.maxZ() / 2));

            // Spawn the template in the world at the offered block pos.

            BlockPos offset = this.spawnAt.offset(-boundingBox.maxX() / 2, this.yOffset, -boundingBox.maxZ() / 2);
            this.template.placeInWorld(this.level, offset, offset, new StructurePlaceSettings(), this.level.getRandom(), 2);

            // TODO: find a better way of doing this!
            // Find the spawn pos and remove any remaining structure blocks
            BlockPos playerSpawnPoint = this.spawnAt.above(2);
            Iterator<BlockPos> iterator = BlockPos.betweenClosedStream(inWorldBoundingBox).iterator();
            while (iterator.hasNext()) {
                BlockPos next = iterator.next();
                if (this.level.getBlockState(next).getBlock() instanceof StructureBlock) {
                    BlockEntity blockEntity = this.level.getBlockEntity(next);
                    if (blockEntity instanceof StructureBlockEntity) {
                        CompoundTag tag = blockEntity.saveWithFullMetadata();

                        if (tag.getString("metadata").startsWith("SPAWN_POINT")) {
                            playerSpawnPoint = next.mutable();
                            this.level.removeBlock(next, false);

                            // Fix dirt if the structure block removed it's grass
                            if (this.level.getBlockState(next.below()).getBlock() == Blocks.DIRT) {
                                this.level.setBlock(next.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                            }
                        }
                    } else {
                        this.level.removeBlock(next, false);
                    }
                }
            }

            Island island = new Island(
                new ChunkPos(this.spawnAt),
                playerSpawnPoint,
                null,
                true,
                true
            );

            MinecraftForge.EVENT_BUS.post(new FTBTeamIslandsEvents.IslandCreated(TeamManager.INSTANCE.getPlayerTeam(player), island));

            // Teleport the player to the new island.
            island.teleportPlayerTo(player, server);
            MinecraftForge.EVENT_BUS.post(new FTBTeamIslandsEvents.FirstTeleportTo(TeamManager.INSTANCE.getPlayerTeam(player), island, player));

            // Set the global spawn
            if (this.setsGlobalSpawn) {
                this.level.setDefaultSpawnPos(island.getSpawnPos(), 90f);
            }

            // If chunks is loaded and the chunks can be claimed, try and claim them.
            if (ModList.get().isLoaded("ftbchunks") && this.claimChunks && Config.islands.autoClaimChunkRadius.get() != -1) {
                FTBChunks.claimChunks(player, player.getLevel(), island.pos);
            }

            // Return true and call a finishing up consumer
            this.onCreation.accept(island);
            return true;
        }
    }
}
