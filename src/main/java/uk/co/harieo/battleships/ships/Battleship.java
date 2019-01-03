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
