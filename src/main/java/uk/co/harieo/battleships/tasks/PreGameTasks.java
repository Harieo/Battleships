package uk.co.harieo.battleships.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;
import uk.co.harieo.FurBridge.players.PlayerInfo;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.timers.GenericTimer;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.guis.BattleGUI;
import uk.co.harieo.battleships.guis.ShipPlacementGUI;
import uk.co.harieo.battleships.items.MenuOpenerItem;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;
import uk.co.harieo.battleships.ships.Battleship;
import uk.co.harieo.battleships.ships.ShipStore;

/**
 * A class that handles all pre-game operations before the game begins, starting the entire game process as a whole
 */
public class PreGameTasks {

	// This is used for achievement purposes
	private static boolean FULL_START = false;

	/**
	 * Begins the pre-game processes required to progress to the main game then progresses to the main game when
	 * complete
	 *
	 * @param game instance that this game is running based on
	 */
	public static void beginPreGame(Battleships game) {
		game.getLogger().info("Starting pre-game tasks");
		game.setState(GameState.PRE_GAME);

		FULL_START = Bukkit.getOnlinePlayers().size() >= game.getMaximumPlayers();

		ShipStore blueStore = ShipStore.get(game.getBlueTeam()); // Stores the blue team's ships
		ShipStore redStore = ShipStore.get(game.getRedTeam()); // Stores the red team's ships

		assignTeams(game);

		// All teams have been assigned their members so we can assign ships to them
		blueStore.assignShips();
		redStore.assignShips();

		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			player.openInventory(ShipPlacementGUI.get(gamePlayer).getInventory()); // Allows players to place their ship
			player.getInventory().setItem(4, new MenuOpenerItem().getItem()); // Allows players to re-open the GUI

			game.getLobbyScoreboard().cancelScoreboard(player);
		}

		GenericTimer timer = new GenericTimer(game, 30, end -> {
			unregisterAllInventories(game);
			teleportToStart(game);
			placeFakeShips(game);
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
	 * @return whether the game was full when the pre-game tasks were started
	 */
	public static boolean wasFullStart() {
		return FULL_START;
	}

	/**
	 * Assigns a {@link Team} to all players who do not already have a team
	 *
	 * @param game instance that this game is running on
	 */
	private static void assignTeams(Battleships game) {
		Team team = game.getBlueTeam();
		for (Player bukkitPlayer : Bukkit.getOnlinePlayers()) {
			GamePlayer player = GamePlayerStore.instance(game).get(bukkitPlayer);
			if (player.isPlaying()) {
				if (!player.hasTeam()) { // They have no team so they need one
					bukkitPlayer.sendMessage(""); // Blank line for aesthetics
					team.addTeamMember(player);
					bukkitPlayer.sendMessage(game.chatModule()
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
		for (Player player : Bukkit.getOnlinePlayers()) {
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			if (gamePlayer.isPlaying() && gamePlayer.hasTeam()) {
				Team team = gamePlayer.getTeam();

				if (team.equals(game.getBlueTeam())) {
					player.teleport(game.getMap().getBlueSpawn());
				} else if (team.equals(game.getRedTeam())) {
					player.teleport(game.getMap().getRedSpawn());
				} else {
					throw new IllegalArgumentException("Passed team in pre-game tasks that was neither blue nor red");
				}
			}
		}
	}

	/**
	 * Closes, unregisters and forcefully completes any user controlled GUIs that are currently open
	 *
	 * @param game instance that this game is running on
	 */
	private static void unregisterAllInventories(Battleships game) {
		for (GamePlayer gamePlayer : GamePlayerStore.instance(game).getAll()) {
			Player player = gamePlayer.toBukkit();
			if (player.isOnline()) {
				player.getOpenInventory().close();
			}

			ShipPlacementGUI placementGUI = ShipPlacementGUI.get(gamePlayer);
			if (!placementGUI.isPlaced()) {
				placementGUI.randomlyAssign();
			}
			placementGUI.getGui().unregister();
		}
	}

	/**
	 * Creates a set of fake {@link Battleship} placements to replace players missing from a team
	 *
	 * @param game instance that this game is running on
	 */
	private static void placeFakeShips(Battleships game) {
		Team blue = game.getBlueTeam();
		Team red = game.getRedTeam();

		Map<GamePlayer, Battleship> blueFakes = ShipStore.get(blue).assignFakeShips();
		Map<GamePlayer, Battleship> redFakes = ShipStore.get(red).assignFakeShips();

		BattleshipsMap map = game.getMap();
		BattleGUI blueGUI = new BattleGUI(blue, map, false);
		BattleGUI redGUI = new BattleGUI(red, map, false);

		for (GamePlayer fakePlayer : blueFakes.keySet()) {
			Battleship battleship = blueFakes.get(fakePlayer);
			for (Coordinate coordinate : map.getCoordinates(blue)) {
				if (blueGUI.canPlaceShip(battleship, coordinate, true)) {
					blueGUI.placeShip(fakePlayer, coordinate, true);
					break;
				} else if (blueGUI.canPlaceShip(battleship, coordinate, false)) {
					blueGUI.placeShip(fakePlayer, coordinate, false);
					break;
				}
			}
		}

		for (GamePlayer fakePlayer : redFakes.keySet()) {
			Battleship battleship = redFakes.get(fakePlayer);
			for (Coordinate coordinate : map.getCoordinates(red)) {
				if (redGUI.canPlaceShip(battleship, coordinate, true)) {
					redGUI.placeShip(fakePlayer, coordinate, true);
					break;
				} else if (redGUI.canPlaceShip(battleship, coordinate, false)) {
					redGUI.placeShip(fakePlayer, coordinate, false);
					break;
				}
			}
		}

		blueGUI.unregister();
		redGUI.unregister();
	}

}
