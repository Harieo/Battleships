package uk.co.harieo.battleships.ships;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;

public class ShipStore {

	private static Map<Team, ShipStore> CACHE = new HashMap<>();

	private Team team;
	private Map<GamePlayer, Battleship> ships = new HashMap<>();
	private Map<GamePlayer, Boolean> destroyed = new HashMap<>();

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
	 * Whether the ship assigned to this player has been destroyed
	 *
	 * @param player to check the ship of
	 * @return whether this player is destroyed
	 */
	public boolean isDestroyed(GamePlayer player) {
		return destroyed.get(player);
	}

	/**
	 * Sets whether the ship assigned to this player has been destroyed
	 *
	 * @param player to assign the value to
	 * @param isDestroyed whether this player has been destroyed
	 */
	public void setDestroyed(GamePlayer player, boolean isDestroyed) {
		destroyed.replace(player, isDestroyed);
	}

	/**
	 * Checks whether the specified {@link Coordinate} has a ship on it and whether that ship has been destroyed
	 *
	 * @param coordinate to be checked
	 * @return whether the Coordinate contained a ship that is now destroyed
	 */
	public boolean checkIfDestroyed(Coordinate coordinate) {
		BattleshipsMap map = Battleships.getInstance().getMap();
		Battleship ship = map.getShip(coordinate);
		GamePlayer owningPlayer = map.getOwningPlayer(coordinate);
		if (ship != null && !destroyed.get(owningPlayer)) {
			// Retrieve a list of coordinates with the same ship and owner as the parameter
			List<Coordinate> shipCoordinates = map.getCoordinates(coordinate.getTeam()).stream()
					.filter(teamCoordinate -> map.getShip(teamCoordinate) != null
							&& map.getShip(teamCoordinate).equals(ship)
							&& map.getOwningPlayer(teamCoordinate) != null
							&& map.getOwningPlayer(teamCoordinate).equals(owningPlayer))
					.collect(Collectors.toList());

			boolean stillAlive = false;
			for (Coordinate teamCoordinate : shipCoordinates) {
				if (!map.isHit(teamCoordinate)) {
					stillAlive = true;
				}
			}

			if (!stillAlive) {
				destroyed.replace(owningPlayer, true);
			}

			return !stillAlive; // If the ship is still alive, it has NOT been destroyed
		} else {
			return false; // Nothing was destroyed here
		}
	}

	/**
	 * @return how many ships have not been destroyed yet
	 */
	public int getShipsRemaining() {
		int remaining = 0;
		for (GamePlayer player : destroyed.keySet()) {
			if (!destroyed.get(player)) {
				remaining++;
			}
		}
		return remaining;
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
			destroyed.put(gamePlayer, false);

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

	public Map<GamePlayer, Battleship> assignFakeShips() {
		Battleship[] battleships = Battleship.values();
		Map<GamePlayer, Battleship> shipsClone = new HashMap<>(ships); // Clone the map so we can remove used values
		Map<GamePlayer, Battleship> fakeShips = new HashMap<>();

		int quantity = 1;
		for (int i = 0; i < battleships.length;) {
			Battleship battleship = battleships[i];
			GamePlayer matchingKey = null;

			for (GamePlayer gamePlayer : shipsClone.keySet()) {
				if (shipsClone.get(gamePlayer).equals(battleship)) {
					matchingKey = gamePlayer;
				}
			}

			if (matchingKey == null) {
				GamePlayer fakePlayer = GamePlayerStore.instance(Battleships.getInstance()).createFakePlayer();
				fakePlayer.setTeam(team);
				ships.put(fakePlayer, battleship);
				destroyed.put(fakePlayer, false);
				fakeShips.put(fakePlayer, battleship);
			} else {
				shipsClone.remove(matchingKey); // Remove the key so it never applies again
			}

			if (quantity < battleship.getMaxPerGame()) {
				quantity++; // Need more of this per game
			} else {
				i++; // Onto the next ship type
			}
		}

		return fakeShips;
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
