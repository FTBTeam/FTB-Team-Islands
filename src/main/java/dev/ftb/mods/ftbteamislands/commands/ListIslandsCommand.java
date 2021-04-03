package dev.ftb.mods.ftbteamislands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.ftb.mods.ftbteamislands.FTBTeamIslands;
import dev.ftb.mods.ftbteamislands.islands.Island;
import dev.ftb.mods.ftbteamislands.islands.IslandsManager;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListIslandsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list")
            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
            .executes(ListIslandsCommand::execute);
    }

    // TODO: complete
    private static int execute(CommandContext<CommandSourceStack> context) {
        HashMap<UUID, Island> islands = IslandsManager.get().getIslands();
        System.out.println(islands);
        System.out.println(TeamManager.INSTANCE.getTeamMap());
        Set<Team> islandTeams = islands.keySet().stream()
            .map(TeamManager.INSTANCE::getTeamByID)
            .collect(Collectors.toSet());

        for (Team islandTeam : islandTeams) {
            Island island = islands.get(islandTeam.getId());
            MutableComponent text = new TranslatableComponent("commands.ftbteamislands.response.islands")
                .append(new TextComponent(islandTeam.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TranslatableComponent("commands.ftbteamislands.response.found_at", island.spawnPos.getX() + ", " + island.spawnPos.getY() + ", " + island.spawnPos.getZ()));

            if (!island.active) {
                text.append(new TranslatableComponent("commands.ftbteamislands.response.is_inactive"));
            }

            text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s islands %s", FTBTeamIslands.MOD_ID, islandTeam.getDisplayName() + "#" + islandTeam.getId().toString().substring(0, 8)))));
            context.getSource().sendSuccess(text, false);
        }

        return 0;
    }
}
