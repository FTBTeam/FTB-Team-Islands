package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * Teleport admin to players team by finding team
 */
public class JumpToIslandCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("islands")
            .executes(JumpToIslandCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        return 0;
    }
}
