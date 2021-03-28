package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.Optional;

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
        //        Set<Map.Entry<UUID, Island>> unclaimedIslands = IslandsManager.get().getUnclaimedIslands();
        //        int islandsToDelete = unclaimedIslands.size();
        //
        //        unclaimedIslands.forEach(island -> {
        //            int regionX = island.getValue().pos.x >> 5;
        //            int regionZ = island.getValue().pos.z >> 5;
        //
        //
        //        });
        //
        Optional<Island> island = IslandsManager.get().getIsland(TeamManager.INSTANCE.getPlayerTeam(context.getSource().getPlayerOrException()));
        island.ifPresent(i -> {
            i.active = false;

            int regionX = i.pos.x >> 5;
            int regionZ = i.pos.z >> 5;

            String fileName = String.format("r.%d.%d.mca", regionX, regionZ);
            String pathWithName = String.format("%s/region/%s", context.getSource().getServer().getWorldPath(LevelResource.ROOT), fileName);

            System.out.println(pathWithName);
            System.out.println(fileName);

            new File(pathWithName).deleteOnExit();
        });

        return 0;
    }
}
