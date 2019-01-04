package uk.co.harieo.battleships.tasks;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.FurCore.scoreboards.ConstantElement;
import uk.co.harieo.FurCore.scoreboards.GameBoard;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.ships.ShipStore;

class InGameTasks {

	private static GameBoard scoreboard;

	static void beginInGameTasks(Battleships game) {
		game.getLogger().info("Starting the in-game processes");
		game.setState(GameState.IN_GAME);

		scoreboard = new GameBoard(ChatColor.GOLD + ChatColor.BOLD.toString() + "Battleships", DisplaySlot.SIDEBAR);
		setupScoreboard(game);

		for (GamePlayer player : GamePlayerStore.instance(game).getAll()) {
			scoreboard.render(game, player.toBukkit(), 5);
		}
	}

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

		scoreboard.addLine(new ConstantElement(ChatColor.AQUA + ChatColor.BOLD.toString() + "Ships Left"));
		scoreboard.addLine((Player player) -> {
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			if (gamePlayer.hasTeam()) {
				return String.valueOf(ShipStore.get(gamePlayer.getTeam()).getShipsRemaining());
			} else {
				return "N/A";
			}
		});
		scoreboard.addBlankLine();

		scoreboard.addLine(new ConstantElement(ChatColor.YELLOW + ChatColor.BOLD.toString() + "patreon.com/harieo"));
	}

	public static GameBoard getScoreboard() {
		return scoreboard;
	}
}
