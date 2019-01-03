package uk.co.harieo.battleships.maps;

import java.util.ArrayList;
import java.util.List;
import uk.co.harieo.GamesCore.teams.Team;

/**
 * This class represents a collection of {@link BattleshipsTile} under 1 coordinate on the board. This allows for multiple
 * locations to be collected into one {@link Coordinate} bound by {@link BattleshipsMap}.
 */
public class Coordinate {

	private static List<Coordinate> coordinates = new ArrayList<>();

	private char letter;
	private int number;
	private Team team;

	private Coordinate(char letter, int number, Team team) {
		this.letter = letter;
		this.number = number;
		this.team = team;
	}

	/**
	 * @return the letter value of this coordinate
	 */
	public char getLetter() {
		return letter;
	}

	/**
	 * @return the number value of this coordinate
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * @return the team value of this coordinate
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * Gets a {@link Coordinate} from the cache or instantiates one if none found. This is to prevent the unnecessary
	 * creation of instances so that this can be safely used in maps (while saving a tiny bit of memory).
	 *
	 * @param letter of the coordinate
	 * @param number of the coordinate
	 * @param team of the coordinate
	 * @return the coordinate found or created
	 */
	public static Coordinate getCoordinate(char letter, int number, Team team) {
		Coordinate coordinate = null;
		for (Coordinate cachedCoordinate : coordinates) {
			if (cachedCoordinate.getLetter() == letter && cachedCoordinate.getNumber() == number
					&& cachedCoordinate.getTeam().equals(team)) {
				coordinate = cachedCoordinate;
			}
		}

		if (coordinate == null) {
			coordinate = new Coordinate(letter, number, team);
			coordinates.add(coordinate);
		}

		return coordinate;
	}

	@Override
	public String toString() {
		return getLetter() + "" + getNumber();
	}

}
