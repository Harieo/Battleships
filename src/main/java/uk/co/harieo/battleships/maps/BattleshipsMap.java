package uk.co.harieo.battleships.maps;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import uk.co.harieo.FurCore.maps.MapImpl;
import uk.co.harieo.GamesCore.teams.Team;

public class BattleshipsMap {

	private MapImpl map;
	private List<BattleshipsTile> tiles = new ArrayList<>();

	BattleshipsMap(MapImpl map) {
		this.map = map;
	}

	void addTile(BattleshipsTile tile) {
		tiles.add(tile);
	}

	public BattleshipsTile getTile(Location location) {
		for (BattleshipsTile tile : tiles) {
			if (tile.getLocation().equals(location)) {
				return tile;
			}
		}
		return null;
	}

	public List<BattleshipsTile> getTiles() {
		return tiles;
	}

	public World getWorld() {
		return map.getWorld();
	}

	public static class BattleshipsTile {

		private Location locations;
		private Team team;
		private String letter;
		private int number;

		BattleshipsTile(Location locations, Team team) {
			this.locations = locations;
			this.team = team;
		}

		public Location getLocation() {
			return locations;
		}

		public Team getTeam() {
			return team;
		}

		public String getLetter() {
			return letter;
		}

		void setLetter(String letter) {
			this.letter = letter;
		}

		public int getNumber() {
			return number;
		}

		void setNumber(int number) {
			this.number = number;
		}

	}

}
