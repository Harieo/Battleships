package uk.co.harieo.battleships.tasks;

import org.bukkit.entity.Player;

import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.timers.GenericTimer;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.guis.ShipPlacementGUI;
import uk.co.harieo.battleships.ships.ShipStore;

public class PreGameTasks {

	public static void beginPreGame(Battleships game) {
		game.getLogger().info("Starting pre-game tasks");
		game.setState(GameState.PRE_GAME);

		GamePlayerStore playerStore = GamePlayerStore.instance(game);

		ShipStore blueStore = ShipStore.get(game.getBlueTeam()); // Stores the blue team's ships
		ShipStore redStore = ShipStore.get(game.getRedTeam()); // Stores the red team's ships

		assignTeams(game, playerStore);

		// All teams have been assigned their members so we can assign ships to them
		blueStore.assignShips();
		redStore.assignShips();

		for (GamePlayer gamePlayer : playerStore.getAll()) {
			Player player = gamePlayer.toBukkit();
			player.openInventory(ShipPlacementGUI.get(gamePlayer).getInventory());
		}

		GenericTimer timer = new GenericTimer(game, 30, end -> {
			// Randomly place unplaced ships
			unregisterAllInventories(game);
			teleportToStart(game);
		});
		timer.setOnRun(timeLeft -> {
			if (timeLeft == 15 || timeLeft <= 5) {
				timer.pingTime();
			}
		});
		timer.beginTimer();
	}

	private static void assignTeams(Battleships game, GamePlayerStore store) {
		Team team = game.getBlueTeam();
		for (GamePlayer player : store.getAll()) {
			if (player.isPlaying()) {
				if (!player.hasTeam()) { // They have no team so they need one
					team.addTeamMember(player);
					player.toBukkit().sendMessage(game.chatModule()
							.formatSystemMessage("You have been assigned to the " + team.getFormattedName()));

					// Invert the teams so the next player is added to the opposite team for balance
					if (team.equals(game.getBlueTeam())) {
						team = game.getRedTeam(); // Next player should be added to red team
					} else {
						team = game.getBlueTeam(); // Next player should be added to blue team
					}
				}
			}
		}
	}

	private static void teleportToStart(Battleships game) {
		for (GamePlayer gamePlayer : GamePlayerStore.instance(game).getAll()) {
			Team team = gamePlayer.getTeam();
			Player player = gamePlayer.toBukkit();

			if (team.equals(game.getBlueTeam())) {
				player.teleport(game.getMap().getBlueSpawn());
			} else if (team.equals(game.getRedTeam())) {
				player.teleport(game.getMap().getRedSpawn());
			} else {
				throw new IllegalArgumentException("Passed team in pre-game tasks that was neither blue nor red");
			}
		}
	}

	private static void unregisterAllInventories(Game game) {
		for (GamePlayer gamePlayer : GamePlayerStore.instance(game).getAll()) {
			Player player = gamePlayer.toBukkit();
			if (player.isOnline()) {
				player.getOpenInventory().close();
				player.getInventory().clear();
			}

			ShipPlacementGUI placementGUI = ShipPlacementGUI.get(gamePlayer);
			placementGUI.getGui().unregister();
		}
	}

}
