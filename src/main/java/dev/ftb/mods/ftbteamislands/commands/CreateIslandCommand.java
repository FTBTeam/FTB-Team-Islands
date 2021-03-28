package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbteamislands.Config;
import dev.ftb.mods.ftbteamislands.islands.IslandSpawner;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteamislands.network.NetworkManager;
import dev.ftb.mods.ftbteamislands.network.OpenSelectionScreenPacket;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CreateIslandCommand {
    private static final SimpleCommandExceptionType ALREADY_OWN_ISLAND = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.already_have_island"));
    private static final SimpleCommandExceptionType PARTY_REQUIRED = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.must_be_party"));

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("create")
            .executes(CreateIslandCommand::execute);
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandsHelper.exceptionIfDisabled(context); // throw if the mod is not enabled

        IslandsManager manager = IslandsManager.get();
        ServerPlayer player = context.getSource().getPlayerOrException();

        // Only party teams can have an island
        Team playerTeam = TeamManager.INSTANCE.getPlayerTeam(player);
        //        if (playerTeam.getType() != TeamType.PARTY) {
        //            throw PARTY_REQUIRED.create();
        //        }

        //        if (manager.getIsland(playerTeam).isPresent()) {
        //            throw ALREADY_OWN_ISLAND.create();
        //        }

        if (manager.getAvailableIslands().size() > 0) {
            NetworkManager.sendTo(new OpenSelectionScreenPacket(manager.getAvailableIslands()), player);
            return 0;
        }

        MinecraftServer server = context.getSource().getServer();
        IslandSpawner.spawnIsland(
            new ResourceLocation(Config.islands.defaultIslandResource.get()),
            server.getLevel(IslandsManager.getTargetIsland()),
            playerTeam,
            player,
            server
        );

        return 0;
    }
}
