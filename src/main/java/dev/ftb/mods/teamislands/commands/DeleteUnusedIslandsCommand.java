package dev.ftb.mods.teamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Delete region of island
 */
public class DeleteUnusedIslandsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delete")
            .requires(commandSource -> commandSource.hasPermission(2))
            .executes(DeleteUnusedIslandsCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        return 0;
    }
}
