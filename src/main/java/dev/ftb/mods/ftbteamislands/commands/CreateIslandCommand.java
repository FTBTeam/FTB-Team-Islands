package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.islands.IslandSpawner;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import dev.ftb.mods.ftbteamislands.network.OpenSelectionScreenPacket;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CreateIslandCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("create")
            .executes(CreateIslandCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        IslandsManager manager = IslandsManager.get();
        ServerPlayer player = context.getSource().getPlayerOrException();

        if (manager.getAvailableIslands().size() > 0) {
            NetworkManager.sendTo(new OpenSelectionScreenPacket(manager.getAvailableIslands()), player);
            return 0;
        }

        MinecraftServer server = context.getSource().getServer();
        IslandSpawner.spawnIsland(
            new ResourceLocation(FTBTeamIslands.MOD_ID, "teamislands_island"),
            server.getLevel(IslandsManager.getTargetIsland()),
            TeamManager.INSTANCE.getPlayerTeam(player),
            player,
            server
        );

        return 0;
    }
}
