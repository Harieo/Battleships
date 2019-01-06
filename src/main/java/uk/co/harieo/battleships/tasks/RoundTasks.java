package uk.co.harieo.battleships.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.animation.ShootingAnimation;
import uk.co.harieo.battleships.guis.BattleGUI;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;
import uk.co.harieo.battleships.votes.CoordinateVote;

public class RoundTasks {

	private Battleships game;
	private int round = 0;

	private BattleGUI blueGUI;
	private CoordinateVote blueVote;
	private BattleGUI redGUI;
	private CoordinateVote redVote;

	private Team currentlyPlaying;

	/**
	 * A task manager for handling rounds in the game
	 *
	 * @param game that is running
	 */
	RoundTasks(Battleships game) {
		this.game = game;
		this.blueGUI = new BattleGUI(game.getRedTeam(), game.getMap(), false); // Show blue team red board
		this.redGUI = new BattleGUI(game.getBlueTeam(), game.getMap(), false); // Show red team blue board
		this.currentlyPlaying = game.getBlueTeam();
		progressRound();
	}

	/**
	 * Increments the round, refreshing voting and GUIs with new instances then calls {@link #handleRound()}
	 */
	private void progressRound() {
		round++;
		blueGUI.setFleetItems();
		blueVote = new CoordinateVote(this, game, blueGUI, game.getBlueTeam());
		redGUI.setFleetItems();
		redVote = new CoordinateVote(this, game, redGUI, game.getRedTeam());

		handleRound();
	}

	/**
	 * Handles voting for each round
	 */
	private void handleRound() {
		ChatModule module = game.chatModule();
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(module.formatSystemMessage(
				"General quarters! The " + currentlyPlaying.getFormattedName() + ChatColor.WHITE
						+ " is preparing to fire!"));
		Bukkit.broadcastMessage("");

		if (currentlyPlaying.equals(game.getBlueTeam())) {
			for (GamePlayer gamePlayer : currentlyPlaying.getTeamMembers()) {
				Player player = gamePlayer.toBukkit();
				player.openInventory(blueGUI.getInventory());
				blueVote.beginVote();
			}
		} else {
			for (GamePlayer gamePlayer : currentlyPlaying.getTeamMembers()) {
				Player player = gamePlayer.toBukkit();
				player.openInventory(redGUI.getInventory());
				redVote.beginVote();
			}
		}
	}

	/**
	 * Handles the results of a {@link CoordinateVote} and announces the result of the shot
	 *
	 * @param team that was voting
	 * @param coordinate that was voted for
	 */
	public void endShotVote(Team team, Coordinate coordinate) {
		ChatModule module = game.chatModule();
		Bukkit.broadcastMessage(module.formatSystemMessage(
				"The " + team.getFormattedName() + ChatColor.WHITE + " is targeting sector " + ChatColor.RED
						+ ChatColor.BOLD.toString() + coordinate.toString()));

		new ShootingAnimation(game, coordinate).setOnEnd(end -> {
			BattleshipsMap map = game.getMap();
			String message;
			if (map.getShip(coordinate) != null) {
				message = ChatColor.GREEN + "Direct hit! " + ChatColor.WHITE + "The " + currentlyPlaying
						.getFormattedName() + ChatColor.WHITE + " hit an enemy ship!";
				currentlyPlaying.addScore(1); // Every hit is +1 score
			} else {
				message = ChatColor.RED + "That made a big splash! " + ChatColor.WHITE + "The " + currentlyPlaying
						.getFormattedName() + ChatColor.WHITE + " missed their shot!";
			}

			Bukkit.broadcastMessage("");
			Bukkit.broadcastMessage(module.formatSystemMessage(message));
			Bukkit.broadcastMessage("");

			map.updateTileIsHit(true, coordinate); // Make sure this coordinate can't be hit again

			BukkitScheduler scheduler = Bukkit.getScheduler();
			if (currentlyPlaying.equals(game.getRedTeam())) { // Red team goes last so the round progresses here
				scheduler.runTaskLater(game, task -> progressRound(), 20 * 3);
			} else {
				currentlyPlaying = game.getRedTeam();
				scheduler.runTaskLater(game, task -> handleRound(), 20 * 3);
			}
		});
	}

}
