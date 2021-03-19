package com.feed_the_beast.mods.teamislands.intergration;

import com.feed_the_beast.mods.ftbteams.api.TeamProperty;
import com.feed_the_beast.mods.teamislands.TeamIslands;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public class Teams {
    public static final IslandProperty ISLAND_PROPERTY = new IslandProperty(new ResourceLocation(TeamIslands.MOD_ID), null);

    private static class IslandProperty extends TeamProperty<UUID> {
        public IslandProperty(ResourceLocation _id, UUID def) {
            super(_id, def);
        }

        @Override
        public Optional<UUID> fromString(String s) {
            try {
                UUID uuid = UUID.fromString(s);
                return Optional.of(uuid);
            } catch (Exception ex) {
                return Optional.empty();
            }
        }
    }
}
