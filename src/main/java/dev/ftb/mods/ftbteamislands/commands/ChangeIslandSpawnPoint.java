package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ChangeIslandSpawnPoint {
    private static final SimpleCommandExceptionType NO_ISLAND_ERROR = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.no_island"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("change_spawn")
                .executes(ChangeIslandSpawnPoint::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandsHelper.exceptionIfDisabled(context); // throw if the mod is not enabled

        ServerPlayer player = context.getSource().getPlayerOrException();

        // Find the island
        Optional<Island> island = IslandsManager.get().getIsland(TeamManager.INSTANCE.getPlayerTeam(player));

        // If not present error
        if (!island.isPresent()) {
            throw NO_ISLAND_ERROR.create();
        }

        island.get().spawnPos = player.blockPosition();

        context.getSource().sendSuccess(new TranslatableComponent("commands.ftbteamislands.response.spawn_changed", player.blockPosition().toShortString()), false);
        return 0;
    }
}
