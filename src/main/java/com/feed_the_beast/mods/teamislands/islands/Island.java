package com.feed_the_beast.mods.teamislands.islands;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class Island {
    public ChunkPos pos;
    public BlockPos spawnPos;
    public String templateId;
    public UUID creator;
    public boolean spawned;
    public boolean active; // false = No one owns and is unclaimed

    public Island(ChunkPos pos, BlockPos spawnPos, String templateId, UUID creator) {
        this(pos, spawnPos, templateId, creator, false, true);
    }

    public Island(ChunkPos pos, BlockPos spawnPos, String templateId, UUID creator, boolean spawned, boolean active) {
        this.pos = pos;
        this.spawnPos = spawnPos;
        this.templateId = templateId;
        this.creator = creator;
        this.spawned = spawned;
        this.active = active;
    }

    public CompoundTag write() {
        CompoundTag compound = new CompoundTag();
        compound.put("spawnPos", NbtUtils.writeBlockPos(this.spawnPos));
        compound.putLong("chunkPos", this.pos.toLong());
        compound.putString("templateId", this.templateId);
        compound.putUUID("creator", this.creator);
        compound.putBoolean("spawned", this.spawned);
        compound.putBoolean("active", this.active);
        return compound;
    }

    public static Island read(CompoundTag compound) {
        return new Island(
            new ChunkPos(compound.getLong("chunkPos")),
            NbtUtils.readBlockPos(compound.getCompound("spawnPos")),
            compound.getString("templateId"),
            compound.getUUID("creator"),
            compound.getBoolean("spawned"),
            compound.getBoolean("active")
        );
    }
}
