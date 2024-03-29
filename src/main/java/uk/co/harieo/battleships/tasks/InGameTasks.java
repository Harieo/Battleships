package uk.co.harieo.battleships.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.scoreboards.ConstantElement;
import uk.co.harieo.GamesCore.scoreboards.GameBoard;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.items.AbilityItem;
import uk.co.harieo.battleships.ships.ShipStore;

public class InGameTasks {

	private static GameBoard scoreboard;
	private static RoundTasks taskManager;

	/**
	 * Begins all tasks related to primary game functions, using {@link RoundTasks} as the task manager
	 *
	 * @param game that this game is based off
	 */
	static void beginInGameTasks(Battleships game) {
		game.getLogger().info("Starting the in-game processes");
		game.setState(GameState.IN_GAME);

		scoreboard = new GameBoard(ChatColor.GOLD + ChatColor.BOLD.toString() + "Battleships", DisplaySlot.SIDEBAR);
		setupScoreboard(game);

		for (GamePlayer gamePlayer : GamePlayerStore.instance(game).getAll()) {
			Player player = gamePlayer.toBukkit();
			if (gamePlayer.isPlaying()) {
				scoreboard.render(game, player, 5);
				player.getInventory().setItem(3, new AbilityItem(3, player).getItem());
			}
		}

		Bukkit.getScheduler().runTaskLater(game, bukkitTask -> taskManager = new RoundTasks(game), 20 * 3);
	}

	/**
	 * @return whether the {@link RoundTasks} manager have been setup yet
	 */
	public static boolean hasSetup() {
		return taskManager != null;
	}

	/**
	 * @return the instance of {@link RoundTasks} which is managing all in-game tasks. This may be null as there is a
	 * 60 tick delay before it is created after calling {@link #beginInGameTasks(Battleships)}.
	 */
	public static RoundTasks getTaskManager() {
		return taskManager;
	}

	/**
	 * Sets all the values of the in-game scoreboards
	 *
	 * @param game that this game is running from
	 */
	private static void setupScoreboard(Battleships game) {
		scoreboard.addBlankLine();

		// Team section //
		scoreboard.addLine(new ConstantElement(
				ChatColor.GREEN + ChatColor.BOLD.toString() + "Team"));
		scoreboard.addLine((Player player) -> {
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			if (gamePlayer.getTeam() == null) {
				return "None";
			} else {
				return gamePlayer.getTeam().getFormattedName();
			}
		});
		scoreboard.addBlankLine();

		// Score section //
		scoreboard.addLine(new ConstantElement(ChatColor.RED + ChatColor.BOLD.toString() + "Hits"));
		scoreboard.addLine((Player player) -> {
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			return String.valueOf(gamePlayer.getTeam().getTeamScore());
		});
		scoreboard.addBlankLine();

		scoreboard.addLine(new ConstantElement(ChatColor.AQUA + ChatColor.BOLD.toString() + "Enemy Ships"));
		scoreboard.addLine((Player player) -> {
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			if (gamePlayer.hasTeam()) {
				Team enemy;
				if (gamePlayer.getTeam().equals(game.getBlueTeam())) {
					enemy = game.getRedTeam();
				} else {
					enemy = game.getBlueTeam();
				}
				return String.valueOf(ShipStore.get(enemy).getShipsRemaining());
			} else {
				return "N/A";
			}
		});
		scoreboard.addBlankLine();

		scoreboard.addLine(new ConstantElement(Battleships.SCOREBOARD_IP));
	}

	/**
	 * Checks to see if the game has been won by either team yet
	 *
	 * @param game that this is being run based on
	 * @return true if the game has been won or false if the game has not been won
	 */
	public static boolean checkWinConditions(Battleships game) {
		ChatModule module = game.chatModule();
		Team blueTeam = game.getBlueTeam();
		Team redTeam = game.getRedTeam();

		boolean gameIsOver = false;
		boolean winByDestruction = false;
		Team winningTeam = null;
		Team losingTeam = null;

		if (blueTeam.getTeamMembers().size() <= 0) { // A team has no players left
			gameIsOver = true;
			winningTeam = redTeam;
			losingTeam = blueTeam;
		} else if (redTeam.getTeamMembers().size() <= 0) {
			gameIsOver = true;
			winningTeam = blueTeam;
			losingTeam = redTeam;
		} else if (ShipStore.get(blueTeam).getShipsRemaining() <= 0) { // A team has no ships left
			gameIsOver = true;
			winByDestruction = true;
			winningTeam = redTeam;
			losingTeam = blueTeam;
		} else if (ShipStore.get(redTeam).getShipsRemaining() <= 0) {
			gameIsOver = true;
			winByDestruction = true;
			winningTeam = blueTeam;
			losingTeam = redTeam;
		}

		if (gameIsOver) {
			Bukkit.broadcastMessage("");
			if (winByDestruction) {
				Bukkit.broadcastMessage(module.formatSystemMessage(
						"The " + winningTeam.getFormattedName() + ChatColor.WHITE + " have destroyed all of the "
								+ losingTeam
								.getFormattedName() + ChatColor.WHITE + "'s ships!"));
			} else {
				Bukkit.broadcastMessage(module.formatSystemMessage(
						"The " + losingTeam.getFormattedName() + ChatColor.WHITE
								+ " have fled the battle in fear of the " + winningTeam.getFormattedName()));
			}

			Bukkit.broadcastMessage(module.formatSystemMessage(
					"The " + winningTeam.getFormattedName() + ChatColor.WHITE + " is " + ChatColor.GREEN
							+ ChatColor.BOLD
							.toString() + "Victorious"));
			Bukkit.broadcastMessage("");
			EndGameTasks.beginEndGame(game, winningTeam, winByDestruction);
		}

		return gameIsOver;
	}

}
