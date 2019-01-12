package uk.co.harieo.battleships.commands;

import org.bukkit.entity.Player;

import app.ashcon.intake.Command;
import app.ashcon.intake.bukkit.parametric.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurCore.ranks.RankCache;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;

public class SwitchTeamCommand {

	@Command(aliases = {"team", "selectteam"},
			 desc = "Switch your team")
	public void switchTeam(@Sender Player sender, String rawTeam) {
		Battleships game = Battleships.getInstance();
		ChatModule module = game.chatModule();

		if (RankCache.getCachedInfo(sender).hasPermission(Rank.PATRON)) {
			GamePlayer player = GamePlayerStore.instance(game).get(sender);
			Team team;
			if (rawTeam.equalsIgnoreCase("blue")) {
				team = game.getBlueTeam();
			} else {
				team = game.getRedTeam();
			}

			if (team.getTeamMembers().size() >= game.getMaximumPlayers() / 2) {
				sender.sendMessage(module.formatSystemMessage(ChatColor.RED + "That team is full!"));
			} else {
				team.addTeamMember(player);
				sender.sendMessage(module.formatSystemMessage("You have joined the " + team.getFormattedName()));
			}
		} else {
			sender.sendMessage(module.formatSystemMessage(
					"You must be a " + Rank.PATRON.getPrefix() + ChatColor.WHITE + " to use this!"));
			sender.spigot().sendMessage(
					new ComponentBuilder(module.getPrefix() + " ").append("Want to support the server by becoming a ")
							.color(
									ChatColor.WHITE).append(Rank.PATRON.getPrefix()).append("? ").color(ChatColor.WHITE)
							.append("Visit ").append("patreon.com/harieo").color(ChatColor.GOLD).underlined(true)
							.event(new ClickEvent(
									Action.OPEN_URL, "https://www.patreon.com/harieo")).create());
		}
	}

}
