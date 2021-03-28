package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class MyIslandCommand {
    private static final SimpleCommandExceptionType NO_ISLAND_ERROR = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.no_island"));
    private static final SimpleCommandExceptionType DISABLED = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.my_island_disabled"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("myisland")
            .executes(MyIslandCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!Config.general.enableMyIslandCommand.get()) {
            throw DISABLED.create();
        }

        ServerPlayer player = context.getSource().getPlayerOrException();

        // Find the island
        Optional<Island> island = IslandsManager.get().getIsland(TeamManager.INSTANCE.getPlayerTeam(player));

        // If not present error
        if (!island.isPresent()) {
            throw NO_ISLAND_ERROR.create();
        }

        // Teleport the player
        island.get().teleportPlayerTo(player, context.getSource().getServer());
        return 0;
    }
}
