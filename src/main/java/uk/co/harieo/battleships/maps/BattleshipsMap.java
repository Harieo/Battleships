package uk.co.harieo.battleships.maps;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import uk.co.harieo.FurCore.maps.MapImpl;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.ships.Battleship;

public class BattleshipsMap {

	private MapImpl map;
	private List<BattleshipsTile> tiles = new ArrayList<>();
	private Location blueSpawn;
	private Location redSpawn;

	BattleshipsMap(MapImpl map, Location blueSpawn, Location redSpawn) {
		this.map = map;
		this.blueSpawn = blueSpawn;
		this.redSpawn = redSpawn;
	}

	/**
	 * Adds a new {@link BattleshipsTile} which holds various values from the game to a single block location
	 *
	 * @param tile to be added
	 */
	void addTile(BattleshipsTile tile) {
		tiles.add(tile);
	}

	/**
	 * Gets the instance of {@link BattleshipsTile} that holds information for the specified location
	 *
	 * @param location to get the tile for
	 * @return the stored tile or none if no tile was found
	 */
	BattleshipsTile getTile(Location location) {
		for (BattleshipsTile tile : tiles) {
			if (tile.getLocation().equals(location)) {
				return tile;
			}
		}
		return null;
	}

	/**
	 * @return a list of all tiles stored for this map
	 */
	public List<BattleshipsTile> getTiles() {
		return tiles;
	}

	/**
	 * Update all stored {@link BattleshipsTile} instances with a new {@link Player} value
	 *
	 * @param player to set the value to
	 * @param coordinate of the tile
	 */
	public void updateTilePlayers(GamePlayer player, Coordinate coordinate) {
		for (BattleshipsTile tile : tiles) {
			if (matchesCoordinate(coordinate, tile)) {
				tile.setPlayerUsing(player);
			}
		}
	}

	/**
	 * Update all stored {@link BattleshipsTile} instances with a new {@link Battleship} value
	 *
	 * @param ship to set the value to
	 * @param coordinate of the tile
	 */
	public void updateTileShips(Battleship ship, Coordinate coordinate) {
		for (BattleshipsTile tile : tiles) {
			if (matchesCoordinate(coordinate, tile)) {
				tile.setShip(ship);
			}
		}
	}

	/**
	 * Update all stored {@link BattleshipsTile} instances with a new boolean value
	 *
	 * @param isHit to set the value to
	 * @param coordinate of the tile
	 */
	public void updateTileIsHit(boolean isHit, Coordinate coordinate) {
		for (BattleshipsTile tile : tiles) {
			if (matchesCoordinate(coordinate, tile)) {
				tile.setIsHit(isHit);
			}
		}
	}

	/**
	 * Resets the owning player and ship of any tile assigned to the stated {@link GamePlayer}
	 *
	 * @param player to reset coordinates of
	 */
	public void resetCoordinates(GamePlayer player) {
		for (Coordinate coordinate : getCoordinates(player.getTeam())) {
			if (getOwningPlayer(coordinate) != null && getOwningPlayer(coordinate).equals(player)) {
				for (BattleshipsTile tile : tiles) {
					if (matchesCoordinate(coordinate, tile)) {
						tile.setPlayerUsing(null);
						tile.setShip(null);
					}
				}
			}
		}
	}

	/**
	 * Gets the owning player of a {@link Coordinate} by checking all stored instances of {@link BattleshipsTile} and
	 * verifies that all the tiles are in sync with each other. If two tiles have different values, this will throw
	 * {@link IllegalStateException}.
	 *
	 * @param coordinate to be checked
	 * @return the owning player or null if the coordinate is not owned
	 */
	public GamePlayer getOwningPlayer(Coordinate coordinate) {
		boolean hasBeenSet = false;
		GamePlayer player = null;
		for (BattleshipsTile tile : tiles) {
			if (matchesCoordinate(coordinate, tile)) {
				if (!hasBeenSet) {
					player = tile.getOwningPlayer();
					hasBeenSet = true;
				} else if ((player == null && tile.getOwningPlayer() != null) ||
						(player != null && !tile.getOwningPlayer().equals(player))) {
					throw new IllegalStateException("Two tiles of the same coordinate have different values!");
				}
			}
		}

		return player;
	}

	/**
	 * Gets the type of ship on a {@link Coordinate} by checking all stored instances of {@link BattleshipsTile} and
	 * verifies that all the tiles are in sync with each other. If two tiles have different values, this will throw
	 * {@link IllegalStateException}.
	 *
	 * @param coordinate to be checked
	 * @return the ship attached or null if the coordinate has no ship attached
	 */
	public Battleship getShip(Coordinate coordinate) {
		boolean hasBeenSet = false;
		Battleship ship = null;
		for (BattleshipsTile tile : tiles) {
			if (matchesCoordinate(coordinate, tile)) {
				if (!hasBeenSet) {
					ship = tile.getShip();
					hasBeenSet = true;
				} else if ((ship == null && tile.getShip() != null) ||
						(ship != null && !tile.getShip().equals(ship))) {
					throw new IllegalStateException("Two tiles of the same coordinate have different values!");
				}
			}
		}

		return ship;
	}

	/**
	 * Checks whether the given {@link Coordinate} is hit by checking all stored instances of {@link BattleshipsTile}
	 * and verifies that all the tiles are in sync with each other. If two tiles have different values, this will
	 * throw {@link IllegalStateException}
	 *
	 * @param coordinate to be checked
	 * @return whether the coordinate is hit or false if the coordinate doesn't match any tile
	 */
	public boolean isHit(Coordinate coordinate) {
		boolean hasBeenSet = false;
		boolean isHit = false;
		for (BattleshipsTile tile : tiles) {
			if (matchesCoordinate(coordinate, tile)) {
				if (!hasBeenSet) {
					isHit = tile.isHit();
					hasBeenSet = true;
				} else if (tile.isHit() != isHit) {
					throw new IllegalStateException("Two tiles of the same coordinate have different values!");
				}
			}
		}

		return isHit;
	}

	/**
	 * @return the location to spawn the blue team
	 */
	public Location getBlueSpawn() {
		return blueSpawn;
	}

	/**
	 * @return the location to spawn the red team
	 */
	public Location getRedSpawn() {
		return redSpawn;
	}

	/**
	 * Using all the stored instances of {@link BattleshipsTile} from {@link #getTiles()}, creates a new list of
	 * coordinates from their location values. There will be no duplicate {@link Coordinate} values as coordinate is
	 * designed to only allow 1 instance of itself with any given values, all values are unique.
	 *
	 * @param team to show which side of the board you are looking for
	 * @return the created list of coordinates
	 */
	public List<Coordinate> getCoordinates(Team team) {
		List<Coordinate> coordinates = new ArrayList<>();
		for (BattleshipsTile tile : tiles) {
			Coordinate coordinate = Coordinate.getCoordinate(tile.getLetter(), tile.getNumber(), team);
			if (!coordinates.contains(coordinate)) {
				coordinates.add(coordinate);
			}
		}
		return coordinates;
	}

	/**
	 * @return the highest number value of all stored instances of {@link BattleshipsTile}
	 */
	public int getHighestX() {
		int highest = 0;
		for (BattleshipsTile tile : tiles) {
			if (tile.getNumber() > highest) {
				highest = tile.getNumber();
			}
		}
		return highest;
	}

	/**
	 * @return the highest letter value, determined by char, of all stored instances of {@link BattleshipsTile}
	 */
	public int getHighestY() {
		int highest = 0;
		for (BattleshipsTile tile : tiles) {
			// All lowercase so starts at 97
			int charNumber = (int) tile.getLetter() - 96;
			if (charNumber > highest) {
				highest = charNumber;
			}
		}
		return highest;
	}

	private boolean matchesCoordinate(Coordinate coordinate, BattleshipsTile tile) {
		return tile.getLetter() == coordinate.getLetter() && tile.getNumber() == coordinate.getNumber()
				&& tile.getTeam().equals(coordinate.getTeam());
	}

	/**
	 * @return the Bukkit world associated with this map
	 */
	public World getWorld() {
		return map.getWorld();
	}

}
