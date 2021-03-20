package com.feed_the_beast.mods.teamislands.islands;

import com.feed_the_beast.mods.ftbteams.api.Team;
import com.feed_the_beast.mods.teamislands.TeamIslands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class IslandsSave extends SavedData {
    private static final String SAVE_NAME = TeamIslands.MOD_NAME + "_islandsave";

    public Set<Island> islands = new HashSet<>();

    private IslandsSave() {
        super(SAVE_NAME);
    }

    public static IslandsSave get(Level level) {
        return ((ServerLevel) level).getDataStorage().computeIfAbsent(IslandsSave::new, SAVE_NAME);
    }

    public boolean registerIsland(Team team, BlockPos pos) {
        return true;
    }

    public boolean markUnclaimed(BlockPos pos) {
        return true;
    }

    public BlockPos getBestPosForIsland() {
        return BlockPos.ZERO;
    }

    @Override
    public void load(CompoundTag compound) {
        if (compound.contains("islands")) {
            ListTag islands = compound.getList("islands", Constants.NBT.TAG_COMPOUND);
            islands.forEach(island -> this.islands.add(Island.read((CompoundTag) island)));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        if (this.islands.size() > 0) {
            ListTag list = new ListTag();
            this.islands.forEach(island -> list.add(island.write()));
            compound.put("islands", list);
        }
        return compound;
    }
}
