package uk.co.harieo.battleships.maps;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Validate;
import uk.co.harieo.FurCore.maps.MapImpl;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.utils.ParsingUtils;
import uk.co.harieo.battleships.Battleships;

/**
 * Locations work on the principle that a location is plotted at one edge with an enclosing location at the other edge,
 * of which both locations are on the same Z or X value on a straight line (allowing them to be connected) within 50
 * blocks
 */
public class MapLoader {

	public static BattleshipsMap parseMap(MapImpl map) {
		Validate.isTrue(map.isValid());
		List<Location> rawBlueSpawn = map.getLocations("bluespawn");
		List<Location> rawRedSpawn = map.getLocations("redspawn");
		if (rawBlueSpawn.isEmpty() || rawRedSpawn.isEmpty()) {
			throw new IllegalArgumentException("Map does not contain either 'bluespawn' or 'redspawn' location");
		}

		BattleshipsMap battleshipsMap = new BattleshipsMap(map, rawBlueSpawn.get(0), rawRedSpawn.get(0));

		// Make a new map so we only scan values that are valid and can remove them when we're done (saving time)
		Map<Location, String> locations = map.getAllLocations();
		Map<Location, String> tileLocations = new HashMap<>();

		for (Location location : locations.keySet()) {
			String id = locations.get(location);
			String[] splitId = id.split(":");
			// All ids are split as [team]:[coordinate] for this game so we'll check for those
			if (splitId.length > 1 &&
					(splitId[0].equalsIgnoreCase("red") || splitId[0].equalsIgnoreCase("blue"))) {
				tileLocations.put(location, id); // Locations that can be used are added to the new map
			}
		}

		for (Location location : tileLocations.keySet()) {
			String firstId = tileLocations.get(location);
			Location matchingLocation = null;
			for (Location secondLocation : tileLocations.keySet()) {
				// Make sure not to match the same location to itself
				if (!location.equals(secondLocation) && firstId.equals(tileLocations.get(secondLocation))
						&& ParsingUtils.areFarInline(location, secondLocation)) {
					matchingLocation = secondLocation;
					break;
				}
			}

			if (matchingLocation != null) {
				List<Location> inbetweenLocations = ParsingUtils.parseInlineLocations(location, matchingLocation);
				Team team = parseTeam(firstId); // As ids are equal, the teams will be as well
				String[] splitId = firstId.split(":");

				for (Location inbetween : inbetweenLocations) {
					if (battleshipsMap.getTile(inbetween) != null) {
						setTileValues(splitId, battleshipsMap.getTile(inbetween));
					} else {
						BattleshipsTile tile = new BattleshipsTile(inbetween, team);
						setTileValues(splitId, tile);
						battleshipsMap.addTile(tile);
					}
				}
			} else {
				Battleships.getInstance().getLogger()
						.warning("Couldn't find match for " + ParsingUtils.formStringCoordinate(location));
			}
		}

		return battleshipsMap;
	}

	private static Team parseTeam(String id) {
		String[] split = id.split(":");
		if (split.length > 0) {
			Battleships game = Battleships.getInstance();
			return split[0].equalsIgnoreCase("red") ? game.getRedTeam() : game.getBlueTeam();
		} else {
			return null;
		}
	}

	private static void setTileValues(String[] splitId, BattleshipsTile tile) {
		if (ParsingUtils.isInteger(splitId[1])) {
			tile.setNumber(Integer.parseInt(splitId[1]));
		} else if (splitId[1].length() == 1) {
			tile.setLetter(splitId[1]);
		} else {
			throw new IllegalArgumentException(
					"Second part of valid location id had neither single letter nor was an integer");
		}
	}

}
