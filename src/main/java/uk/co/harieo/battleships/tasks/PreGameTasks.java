package uk.co.harieo.battleships.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;

public class PreGameTasks {

	public static void beginPreGame(Battleships game) {
		game.getLogger().info("Starting pre-game tasks");
		GamePlayerStore playerStore = GamePlayerStore.instance(game);
		Team team = game.getBlueTeam();

		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = playerStore.get(player); // Create or retrieve their player data
			if (!gamePlayer.hasTeam()) {
				team.addTeamMember(gamePlayer);
				player.sendMessage(game.chatModule()
						.formatSystemMessage("You have been assigned to the " + team.getFormattedName()));

				// Invert the teams so the next player is added to the opposite team for balance
				if (team.equals(game.getBlueTeam())) {
					team = game.getRedTeam();
				} else {
					team = game.getBlueTeam();
				}
			}
		}
	}

}
