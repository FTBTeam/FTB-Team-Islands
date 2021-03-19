package com.feed_the_beast.mods.teamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ListIslandsCommand {
    public static LiteralArgumentBuilder<CommandSource> register() {
        return Commands.literal("list")
            .executes(ListIslandsCommand::execute);
    }

    private static int execute(CommandContext<CommandSource> context) {
        return 0;
    }
}
