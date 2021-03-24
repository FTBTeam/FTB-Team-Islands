package dev.ftb.mods.ftbteamislands.islands;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.UUID;

public class Island {
    public ChunkPos pos;
    public BlockPos spawnPos;
    public String templateId;

    @Nullable
    public UUID creator;

    public boolean spawned;
    public boolean active; // false = No one owns and is unclaimed

    public Island(ChunkPos pos, BlockPos spawnPos, String templateId, UUID creator) {
        this(pos, spawnPos, templateId, creator, false, true);
    }

    public Island(ChunkPos pos, BlockPos spawnPos, String templateId, @Nullable UUID creator, boolean spawned, boolean active) {
        this.pos = pos;
        this.spawnPos = spawnPos;
        this.templateId = templateId;
        this.creator = creator;
        this.spawned = spawned;
        this.active = active;
    }

    public BlockPos getSpawnPos() {
        return spawnPos;
    }

    @Nullable
    public UUID getCreator() {
        return creator;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public boolean isActive() {
        return active;
    }

    // TODO: add safe spawning logic
    public void teleportPlayerTo(ServerPlayer player, MinecraftServer server) {
        ServerLevel level = server.getLevel(IslandsManager.getTargetIsland());

        if (player.level.dimension() != IslandsManager.getTargetIsland())
            player.changeDimension(level);

        BlockPos spawnPos = this.getSpawnPos();
        player.teleportTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
    }

    public CompoundTag write() {
        CompoundTag compound = new CompoundTag();
        compound.put("spawnPos", NbtUtils.writeBlockPos(this.spawnPos));
        compound.putLong("chunkPos", this.pos.toLong());
        compound.putString("templateId", this.templateId);
        if (this.creator != null) {
            compound.putUUID("creator", this.creator);
        }
        compound.putBoolean("spawned", this.spawned);
        compound.putBoolean("active", this.active);
        return compound;
    }

    public static Island read(CompoundTag compound) {
        return new Island(
            new ChunkPos(compound.getLong("chunkPos")),
            NbtUtils.readBlockPos(compound.getCompound("spawnPos")),
            compound.getString("templateId"),
            compound.contains("creator") ? compound.getUUID("creator") : null,
            compound.getBoolean("spawned"),
            compound.getBoolean("active")
        );
    }
}
