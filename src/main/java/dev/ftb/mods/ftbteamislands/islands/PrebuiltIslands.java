package dev.ftb.mods.ftbteamislands.islands;

import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

// Wrapper for our json format to help with reading and deserializing
public class PrebuiltIslands {
    private final String name;
    private final String author;
    private final String desc;
    private final List<PrebuiltIsland> islands;

    public PrebuiltIslands(String name, String author, String desc, List<PrebuiltIsland> islands) {
        this.name = name;
        this.author = author;
        this.desc = desc;
        this.islands = islands;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDesc() {
        return desc;
    }

    public List<PrebuiltIsland> getIslands() {
        return islands;
    }

    public CompoundTag write() {
        CompoundTag compound = new CompoundTag();
        compound.putString("name", this.name);
        compound.putString("desc", this.desc);
        compound.putString("author", this.author);
        ListTag list = new ListTag();
        this.islands.forEach(island -> list.add(island.write()));
        compound.put("islands", list);
        return compound;
    }

    public static PrebuiltIslands read(CompoundTag compound) {
        return new PrebuiltIslands(
            compound.getString("name"),
            compound.getString("desc"),
            compound.getString("author"),
            compound.getList("islands", Constants.NBT.TAG_COMPOUND).stream()
                .map(tag -> PrebuiltIsland.read((CompoundTag) tag))
                .collect(Collectors.toList())
        );
    }

    public static class PrebuiltIsland {
        private final String name;
        private final String desc;
        private final String structureFileLocation;
        private final ResourceLocation image;

        public PrebuiltIsland(String name, String desc, String structureFileLocation, ResourceLocation image) {
            this.name = name;
            this.desc = desc;
            this.structureFileLocation = structureFileLocation;
            this.image = image;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getStructureFileLocation() {
            return structureFileLocation;
        }

        public ResourceLocation getImage() {
            return image;
        }

        public CompoundTag write() {
            CompoundTag compound = new CompoundTag();
            compound.putString("name", this.name);
            compound.putString("desc", this.desc);
            compound.putString("structure", this.structureFileLocation);
            compound.putString("image", this.image.toString());
            return compound;
        }

        public static PrebuiltIsland read(CompoundTag compound) {
            return new PrebuiltIsland(
                compound.getString("name"),
                compound.getString("desc"),
                compound.getString("structure"),
                new ResourceLocation(compound.getString("image"))
            );
        }

        public static class Deserializer implements JsonDeserializer<PrebuiltIsland> {
            @Override
            public PrebuiltIsland deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject obj = json.getAsJsonObject();
                return new PrebuiltIsland(
                    obj.get("name").getAsString(),
                    obj.get("desc").getAsString(),
                    obj.get("structure").getAsString(),
                    new ResourceLocation(obj.get("image").getAsString())
                );
            }
        }
    }
}
