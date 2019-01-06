package uk.co.harieo.battleships.ships;

import org.bukkit.ChatColor;

public enum Battleship {

	FRIGATE("Frigate", 2, 2, ChatColor.YELLOW),
	CRUISER("Cruiser", 3, ChatColor.GOLD),
	DREADNOUGHT("Dreadnought", 4, ChatColor.LIGHT_PURPLE),
	AIRCRAFT_CARRIER("Aircraft Carrier", 5, ChatColor.RED);

	private String name;
	private int size;
	private int max;
	private ChatColor chatColor;

	/**
	 * A ship holding values for use in-game
	 *
	 * @param name of the ship to be displayed to players
	 * @param size of the ship (how many squares it takes up)
	 * @param maxPerGame how many of this ship may be present in 1 game
	 * @param chatColor to be applied to the name to create {@link #getFormattedName()}
	 */
	Battleship(String name, int size, int maxPerGame, ChatColor chatColor) {
		this.name = name;
		this.size = size;
		this.max = maxPerGame;
		this.chatColor = chatColor;
	}

	Battleship(String name, int size, ChatColor chatColor) {
		this(name, size, 1, chatColor);
	}

	public String getName() {
		return name;
	}

	public String getFormattedName() {
		return chatColor + name;
	}

	public int getSize() {
		return size;
	}

	public int getMaxPerGame() {
		return max;
	}

}
