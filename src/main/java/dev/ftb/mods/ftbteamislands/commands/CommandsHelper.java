package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

public class CommandsHelper {
    public static final DynamicCommandExceptionType DISABLED_IN = new DynamicCommandExceptionType((obj) -> new TranslatableComponent("commands.ftbteamislands.error.already_have_island", obj));

    public static void exceptionIfDisabled(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!IslandsManager.isEnabled(context.getSource().getServer())) {
            throw DISABLED_IN.create(context.getSource().getServer().isDedicatedServer()
                ? "Multiplayer"
                : "Singleplayer");
        }
    }
}
