package uk.co.harieo.battleships.maps;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import uk.co.harieo.FurCore.maps.MapImpl;
import uk.co.harieo.battleships.ships.Battleship;

public class BattleshipsMap {

	private MapImpl map;
	private List<BattleshipsTile> tiles = new ArrayList<>();

	BattleshipsMap(MapImpl map) {
		this.map = map;
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
	public BattleshipsTile getTile(Location location) {
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
	public void updateTilePlayers(Player player, Coordinate coordinate) {
		for (BattleshipsTile tile : tiles) {
			if (tile.getLetter() == coordinate.getLetter() && tile.getNumber() == coordinate.getNumber()) {
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
			if (tile.getLetter() == coordinate.getLetter() && tile.getNumber() == coordinate.getNumber()) {
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
			if (tile.getLetter() == coordinate.getLetter() && tile.getNumber() == coordinate.getNumber()) {
				tile.setIsHit(isHit);
			}
		}
	}

	/**
	 * Using all the stored instances of {@link BattleshipsTile} from {@link #getTiles()}, creates a new list of
	 * coordinates from their location values. There will be no duplicate {@link Coordinate} values as coordinate is
	 * designed to only allow 1 instance of itself with any given values, all values are unique.
	 *
	 * @return the created list of coordinates
	 */
	public List<Coordinate> getCoordinates() {
		List<Coordinate> coordinates = new ArrayList<>();
		for (BattleshipsTile tile : tiles) {
			Coordinate coordinate = Coordinate.getCoordinate(tile.getLetter(), tile.getNumber());
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

	/**
	 * @return the Bukkit world associated with this map
	 */
	public World getWorld() {
		return map.getWorld();
	}

}
