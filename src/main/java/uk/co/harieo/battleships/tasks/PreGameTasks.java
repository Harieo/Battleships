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

/**
 * A class that handles all pre-game operations before the game begins, starting the entire game process as a whole
 */
public class PreGameTasks {

	/**
	 * Begins the pre-game processes required to progress to the main game then progresses to the main game when
	 * complete
	 *
	 * @param game instance that this game is running based on
	 */
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
			game.getLobbyScoreboard().cancelScoreboard(player);
		}

		GenericTimer timer = new GenericTimer(game, 30, end -> {
			unregisterAllInventories(game);
			teleportToStart(game);
			InGameTasks.beginInGameTasks(game);
		});

		timer.setOnRun(timeLeft -> {
			if (timeLeft == 15 || timeLeft <= 5) {
				timer.pingTime();
			}
		});
		timer.beginTimer();
	}

	/**
	 * Assigns a {@link Team} to all players who do not already have a team
	 *
	 * @param game instance that this game is running on
	 * @param store of players to assign teams to
	 */
	private static void assignTeams(Battleships game, GamePlayerStore store) {
		Team team = game.getBlueTeam();
		for (GamePlayer player : store.getAll()) {
			if (player.isPlaying()) {
				if (!player.hasTeam()) { // They have no team so they need one
					player.toBukkit().sendMessage(""); // Blank line for aesthetics
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

	/**
	 * Teleports all players to their starting positions based on their assigned {@link Team}
	 *
	 * @param game instance that this game is running on
	 */
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

	/**
	 * Closes, unregisters and forcefully completes any user controlled GUIs that are currently open
	 *
	 * @param game instance that this game is running on
	 */
	private static void unregisterAllInventories(Game game) {
		for (GamePlayer gamePlayer : GamePlayerStore.instance(game).getAll()) {
			Player player = gamePlayer.toBukkit();
			if (player.isOnline()) {
				player.getOpenInventory().close();
				player.getInventory().clear();
			}

			ShipPlacementGUI placementGUI = ShipPlacementGUI.get(gamePlayer);
			if (!placementGUI.isPlaced()) {
				placementGUI.randomlyAssign();
			}
			placementGUI.getGui().unregister();
		}
	}

}
