package dev.ftb.mods.teamislands.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ListIslandsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list")
            .executes(ListIslandsCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        return 0;
    }
}
