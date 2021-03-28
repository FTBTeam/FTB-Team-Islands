package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class ReloadIslandsJsonCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("reload-islands-json")
            .requires(commandSource -> commandSource.hasPermission(2))
            .executes(ReloadIslandsJsonCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        IslandsManager.get().reloadPrebuilts();
        context.getSource().sendSuccess(new TranslatableComponent("commands.ftbteamislands.success.json_updated"), false);
        return 0;
    }
}
