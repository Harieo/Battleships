package uk.co.harieo.battleships.commands;

import org.bukkit.entity.Player;

import app.ashcon.intake.Command;
import app.ashcon.intake.bukkit.parametric.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
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

		GamePlayer player = GamePlayerStore.instance(game).get(sender);
		Team team;
		if (rawTeam.equalsIgnoreCase("blue")) {
			team = game.getBlueTeam();
		} else {
			team = game.getRedTeam();
		}

		Team otherTeam = Battleships.getInstance().getEnemyTeam(team);

		// Makes sure that no team can ever be left with a player difference of more than 1
		boolean willBeImbalanced =
				otherTeam.getTeamMembers().size() == 0 || (player.hasTeam() && player.getTeam().equals(otherTeam)
						&& otherTeam.getTeamMembers().size() - 1 == 0);

		if (team.getTeamMembers().size() >= game.getMaximumPlayers() / 2) {
			sender.sendMessage(module.formatSystemMessage(ChatColor.RED + "That team is full!"));
		} else if (!(player.hasTeam() && player.getTeam().equals(team)) && team.getTeamMembers().size() > 0
				&& willBeImbalanced) {
			sender.sendMessage(module.formatSystemMessage(
					ChatColor.RED + "The teams are too imbalanced and you cannot join that team!"));
		} else if (!player.hasTeam() || !player.getTeam().equals(team)) {
			if (player.hasTeam()) {
				player.getTeam().removeTeamMember(player);
			}
			team.addTeamMember(player);
			sender.sendMessage(module.formatSystemMessage("You have joined the " + team.getFormattedName()));
		}
	}

}
