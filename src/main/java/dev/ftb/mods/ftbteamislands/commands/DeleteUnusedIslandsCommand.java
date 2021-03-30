package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Delete region of island
 */
public class DeleteUnusedIslandsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delete")
            .requires(commandSource -> commandSource.hasPermission(2))
            .executes(DeleteUnusedIslandsCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandsHelper.exceptionIfDisabled(context); // throw if the mod is not enabled

        Set<Map.Entry<UUID, Island>> unclaimedIslands = IslandsManager.get().getUnclaimedIslands();

        int islandsToDelete = unclaimedIslands.size(), islandsDeleted = 0;
        int regionRadius = Config.islands.distanceBetweenIslands.get() / 2;

        for (Map.Entry<UUID, Island> map : unclaimedIslands) {
            Island island = map.getValue();

            int posX = island.pos.x >> 5;
            int posZ = island.pos.z >> 5;

            // Delete a 3x3 (by default) region file area around the island. This should never delete other users islands (tm)
            boolean regionDeleted = false;
            for (int x = posX - regionRadius; x <= posX + regionRadius; x++) {
                for (int z = posZ - regionRadius; z <= posZ + regionRadius; z++) {
                    Path worldPath = context.getSource().getServer().getWorldPath(LevelResource.ROOT);
                    File region = new File(String.format("%s/region/r.%d.%d.mca", worldPath, x, z));

                    if (!regionDeleted && region.exists()) {
                        regionDeleted = true;
                        islandsDeleted++;
                    }

                    // Delete the file on a server restart.
                    region.deleteOnExit();
                }
            }

            if (regionDeleted) {
                IslandsManager.get().removeIsland(map.getKey());
            }
        }

        context.getSource().sendSuccess(new TranslatableComponent("commands.ftbteamislands.success.islands_deleted", islandsDeleted, islandsToDelete, islandsToDelete - islandsDeleted), false);

        return 0;
    }
}
