package uk.co.harieo.battleships.ships;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;

public class ShipStore {

	private static Map<Team, ShipStore> CACHE = new HashMap<>();

	private Team team;
	private Map<GamePlayer, Battleship> ships = new HashMap<>();

	private ShipStore(Team team) {
		this.team = team;
	}

	/**
	 * @return the team this store is based on
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * Retrieves the {@link Battleship} assigned to a specific player
	 *
	 * @param player to find the ship of
	 * @return the ship assigned to the player or null if no ship is assigned
	 */
	public Battleship getShip(GamePlayer player) {
		return ships.get(player);
	}

	/**
	 * @return a list of players and their assigned ships
	 */
	public Map<GamePlayer, Battleship> getShips() {
		return ships;
	}

	/**
	 * Assigns a {@link Battleship} to all members of the given {@link Team} based on the maximum amount of ships a game
	 * may have.
	 */
	public void assignShips() {
		ships.clear();

		int i = 0;
		int quantity = 1;
		for (GamePlayer gamePlayer : team.getTeamMembers()) {
			if (i >= Battleship.values().length) {
				i = 0; // Start at the beginning
				Battleships.getInstance().getLogger().warning("Ran out of ships for players, starting from beginning");
			}

			Battleship battleship = Battleship.values()[i];
			ships.put(gamePlayer, battleship);

			Player player = gamePlayer.toBukkit();
			if (player.isOnline()) {
				player.sendMessage(""); // Blank line for aesthetics
				ChatModule module = Battleships.getInstance().chatModule();
				player.sendMessage(module.formatSystemMessage(
						"You are now commander of the team's " + ChatColor.GOLD + ChatColor.BOLD.toString() + battleship
								.getName()));
				player.sendMessage(module.formatSystemMessage(
						"Your ship is " + ChatColor.GREEN + battleship.getSize() + " spaces " + ChatColor.WHITE
								+ "long"));
				player.sendMessage(""); // Another blank line
			}

			if (quantity < battleship.getMaxPerGame()) {
				quantity++; // Need more of this per game
			} else {
				i++; // Onto the next ship type
			}
		}
	}

	/**
	 * Gets an instance of this class for the specified {@link Team} either from the cache or by instantiation
	 *
	 * @param team to get the store of
	 * @return the instance of store
	 */
	public static ShipStore get(Team team) {
		if (CACHE.containsKey(team)) {
			return CACHE.get(team);
		} else {
			ShipStore store = new ShipStore(team);
			CACHE.put(team, store);
			return store;
		}
	}

}