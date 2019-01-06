package uk.co.harieo.battleships.votes;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.timers.GenericTimer;
import uk.co.harieo.GamesCore.voting.Vote;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.guis.BattleGUI;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;
import uk.co.harieo.battleships.tasks.RoundTasks;

public class CoordinateVote {

	private Map<Coordinate, Integer> votes = new HashMap<>();
	private Map<Player, Coordinate> playerVotes = new HashMap<>();

	private RoundTasks tasks;
	private Battleships game;
	private BattleGUI gui;
	private GenericTimer timer;
	private Team team;

	private boolean isOpen = false;
	private Random random = new Random();
	private Coordinate mostPopularCoordinate;

	/**
	 * Converts a {@link BattleGUI} into a vote for which {@link Coordinate} a team wishes to shoot at
	 *
	 * @param tasks to report the vote results to
	 * @param game that this vote is running for
	 * @param gui that should be converted into a vote
	 * @param team that is voting (not the team that is being shot at)
	 */
	public CoordinateVote(RoundTasks tasks, Battleships game, BattleGUI gui, Team team) {
		this.tasks = tasks;
		this.game = game;
		this.gui = gui;
		this.timer = new GenericTimer(game, 30, end -> endVote());
		this.team = team;

		BattleshipsMap map = game.getMap();
		// Add enemy coordinates to vote for as we're not shooting ourselves
		for (Coordinate coordinate : map.getCoordinates(gui.getDisplayTeam())) {
			if (!map.isHit(coordinate)) {
				votes.put(coordinate, 0);
			}
		}

		gui.setOnClick(event -> {
			int slot = event.getSlot();
			Coordinate coordinate = gui.getCoordinate(slot);
			Player player = (Player) event.getWhoClicked();
			ChatModule module = game.chatModule();

			if (!isOpen) { // Prevents voting passed allotted time should it ever become possible
				player.sendMessage(module.formatSystemMessage(
						ChatColor.RED + "Time for voting is over, the time for shooting is at hand"));
				return;
			}

			if (map.isApplicableCoordinate(gui.getDisplayTeam(), coordinate)) {
				if (map.isHit(coordinate)) {
					player.sendMessage(module.formatSystemMessage("That space has already been hit!"));
				} else {
					votes.replace(coordinate, votes.get(coordinate) + 1);
					player.sendMessage(module.formatSystemMessage(
							"You are voting to " + ChatColor.RED + "attack " + ChatColor.WHITE + "coordinate "
									+ ChatColor.YELLOW + coordinate.toString()));

					// If they have already voted, switch their vote to the new coordinate
					if (playerVotes.containsKey(player)) {
						Coordinate oldVote = playerVotes.get(player);
						votes.replace(oldVote, votes.get(oldVote) - 1);
						playerVotes.replace(player, coordinate);
					} else {
						playerVotes.put(player, coordinate);
					}

					Coordinate highestCoordinate = getHighestCoordinate();
					if (mostPopularCoordinate == null ||
							!mostPopularCoordinate
									.equals(highestCoordinate)) { // The most popular coordinate has changed
						if (mostPopularCoordinate != null) { // Can't get the slot of a null value
							gui.setItem(gui.getSlot(mostPopularCoordinate), gui.createItem(mostPopularCoordinate));
						}
						gui.setItem(gui.getSlot(highestCoordinate), createTargetItem(highestCoordinate));
						mostPopularCoordinate = highestCoordinate;
					}
				}
			}
		});

		timer.setOnRun(timeLeft -> {
			if (timeLeft == 15 || timeLeft <= 5) {
				timer.pingTime();
			}
		});
	}

	/**
	 * Begins the timer for this vote
	 */
	public void beginVote() {
		isOpen = true; // Opens the vote
		timer.beginTimer();
	}

	/**
	 * Ends this vote and submits the results to the round task manager
	 */
	private void endVote() {
		this.isOpen = false;
		for (GamePlayer gamePlayer : team.getTeamMembers()) {
			gamePlayer.toBukkit().getOpenInventory().close();
		}

		tasks.endShotVote(team, getHighestCoordinate());
	}

	/**
	 * @return a randomly selected coordinate from the list of applicable coordinates
	 */
	private Coordinate randomSelectCoordinate() {
		BattleshipsMap map = game.getMap();
		// Get a list of coordinates that aren't hit and are valid
		List<Coordinate> coordinates = votes.keySet().stream()
				.filter(coordinate -> map.isApplicableCoordinate(gui.getDisplayTeam(), coordinate) && !map
						.isHit(coordinate)).collect(
						Collectors.toList());
		return coordinates.get(random.nextInt(coordinates.size()));
	}

	/**
	 * @return the most popular coordinate or a random coordinate if there are no votes
	 */
	private Coordinate getHighestCoordinate() {
		Coordinate highestCoordinate = null;
		int highestVote = 0;
		for (Coordinate coordinate : votes.keySet()) {
			int count = votes.get(coordinate);
			if (count > highestVote) {
				highestCoordinate = coordinate;
				highestVote = count;
			}
		}

		if (highestCoordinate == null) {
			highestCoordinate = randomSelectCoordinate();
		}

		return highestCoordinate;
	}

	/**
	 * Creates an item that shows a specified {@link Coordinate} as the most popular voting target
	 *
	 * @param coordinate to create the item for
	 * @return the created item
	 */
	private ItemStack createTargetItem(Coordinate coordinate) {
		ItemStack item = new ItemStack(Material.YELLOW_TERRACOTTA);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Target: " + coordinate.toString());
		// We could list how many people are voting but it serves no purpose, only slowing down the GUI
		meta.setLore(Arrays.asList("", ChatColor.GREEN + "Your allies " + ChatColor.WHITE
						+ "wish to target this coordinate",
				ChatColor.WHITE + "This coordinate is the " + ChatColor.GOLD + "most popular target"));
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * @return the timer counting down for this vote
	 */
	public GenericTimer getTimer() {
		return timer;
	}

}
