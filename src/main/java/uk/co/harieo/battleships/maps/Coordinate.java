package uk.co.harieo.battleships.maps;

import java.util.ArrayList;
import java.util.List;

public class Coordinate {

	private static List<Coordinate> coordinates = new ArrayList<>();

	private char letter;
	private int number;

	private Coordinate(char letter, int number) {
		this.letter = letter;
		this.number = number;
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
	 * Gets a {@link Coordinate} from the cache or instantiates one if none found. This is to prevent the unnecessary
	 * creation of instances so that this can be safely used in maps (while saving a tiny bit of memory).
	 *
	 * @param letter of the coordinate
	 * @param number of the coordinate
	 * @return the coordinate found or created
	 */
	public static Coordinate getCoordinate(char letter, int number) {
		Coordinate coordinate = null;
		for (Coordinate cachedCoordinate : coordinates) {
			if (cachedCoordinate.getLetter() == letter && cachedCoordinate.getNumber() == number) {
				coordinate = cachedCoordinate;
			}
		}

		if (coordinate == null) {
			coordinate = new Coordinate(letter, number);
			coordinates.add(coordinate);
		}

		return coordinate;
	}

}
