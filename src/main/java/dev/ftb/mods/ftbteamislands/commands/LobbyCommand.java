package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class LobbyCommand {
    private static final SimpleCommandExceptionType NO_LOBBY_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.no_lobby_found"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("lobby")
            .executes(LobbyCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        ServerPlayer player = context.getSource().getPlayerOrException();

        // Find the lobby and teleport them to it
        Optional<Island> lobby = IslandsManager.get().getLobby();
        if (!lobby.isPresent())
            throw NO_LOBBY_FOUND.create();

        lobby.get().teleportPlayerTo(player, server);
        return 0;
    }
}
