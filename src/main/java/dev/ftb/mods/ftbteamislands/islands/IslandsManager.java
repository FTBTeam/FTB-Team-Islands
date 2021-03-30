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
import net.minecraftforge.fml.loading.FMLLoader;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class IslandsManager {
    public static final LevelResource FOLDER_NAME = new LevelResource("ftbteamislands");

    public static final String PREBUILT_ISLANDS_PATH = "/config/ftbteamislands/";
    private static final String PREBUILT_ISLANDS_JSON = PREBUILT_ISLANDS_PATH + "/islands.json";
    private static IslandsManager INSTANCE;

    public final MinecraftServer server;
    private final HashMap<UUID, Island> islands = new HashMap<>();
    private final HashMap<UUID, Island> deletedIslands = new HashMap<>();
    private final List<PrebuiltIslands> availableIslands = new ArrayList<>();
    private int islandsEverCreated = 1;
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

    public static ResourceKey<Level> getTargetIsland() {
        return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(Config.general.targetIslandLevel.get()));
    }

    public static boolean isEnabled(MinecraftServer server) {
        return server.isDedicatedServer()
            ? Config.general.enableMultiplayer.get()
            : Config.general.enableSinglePlayer.get();
    }

    /**
     * Generates an empty config if one does not already exist in the config/ftbteamislands/ folder
     */
    public static void createEmptyJson() {
        Path gamePath = FMLLoader.getGamePath();
        File file = new File(gamePath + PREBUILT_ISLANDS_PATH);
        if (file.exists()) {
            return;
        }

        try {
            file.mkdirs();
            File jsonFile = new File(file.getAbsolutePath() + "/islands.json");
            boolean didCreate = jsonFile.createNewFile();
            new File(gamePath + PREBUILT_ISLANDS_PATH + "structures/").mkdir();

            if (didCreate) {
                FileWriter fileWriter = new FileWriter(jsonFile);
                fileWriter.write("[]");
                fileWriter.close();
            }
        } catch (IOException e) {
            FTBTeamIslands.LOGGER.error("Failed to auto generate islands json file [{}]", gamePath + PREBUILT_ISLANDS_JSON);
        }
    }

    public static ListTag writeIslandsToTag(HashMap<UUID, Island> islands) {
        ListTag list = new ListTag();

        for (Map.Entry<UUID, Island> island : islands.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("key", island.getKey());
            tag.put("island", island.getValue().write());

            list.add(tag);
        }

        return list;
    }

    /**
     * Loads and populates the availableIslands from the json
     */
    private void findAndLoadPrebuilts() {
        FTBTeamIslands.LOGGER.info("Prebuilts: Searching for prebuilt islands in {}", PREBUILT_ISLANDS_JSON);
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(PrebuiltIslands.PrebuiltIsland.class, new PrebuiltIslands.PrebuiltIsland.Deserializer())
            .create();

        Type prebuiltIslandsType = new TypeToken<List<PrebuiltIslands>>() {
        }.getType();
        try {
            try {
                List<PrebuiltIslands> islands = gson.fromJson(new FileReader(this.server.getServerDirectory().getAbsolutePath() + PREBUILT_ISLANDS_JSON), prebuiltIslandsType);
                this.availableIslands.addAll(islands);

                FTBTeamIslands.LOGGER.info("Prebuilts: found {} islands in {}", this.availableIslands.stream().mapToInt(e -> e.getIslands().size()).count(), PREBUILT_ISLANDS_JSON);
            } catch (JsonIOException | JsonSyntaxException e) {
                FTBTeamIslands.LOGGER.error("Prebuilts: Failed to read json data in {}", PREBUILT_ISLANDS_JSON);
            }
        } catch (FileNotFoundException e) {
            FTBTeamIslands.LOGGER.info("Prebuilts: No islands found in {}", PREBUILT_ISLANDS_JSON);
        }
    }

    public void reloadPrebuilts() {
        this.availableIslands.clear();
        this.findAndLoadPrebuilts();
    }

    public void registerIsland(Team team, Island island) {
        this.islands.put(team.getId(), island);
        this.islandsEverCreated++;
        this.save();
    }

    /**
     * Wrap with optional. Get the island from the Teams UUID
     */
    public Optional<Island> getIsland(Team team) {
        Island island = this.islands.get(team.getId());
        return island == null
            ? Optional.empty()
            : Optional.of(island);
    }

    /**
     * Marks and island as inactive / unclaimed and removes the creator from the island.
     */
    public void markUnclaimed(UUID teamId) {
        if (!this.islands.containsKey(teamId)) {
            return;
        }

        Island island = this.islands.get(teamId);
        island.creator = null;
        island.active = false;

        this.save();
    }

    public void removeIsland(UUID teamId) {
        Island island = this.islands.get(teamId);
        if (island == null) {
            return;
        }

        this.deletedIslands.put(teamId, island);
        this.islands.remove(teamId);
        this.save();
    }

    /**
     * Find unclaimed islands by their active flag
     *
     * @return a set of island UUID's
     */
    public Set<Map.Entry<UUID, Island>> getUnclaimedIslands() {
        return this.islands.entrySet().stream()
            .filter(island -> !island.getValue().isActive())
            .collect(Collectors.toSet());
    }

    public Optional<Island> getLobby() {
        return this.lobby != null
            ? Optional.of(this.lobby)
            : Optional.empty();
    }

    public void setLobby(@Nullable Island lobby) {
        this.lobby = lobby;
    }

    public HashMap<UUID, Island> getIslands() {
        return this.islands;
    }

    public List<PrebuiltIslands> getAvailableIslands() {
        return this.availableIslands;
    }

    public void load() {
        CompoundTag compound = this.getSaveCompound();

        if (compound.contains("islands")) {
            compound.getList("islands", Constants.NBT.TAG_COMPOUND)
                .forEach(island -> this.islands.put(((CompoundTag) island).getUUID("key"), Island.read(((CompoundTag) island).getCompound("island"))));
        }

        if (compound.contains("deletedIslands")) {
            compound.getList("deletedIslands", Constants.NBT.TAG_COMPOUND)
                .forEach(island -> this.deletedIslands.put(((CompoundTag) island).getUUID("key"), Island.read(((CompoundTag) island).getCompound("island"))));
        }

        if (compound.contains("lobby")) {
            this.lobby = Island.read(compound.getCompound("lobby"));
        }

        this.islandsEverCreated = compound.contains("islandsEverCreated")
            ? compound.getInt("islandsEverCreated")
            : 1;
    }

    public void save() {
        this.shouldSave = true;
    }

    /**
     * Save the current data to a compound nbt file in the main worlds path.
     */
    public void saveNow() {
        Path directory = this.server.getWorldPath(FOLDER_NAME);

        if (Files.notExists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (this.shouldSave) {
            try (OutputStream stream = Files.newOutputStream(directory.resolve("ftbteamislands.nbt"))) {
                CompoundTag compound = new CompoundTag();
                if (this.lobby != null) {
                    compound.put("lobby", this.lobby.write());
                }

                compound.putInt("islandsEverCreated", this.islandsEverCreated);
                compound.put("islands", writeIslandsToTag(this.islands));
                compound.put("deletedIslands", writeIslandsToTag(this.deletedIslands));

                NbtIo.writeCompressed(compound, stream);
                this.shouldSave = false;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public CompoundTag getSaveCompound() {
        Path directory = this.server.getWorldPath(FOLDER_NAME);

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

    public int getIslandsEverCreated() {
        return this.islandsEverCreated;
    }
}
