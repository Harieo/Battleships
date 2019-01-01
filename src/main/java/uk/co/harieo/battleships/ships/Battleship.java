package uk.co.harieo.battleships.ships;

public enum Battleship {

	FRIGATE("Frigate", 2),
	CRUISER("Cruiser", 3),
	DREADNOUGHT("Dreadnought", 4),
	AIRCRAFT_CARRIER("Aircraft Carrier", 5);

	private String name;
	private int size;

	// char 'a' is 97 so subtract 96 (note to self)
	Battleship(String name, int size) {
		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public int getSize() {
		return size;
	}

}
