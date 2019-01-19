package uk.co.harieo.battleships.tasks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.utils.PlayerUtils;
import uk.co.harieo.battleships.BattleshipAbility;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.animation.ShootingAnimation;
import uk.co.harieo.battleships.guis.BattleGUI;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;
import uk.co.harieo.battleships.ships.ShipStore;
import uk.co.harieo.battleships.votes.CoordinateVote;

public class RoundTasks {

	private Battleships game;

	private BattleGUI blueGUI;
	private BattleGUI bluePassiveGUI;
	private CoordinateVote blueVote;
	private BattleGUI redGUI;
	private BattleGUI redPassiveGUI;
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
		this.bluePassiveGUI = new BattleGUI(game.getBlueTeam(), game.getMap(), true);
		this.redGUI = new BattleGUI(game.getBlueTeam(), game.getMap(), false); // Show red team blue board
		this.redPassiveGUI = new BattleGUI(game.getRedTeam(), game.getMap(), true);
		this.currentlyPlaying = game.getBlueTeam();
		progressRound();
	}

	/**
	 * Increments the round, refreshing voting and GUIs with new instances then calls {@link #handleRound()}
	 */
	private void progressRound() {
		blueGUI.setFleetItems();
		bluePassiveGUI.setFleetItems();
		blueVote = new CoordinateVote(this, game, blueGUI, game.getBlueTeam(),
				(BattleshipAbility.isAbilityActive(BattleshipAbility.PRESSURE, game.getRedTeam()) ? 5 : 15));
		redGUI.setFleetItems();
		redPassiveGUI.setFleetItems();
		redVote = new CoordinateVote(this, game, redGUI, game.getRedTeam(),
				(BattleshipAbility.isAbilityActive(BattleshipAbility.PRESSURE, game.getBlueTeam()) ? 5 : 15));

		this.currentlyPlaying = game.getBlueTeam();
		BattleshipAbility.resetAbilities(); // New round, new abilities
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
			}

			for (GamePlayer gamePlayer : game.getRedTeam().getTeamMembers()) {
				Player player = gamePlayer.toBukkit();
				player.openInventory(redPassiveGUI.getInventory());
			}
			blueVote.beginVote();
		} else {
			for (GamePlayer gamePlayer : currentlyPlaying.getTeamMembers()) {
				Player player = gamePlayer.toBukkit();
				player.openInventory(redGUI.getInventory());
			}

			for (GamePlayer gamePlayer : game.getBlueTeam().getTeamMembers()) {
				Player player = gamePlayer.toBukkit();
				player.openInventory(bluePassiveGUI.getInventory());
			}
			redVote.beginVote();
		}
	}

	private final String obscuredMessage =
			ChatColor.YELLOW + "*crackle*! " + ChatColor.WHITE + "The " + ChatColor.LIGHT_PURPLE
					+ ChatColor.MAGIC + "Unknown Team " + ChatColor.WHITE + ".. artillery course... *static*";
	;

	/**
	 * Handles the results of a {@link CoordinateVote} and announces the result of the shot
	 *
	 * @param team that was voting
	 * @param coordinate that was voted for
	 */
	public void endShotVote(Team team, Coordinate coordinate, Map<Player, Coordinate> voters) {
		ChatModule module = game.chatModule();
		Bukkit.broadcastMessage(module.formatSystemMessage(
				"The " + team.getFormattedName() + ChatColor.WHITE + " is targeting sector " + ChatColor.RED
						+ ChatColor.BOLD.toString() + coordinate.toString()));

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.getOpenInventory().close();
		}

		new ShootingAnimation(game, coordinate).setOnEnd(end -> {
			Team enemy;
			if (currentlyPlaying.equals(game.getBlueTeam())) {
				enemy = game.getRedTeam();
			} else {
				enemy = game.getBlueTeam();
			}

			boolean isChatSuppressed = BattleshipAbility.isAbilityActive(BattleshipAbility.SIGNAL_JAMMER, enemy);

			BattleshipsMap map = game.getMap();
			String message;

			map.updateTileIsHit(true, coordinate); // Make sure this coordinate can't be hit again
			if (map.getShip(coordinate) != null) {
				if (isChatSuppressed) {
					message = obscuredMessage;
				} else {
					message = ChatColor.GREEN + "Direct hit! " + ChatColor.WHITE + "The " + currentlyPlaying
							.getFormattedName() + ChatColor.WHITE + " hit an enemy ship!";
				}

				currentlyPlaying.addScore(1); // Every hit is +1 score

				if (ShipStore.get(coordinate.getTeam()).checkIfDestroyed(coordinate)) {
					// The ship here was destroyed meaning it has to have an owner and a ship
					GamePlayer gamePlayer = map.getOwningPlayer(coordinate);
					PlayerUtils.playLocalizedSound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1);

					Bukkit.broadcastMessage("");
					if (!isChatSuppressed) {
						Bukkit.broadcastMessage(module.formatSystemMessage(
								ChatColor.RED + "A thunderous explosion sounds! " + ChatColor.WHITE + "It appears "
										+ ChatColor.GREEN +
										(gamePlayer.isFake() ? "a random ship "
												: gamePlayer.toBukkit().getName() + "'s ship ")
										+ ChatColor.WHITE + "has been destroyed!"));
					}
				}
			} else {
				if (isChatSuppressed) {
					message = obscuredMessage;
				} else {
					message = ChatColor.RED + "That made a big splash! " + ChatColor.WHITE + "The " + currentlyPlaying
							.getFormattedName() + ChatColor.WHITE + " missed their shot!";
				}
			}

			Bukkit.broadcastMessage("");
			Bukkit.broadcastMessage(module.formatSystemMessage(message));
			if (isChatSuppressed) {
				Bukkit.broadcastMessage(
						"The " + enemy.getFormattedName() + ChatColor.WHITE + " has " + ChatColor.YELLOW
								+ ChatColor.MAGIC + "0 "
								+ ChatColor.WHITE + "ships left");
			} else {
				Bukkit.broadcastMessage(module.formatSystemMessage(
						"The " + enemy.getFormattedName() + ChatColor.WHITE + " has " + ChatColor.GREEN
								+ ShipStore.get(enemy).getShipsRemaining() + " ships " + ChatColor.WHITE
								+ "left"));
			}
			Bukkit.broadcastMessage("");

			if (InGameTasks.checkWinConditions(game)) {
				return; // Halt the sequence, the game is over
			}

			BukkitScheduler scheduler = Bukkit.getScheduler();
			if (currentlyPlaying.equals(game.getRedTeam())) { // Red team goes last so the round progresses here
				scheduler.runTaskLater(game, task -> progressRound(), 20 * 3);
			} else {
				currentlyPlaying = game.getRedTeam();
				scheduler.runTaskLater(game, task -> handleRound(), 20 * 3);
			}
		});
	}

	public Team getCurrentlyPlaying() {
		return currentlyPlaying;
	}

	public BattleGUI getGUI(Team team) {
		if (team.equals(game.getBlueTeam())) {
			return blueGUI;
		} else {
			return redGUI;
		}
	}

	public BattleGUI getPassiveGUI(Team team) {
		if (team.equals(game.getBlueTeam())) {
			return bluePassiveGUI;
		} else {
			return redPassiveGUI;
		}
	}

	public CoordinateVote getVote(Team team) {
		if (team.equals(game.getBlueTeam())) {
			return blueVote;
		} else {
			return redVote;
		}
	}

}
