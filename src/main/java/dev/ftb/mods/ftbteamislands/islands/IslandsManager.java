package dev.ftb.mods.ftbteamislands.islands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteams.data.Team;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class IslandsManager {
    public static final LevelResource FOLDER_NAME = new LevelResource("ftbteamislands");
    private static final String PREBUILT_ISLANDS_PATH = "/config/ftbteamislands/islands.json";
    private static IslandsManager INSTANCE;

    public final MinecraftServer server;
    private final HashMap<UUID, Island> islands = new HashMap<>();
    private final List<PrebuiltIslands> availableIslands = new ArrayList<>();
    private boolean shouldSave;

    @Nullable
    private Island lobby;

    private IslandsManager(MinecraftServer server) {
        this.server = server;
    }

    public static IslandsManager get() {
        return INSTANCE;
    }

    public static void setup(MinecraftServer server) {
        INSTANCE = new IslandsManager(server);
        INSTANCE.load();
        INSTANCE.findAndLoadPrebuilts();
    }

    /**
     * Loads and populates the availableIslands from the json
     */
    private void findAndLoadPrebuilts() {
        FTBTeamIslands.LOGGER.info("Prebuilts: Searching for prebuilt islands in {}", PREBUILT_ISLANDS_PATH);
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(PrebuiltIslands.PrebuiltIsland.class, new PrebuiltIslands.PrebuiltIsland.Deserializer())
            .create();

        System.out.println(server.getServerDirectory().getAbsolutePath());
        Type prebuiltIslandsType = new TypeToken<List<PrebuiltIslands>>(){}.getType();
        try {
            try {
                List<PrebuiltIslands> islands = gson.fromJson(new FileReader(server.getServerDirectory().getAbsolutePath() + PREBUILT_ISLANDS_PATH), prebuiltIslandsType);
                this.availableIslands.addAll(islands);

                FTBTeamIslands.LOGGER.info("Prebuilts: found {} islands in {}", this.availableIslands.stream().mapToInt(e -> e.getIslands().size()).count(), PREBUILT_ISLANDS_PATH);
            } catch (JsonIOException | JsonSyntaxException e) {
                FTBTeamIslands.LOGGER.error("Prebuilts: Failed to read json data in {}", PREBUILT_ISLANDS_PATH);
            }
        } catch (FileNotFoundException e) {
            FTBTeamIslands.LOGGER.info("Prebuilts: No islands found in {}", PREBUILT_ISLANDS_PATH);
        }
    }

    public void registerIsland(Team team, Island island) {
        this.islands.put(team.getId(), island);
        this.save();
    }

    public Optional<Island> getIsland(Team team) {
        Island island = this.islands.get(team.getId());
        return island == null ? Optional.empty() : Optional.of(island);
    }

    /**
     * Marks and island as inactive / unclaimed and removes the creator from the island.
     */
    public void markUnclaimed(UUID teamId) {
        if (!this.islands.containsKey(teamId))
            return;

        Island island = this.islands.get(teamId);
        island.creator = null;
        island.active = false;

        this.save();
    }

    public void removeIsland(UUID teamId) {
        this.islands.remove(teamId);
        this.save();
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

    public Optional<Island> getLobby() {
        return lobby != null ? Optional.of(lobby) : Optional.empty();
    }

    public void setLobby(@Nullable Island lobby) {
        this.lobby = lobby;
    }

    public HashMap<UUID, Island> getIslands() {
        return this.islands;
    }

    public List<PrebuiltIslands> getAvailableIslands() {
        return availableIslands;
    }

    public static ResourceKey<Level> getTargetIsland() {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Config.general.targetIslandLevel.get()));
    }

    public void load() {
        CompoundTag compound = this.getSaveCompound();

        if (compound.contains("islands")) {
            ListTag islands = compound.getList("islands", Constants.NBT.TAG_COMPOUND);
            islands.forEach(island -> this.islands.put(((CompoundTag) island).getUUID("key"), Island.read(((CompoundTag) island).getCompound("island"))));
        }

        if (compound.contains("lobby")) {
            this.lobby = Island.read(compound.getCompound("lobby"));
        }
    }

    public void save() {
        shouldSave = true;
    }

    public void saveNow() {
        Path directory = server.getWorldPath(FOLDER_NAME);

        if (Files.notExists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (shouldSave) {
            try (OutputStream stream = Files.newOutputStream(directory.resolve("ftbteamislands.nbt"))) {
                CompoundTag compound = new CompoundTag();
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

                NbtIo.writeCompressed(compound, stream);
                shouldSave = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public CompoundTag getSaveCompound() {
        Path directory = server.getWorldPath(FOLDER_NAME);

        if (Files.notExists(directory) || !Files.isDirectory(directory)) {
            return new CompoundTag();
        }

        Path dataFile = directory.resolve("ftbteamislands.nbt");

        if (Files.exists(dataFile)) {
            try (InputStream stream = Files.newInputStream(dataFile)) {
                return Objects.requireNonNull(NbtIo.readCompressed(stream));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return new CompoundTag();
    }
}
