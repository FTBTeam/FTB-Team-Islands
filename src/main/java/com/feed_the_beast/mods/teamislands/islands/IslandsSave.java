package com.feed_the_beast.mods.teamislands.islands;

import com.feed_the_beast.mods.ftbteams.data.Team;
import com.feed_the_beast.mods.teamislands.Config;
import com.feed_the_beast.mods.teamislands.TeamIslands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class IslandsSave extends SavedData {
    private static final String SAVE_NAME = TeamIslands.MOD_ID + "_islandsave";

    private HashMap<UUID, Island> islands = new HashMap<>();

    private IslandsSave() {
        super(SAVE_NAME);
    }

    public static IslandsSave get(Level level) {
        return ((ServerLevel) level).getDataStorage().computeIfAbsent(IslandsSave::new, SAVE_NAME);
    }

    public boolean registerIsland(Team team, Island pos) {
        return true;
    }

    public Optional<Island> getIsland(Team team) {
        Island island = this.islands.get(team.getId());

        if (island == null) {
            int index = islands.size() + 1;
            int distanceInRegions = Config.islands.distanceBetweenIslands.get();

            ChunkPos spawnPos = new ChunkPos(256 + ((index * distanceInRegions * 512) % 1024), 256 + ((index * distanceInRegions * 512) / 1024));
            return new Island(
                spawnPos,
                spawnPos.getWorldPosition(),

            );
        }
    }

    public boolean markUnclaimed(Island pos) {
        return true;
    }

    public BlockPos getBestPosForIsland() {
        return BlockPos.ZERO;
    }

    @Override
    public void load(CompoundTag compound) {
        if (compound.contains("islands")) {
            ListTag islands = compound.getList("islands", Constants.NBT.TAG_COMPOUND);
            islands.forEach(island -> this.islands.put(((CompoundTag) island).getUUID("key"), Island.read(((CompoundTag) island).getCompound("island"))));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        if (this.islands.size() > 0) {
            ListTag list = new ListTag();

            for (Map.Entry<UUID, Island> island : this.islands.entrySet()) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("key", island.getKey());
                tag.put("island", island.getValue().write());

                list.add(tag);
            }

            compound.put("islands", list);
        }
        return compound;
    }
}
