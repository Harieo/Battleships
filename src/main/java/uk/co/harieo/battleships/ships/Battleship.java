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
	 * @param material that the ship should be displayed as in GUIs
	 */
	Battleship(String name, int size, int maxPerGame, ChatColor chatColor, Material material) {
		this.name = name;
		this.size = size;
		this.max = maxPerGame;
		this.chatColor = chatColor;
		this.material = material;
	}

	/**
	 * A ship holding values for use in-game with {@link #max} defaulted to 1
	 *
	 * @param name of the ship to be displayed to players
	 * @param size of the ship (how many squares it takes up)
	 * @param chatColor to be applied to the name to create {@link #getFormattedName()}
	 * @param material that the ship should be displayed as in GUIs
	 */
	Battleship(String name, int size, ChatColor chatColor, Material material) {
		this(name, size, 1, chatColor, material);
	}

	/**
	 * @return this Battleship's name that can be shown to players
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the colour that this ship should be identified as when displayed with text
	 */
	public ChatColor getChatColor() {
		return chatColor;
	}

	/**
	 * @return a concatenation of {@link #getChatColor()} plus {@link #getName()}
	 */
	public String getFormattedName() {
		return chatColor + name;
	}

	/**
	 * @return how many coordinates this ship consumes when placed
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return how many of this ship may be used in a game
	 */
	public int getMaxPerGame() {
		return max;
	}

	/**
	 * @return the material that this ship should be shown as in GUIs
	 */
	public Material getMaterial() {
		return material;
	}

}
