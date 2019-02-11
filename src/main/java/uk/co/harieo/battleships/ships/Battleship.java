package uk.co.harieo.battleships.ships;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum Battleship {

	AIRCRAFT_CARRIER("Aircraft Carrier", 5, ChatColor.RED, (byte) 7),
	DREADNOUGHT("Dreadnought", 4, ChatColor.LIGHT_PURPLE, (byte) 10),
	CRUISER("Cruiser", 3, ChatColor.GOLD, (byte) 1),
	FRIGATE("Frigate", 2, 2, ChatColor.YELLOW, (byte) 4);

	private String name;
	private int size;
	private int max;
	private ChatColor chatColor;
	private byte materialColor;

	/**
	 * A ship holding values for use in-game
	 *
	 * @param name of the ship to be displayed to players
	 * @param size of the ship (how many squares it takes up)
	 * @param maxPerGame how many of this ship may be present in 1 game
	 * @param chatColor to be applied to the name to create {@link #getFormattedName()}
	 * @param durability to be applied to color the stained clay material
	 */
	Battleship(String name, int size, int maxPerGame, ChatColor chatColor, byte durability) {
		this.name = name;
		this.size = size;
		this.max = maxPerGame;
		this.chatColor = chatColor;
		this.materialColor = durability;
	}

	/**
	 * A ship holding values for use in-game with {@link #max} defaulted to 1
	 *
	 * @param name of the ship to be displayed to players
	 * @param size of the ship (how many squares it takes up)
	 * @param chatColor to be applied to the name to create {@link #getFormattedName()}
	 * @param durability to be applied to color the stained clay material
	 */
	Battleship(String name, int size, ChatColor chatColor, byte durability) {
		this(name, size, 1, chatColor, durability);
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
	@Deprecated
	public Material getMaterial() {
		return Material.STAINED_CLAY;
	}

	/**
	 * @return the durability value which represents the clay color of this ship
	 */
	public byte getDurabilityValue() {
		return materialColor;
	}

}
