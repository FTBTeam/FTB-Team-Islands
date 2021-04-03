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
import dev.ftb.mods.ftbteams.data.TeamType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

public class CreateIslandCommand {
    static final HashMap<UUID, Instant> playersTimeout = new HashMap<>();

    private static final SimpleCommandExceptionType ALREADY_OWN_ISLAND = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.already_have_island"));
    private static final SimpleCommandExceptionType TO_QUICK = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.to_quick"));
    private static final SimpleCommandExceptionType IN_PARTY = new SimpleCommandExceptionType(new TranslatableComponent("commands.ftbteamislands.error.in_party"));

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
        if (playerTeam.getType() != TeamType.PARTY) {
            // Creates a team for the player if they don't have a party.
            TeamManager.INSTANCE.createParty(player, "");

            // Ensure it's actually been created
            if (TeamManager.INSTANCE.getPlayerTeam(player).getType() != TeamType.PARTY) {
                throw IN_PARTY.create();
            }
        }

        if (manager.getIsland(playerTeam).isPresent()) {
            throw ALREADY_OWN_ISLAND.create();
        }

        // Ensure they're not spamming the island creation
        if (Config.general.creationTimeout.get()) {
            Instant instant = playersTimeout.get(player.getUUID());
            if (instant != null && !instant.plus(5, ChronoUnit.MINUTES).isBefore(Instant.now())) {
                throw TO_QUICK.create();
            }

            // Update their last created date.
            playersTimeout.put(player.getUUID(), Instant.now());
        }

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
            server,
            Config.islands.defaultIslandResourceYOffset.get()
        );

        return 0;
    }
}
