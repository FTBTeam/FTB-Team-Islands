package dev.ftb.mods.teamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MyIslandCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("myisland")
            .executes(MyIslandCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        return 0;
    }
}
