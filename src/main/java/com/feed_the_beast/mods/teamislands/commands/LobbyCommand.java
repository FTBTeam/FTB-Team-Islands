package com.feed_the_beast.mods.teamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class LobbyCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("lobby")
            .executes(LobbyCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        return 0;
    }
}
