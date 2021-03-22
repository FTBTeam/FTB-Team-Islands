package dev.ftb.mods.ftbteamislands.islands;

import com.feed_the_beast.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class IslandsManager extends SavedData {
    private static final String SAVE_NAME = FTBTeamIslands.MOD_ID + "_islandsave";

    private final HashMap<UUID, Island> islands = new HashMap<>();
    private final Set<SpawnableIsland> availableIslands = new HashSet<>();

    @Nullable
    private Island lobby;

    private IslandsManager() {
        super(SAVE_NAME);
    }

    public static IslandsManager get(Level level) {
        return ((ServerLevel) level).getDataStorage().computeIfAbsent(IslandsManager::new, SAVE_NAME);
    }

    public boolean registerIsland(Team team, Island island) {
        this.islands.put(team.getId(), island);
        return true;
    }

    public Optional<Island> getIsland(Team team) {
        Island island = this.islands.get(team.getId());
        return island == null ? Optional.empty() : Optional.of(island);
    }

    /**
     * Marks and island as inactive / unclaimed and removes the creator from the island.
     */
    public void markUnclaimed(UUID islandId) {
        Island island = this.islands.get(islandId);
        island.creator = null;
        island.active = false;
    }

    public void removeIsland(UUID islandId) {
        this.islands.remove(islandId);
    }

    /**
     * Find unclaimed islands by their active flag
     *
     * @return a set of island UUID's
     */
    public Set<UUID> getUnclaimedIslands() {
        return this.islands.entrySet().stream()
            .filter(island -> !island.getValue().isActive())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    @Nullable
    public Island getLobby() {
        return lobby;
    }

    public void setLobby(@Nullable Island lobby) {
        this.lobby = lobby;
    }

    @Override
    public void load(CompoundTag compound) {
        if (compound.contains("islands")) {
            ListTag islands = compound.getList("islands", Constants.NBT.TAG_COMPOUND);
            islands.forEach(island -> this.islands.put(((CompoundTag) island).getUUID("key"), Island.read(((CompoundTag) island).getCompound("island"))));
        }

        if (compound.contains("lobby")) {
            this.lobby = Island.read(compound.getCompound("lobby"));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        if (this.lobby != null)
            compound.put("lobby", this.lobby.write());

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

    public HashMap<UUID, Island> getIslands() {
        return this.islands;
    }

    public Set<SpawnableIsland> getAvailableIslands() {
        return availableIslands;
    }

    public static ResourceKey<Level> getTargetIsland() {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Config.general.targetIslandLevel.get()));
    }
}
