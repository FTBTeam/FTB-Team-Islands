package dev.ftb.mods.ftbteamislands.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListIslandsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list")
            .executes(ListIslandsCommand::execute);
    }

    // TODO: complete
    private static int execute(CommandContext<CommandSourceStack> context) {
        HashMap<UUID, Island> islands = IslandsManager.get().getIslands();
        Set<Team> islandTeams = islands.keySet().stream()
            .map(uuid -> TeamManager.INSTANCE.getPlayerTeam(uuid))
            .collect(Collectors.toSet());

        for (Team islandTeam : islandTeams) {
            MutableComponent clickName = islandTeam.getName().copy();
            MutableComponent text = new TextComponent("hi").append(clickName);
            text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("%s islands %s", FTBTeamIslands.MOD_ID, islandTeam.getDisplayName())));
            context.getSource().sendSuccess(text, true);
        }

        return 0;
    }
}
