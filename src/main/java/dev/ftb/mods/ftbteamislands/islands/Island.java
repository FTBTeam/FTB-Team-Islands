package dev.ftb.mods.ftbteamislands.islands;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class Island {
    public ChunkPos pos;
    public BlockPos spawnPos;
    public UUID templateId;

    @Nullable
    public UUID creator;

    public boolean spawned;
    public boolean active; // false = No one owns and is unclaimed

    public Island(ChunkPos pos, BlockPos spawnPos, String templateId, UUID creator) {
        this(pos, spawnPos, creator, false, true);
    }

    public Island(ChunkPos pos, BlockPos spawnPos, @Nullable UUID creator, boolean spawned, boolean active) {
        this.templateId = UUID.randomUUID();
        this.pos = pos;
        this.spawnPos = spawnPos;
        this.creator = creator;
        this.spawned = spawned;
        this.active = active;
    }

    public static Island read(CompoundTag compound) {
        Island island = new Island(
            new ChunkPos(compound.getLong("chunkPos")),
            NbtUtils.readBlockPos(compound.getCompound("spawnPos")),
            compound.contains("creator")
                ? compound.getUUID("creator")
                : null,
            compound.getBoolean("spawned"),
            compound.getBoolean("active")
        );

        island.templateId = compound.getUUID("templateId");
        return island;
    }

    public BlockPos getSpawnPos() {
        return this.spawnPos;
    }

    @Nullable
    public UUID getCreator() {
        return this.creator;
    }

    public boolean isSpawned() {
        return this.spawned;
    }

    public boolean isActive() {
        return this.active;
    }

    // TODO: add safe spawning logic
    public void teleportPlayerTo(ServerPlayer player, MinecraftServer server) {
        ServerLevel level = server.getLevel(IslandsManager.getTargetIsland());

        if (player.level.dimension() != IslandsManager.getTargetIsland()) {
            player.changeDimension(level);
        }

        Vec3 spawnPos = Vec3.atBottomCenterOf(new Vec3i(this.getSpawnPos().getX(), this.getSpawnPos().getY(), this.getSpawnPos().getZ()));
        player.teleportTo(spawnPos.x, spawnPos.y, spawnPos.z);
    }

    public CompoundTag write() {
        CompoundTag compound = new CompoundTag();
        compound.put("spawnPos", NbtUtils.writeBlockPos(this.spawnPos));
        compound.putLong("chunkPos", this.pos.toLong());
        compound.putUUID("templateId", this.templateId);
        if (this.creator != null) {
            compound.putUUID("creator", this.creator);
        }
        compound.putBoolean("spawned", this.spawned);
        compound.putBoolean("active", this.active);
        return compound;
    }
}
