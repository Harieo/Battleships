package uk.co.harieo.battleships.ships;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum Battleship {

	AIRCRAFT_CARRIER("Aircraft Carrier", 5, ChatColor.RED, Material.GRAY_TERRACOTTA),
	DREADNOUGHT("Dreadnought", 4, ChatColor.LIGHT_PURPLE, Material.PURPLE_TERRACOTTA),
	CRUISER("Cruiser", 3, ChatColor.GOLD, Material.ORANGE_TERRACOTTA),
	FRIGATE("Frigate", 2, 2, ChatColor.YELLOW, Material.YELLOW_TERRACOTTA);

	private String name;
	private int size;
	private int max;
	private ChatColor chatColor;
	private Material material;

	/**
	 * A ship holding values for use in-game
	 *
	 * @param name of the ship to be displayed to players
	 * @param size of the ship (how many squares it takes up)
	 * @param maxPerGame how many of this ship may be present in 1 game
	 * @param chatColor to be applied to the name to create {@link #getFormattedName()}
	 */
	Battleship(String name, int size, int maxPerGame, ChatColor chatColor, Material material) {
		this.name = name;
		this.size = size;
		this.max = maxPerGame;
		this.chatColor = chatColor;
		this.material = material;
	}

	Battleship(String name, int size, ChatColor chatColor, Material material) {
		this(name, size, 1, chatColor, material);
	}

	public String getName() {
		return name;
	}

	public ChatColor getChatColor() {
		return chatColor;
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

	public Material getMaterial() {
		return material;
	}

}
