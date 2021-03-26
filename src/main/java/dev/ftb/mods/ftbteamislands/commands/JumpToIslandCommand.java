package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Optional;

/**
 * Teleport admin to players team by finding team
 *
 * @implNote Waiting on lat to finish is part of this.
 */
public class JumpToIslandCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("islands")
            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
            .then(Commands.argument("team", TeamArgument.create())
                .executes(JumpToIslandCommand::execute)
            );
    }

    // FIXME: this doesn't work right now
    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Team team = TeamArgument.get(context, "team");
        Optional<Island> island = IslandsManager.get().getIsland(team);
        if (!island.isPresent()) {
            return 0;
        }

        island.get().teleportPlayerTo(context.getSource().getPlayerOrException(), context.getSource().getServer());
        return 0;
    }
}
