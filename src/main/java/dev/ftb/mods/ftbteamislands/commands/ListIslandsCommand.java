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
import java.util.Map;
import java.util.UUID;

public class ListIslandsCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list")
            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
            .executes(ListIslandsCommand::execute);
    }

    // TODO: complete
    private static int execute(CommandContext<CommandSourceStack> context) {
        HashMap<UUID, Island> islands = IslandsManager.get().getIslands();

        for (Map.Entry<UUID, Island> entry : islands.entrySet()) {
            Island island = islands.get(entry.getKey());

            MutableComponent text;
            Team team = TeamManager.INSTANCE.getTeamByID(entry.getKey());
            if (!island.active && team != null) {
                text = new TranslatableComponent("commands.ftbteamislands.response.islands")
                    .append(new TextComponent(team.getDisplayName()).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(new TranslatableComponent("commands.ftbteamislands.response.found_at", island.spawnPos.getX() + ", " + island.spawnPos.getY() + ", " + island.spawnPos.getZ()));

                text.setStyle(text.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s islands %s", FTBTeamIslands.MOD_ID, team.getDisplayName() + "#" + team.getId().toString().substring(0, 8)))));
            } else {
                text = new TranslatableComponent("commands.ftbteamislands.response.inactive_island", String.format("x: %d, y: %d, z: %d", island.getSpawnPos().getX(), island.getSpawnPos().getY(), island.getSpawnPos().getZ()));
            }

            context.getSource().sendSuccess(text, false);
        }

        return 0;
    }
}
