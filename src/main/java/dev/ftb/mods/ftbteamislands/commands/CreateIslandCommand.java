package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteamislands.islands.IslandSpawner;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CreateIslandCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("create")
            .executes(CreateIslandCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        IslandSpawner.spawnIslandSinglePlayer(context.getSource().getServer().getLevel(IslandsManager.getTargetIsland()), TeamManager.INSTANCE.getPlayerTeam(context.getSource().getPlayerOrException()), context.getSource().getPlayerOrException(), context.getSource().getServer());
        return 0;
    }
}
