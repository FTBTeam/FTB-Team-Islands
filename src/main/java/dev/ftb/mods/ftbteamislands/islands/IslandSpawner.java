package dev.ftb.mods.ftbteamislands.islands;

import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.intergration.FTBChunks;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.fml.ModList;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

public class IslandSpawner {

    /**
     * Island spawner logic for the lobby
     */
    public static void spawnLobby(ServerLevel level, ServerPlayer creator) {
        boolean result = new Worker(level, new ResourceLocation(FTBTeamIslands.MOD_ID, Config.lobby.lobbyIslandFile.get()))
            .setSpawnAt(new BlockPos(0, Config.islands.height.get(), 0))
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
    public static void spawnIsland(Worker worker, Team team, ServerPlayer player, MinecraftServer server) {
        if (player == null) {
            return;
        }

        int index = Math.max(1, IslandsManager.get().getIslands().size());
        int distanceInRegions = Config.islands.distanceBetweenIslands.get();

        boolean result = worker
            .setSpawnAt(new BlockPos(256 + ((index * distanceInRegions * 512) % 1024), Config.islands.height.get(), 256 + ((index * distanceInRegions * 512) / 1024)))
            .setGlobalSpawns(!IslandsManager.get().getLobby().isPresent() && IslandsManager.get().getIslands().size() == 0)
            .claimChunks(true)
            .onCreation((island -> IslandsManager.get().registerIsland(team, island)))
            .create(server, player);

        if (!result) {
            FTBTeamIslands.LOGGER.error("Failed to spawn island!");
        }
    }

    public static void spawnIsland(String islandName, ServerLevel level, Team team, ServerPlayer player, MinecraftServer server) {
        try {
            InputStream file = new FileInputStream(server.getServerDirectory().getAbsolutePath() + IslandsManager.PREBUILT_ISLANDS_PATH + "structures/" + islandName);
            CompoundTag compoundTag = NbtIo.readCompressed(file);
            if (compoundTag == null) {
                FTBTeamIslands.LOGGER.error("Failed to read `{}` island", islandName);
                return;
            }

            spawnIsland(new Worker(level, compoundTag), team, player, server);
        } catch (IOException e) {
            FTBTeamIslands.LOGGER.error("Failed to find `{}` island in the prebuilt structures folder `{}/structures`", islandName, IslandsManager.PREBUILT_ISLANDS_PATH);
            e.printStackTrace();
        }
    }

    public static void spawnIsland(ResourceLocation islandName, ServerLevel level, Team team, ServerPlayer player, MinecraftServer server) {
        spawnIsland(new Worker(level, islandName), team, player, server);
    }

    public static class Worker {
        private final StructureTemplate template;
        private final ServerLevel level;

        private BlockPos spawnAt = BlockPos.ZERO;
        private boolean setsGlobalSpawn = false;
        private boolean claimChunks = true;
        private Consumer<Island> onCreation = island -> {
        };

        public Worker(ServerLevel level, ResourceLocation nbtLocation) {
            this.template = level.getStructureManager().get(nbtLocation);
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
            BoundingBox inWorldBoundingBox = this.template.getBoundingBox(new StructurePlaceSettings(), this.spawnAt.offset(-boundingBox.x1 / 2, 0, -boundingBox.z1 / 2));

            // Spawn the template in the world at the offered block pos.
            this.template.placeInWorldChunk(this.level, this.spawnAt.offset(-boundingBox.x1 / 2, 0, -boundingBox.z1 / 2), new StructurePlaceSettings(), this.level.getRandom());

            // TODO: find a better way of doing this!
            // Find the spawn pos and remove any remaining structure blocks
            BlockPos playerSpawnPoint = this.spawnAt.above(2);
            Iterator<BlockPos> iterator = BlockPos.betweenClosedStream(inWorldBoundingBox).iterator();
            while (iterator.hasNext()) {
                BlockPos next = iterator.next();
                if (this.level.getBlockState(next).getBlock() instanceof StructureBlock) {
                    BlockEntity blockEntity = this.level.getBlockEntity(next);
                    if (blockEntity instanceof StructureBlockEntity && ((StructureBlockEntity) blockEntity).getMetaData().startsWith("SPAWN_POINT")) {
                        playerSpawnPoint = next.mutable();
                    }

                    this.level.removeBlock(next, false);
                }
            }

            Island island = new Island(
                new ChunkPos(this.spawnAt),
                playerSpawnPoint,
                null,
                true,
                true
            );

            // Teleport the player to the new island.
            island.teleportPlayerTo(player, server);

            // Set the global spawn
            if (this.setsGlobalSpawn) {
                this.level.setDefaultSpawnPos(island.getSpawnPos(), 90f);
            }

            // If chunks is loaded and the chunks can be claimed, try and claim them.
            if (ModList.get().isLoaded("ftbchunks") && this.claimChunks) {
                FTBChunks.claimChunks(player, player.getLevel(), island.pos);
            }

            // Return true and call a finishing up consumer
            this.onCreation.accept(island);
            return true;
        }
    }
}
