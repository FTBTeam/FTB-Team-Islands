package dev.ftb.mods.ftbteamislands.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.HashMap;
import java.util.UUID;

public class ListIslandsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list")
            .executes(ListIslandsCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        HashMap<UUID, Island> islands = IslandsManager.get().getIslands();
        return 0;
    }
}
