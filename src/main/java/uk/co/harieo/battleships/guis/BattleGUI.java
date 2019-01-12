package uk.co.harieo.battleships.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;
import uk.co.harieo.FurCore.guis.GUI;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;
import uk.co.harieo.battleships.ships.Battleship;
import uk.co.harieo.battleships.ships.ShipStore;

public class BattleGUI extends GUI {

	private Team displayTeam;
	private BattleshipsMap map;
	private boolean showShips;

	private Map<Integer, Coordinate> coordinates = new HashMap<>();

	private Consumer<InventoryClickEvent> onClick;
	private Consumer<InventoryCloseEvent> onClose;

	/**
	 * An extension of {@link GUI} that shows all the ships on one team's side of the board by {@link Coordinate}
	 *
	 * @param displayTeam to show the board of
	 * @param map that the {@link Coordinate} will be based on
	 * @param showShips whether to show where the ships are on this side of the board
	 */
	public BattleGUI(Team displayTeam, BattleshipsMap map, boolean showShips) {
		super(displayTeam.getFormattedName() + "'s Board",
				(map.getHighestY() >= 5 ? map.getHighestY() : 5)); // Y axis will be the one going down each row
		this.displayTeam = displayTeam;
		this.map = map;
		this.showShips = showShips;
		for (Coordinate coordinate : map.getCoordinates(displayTeam)) {
			getSlot(coordinate); // This loads the coordinate into the cache
		}
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		if (onClick != null) {
			onClick.accept(event);
		}
	}

	/**
	 * Sets a function to occur when an item in this GUI is clicked
	 *
	 * @param onClick the event consumer
	 */
	public void setOnClick(Consumer<InventoryClickEvent> onClick) {
		this.onClick = onClick;
	}

	@Override
	public void onClose(InventoryCloseEvent event) {
		if (onClose != null) {
			onClose.accept(event);
		}
	}

	/**
	 * Sets a function to occur when the inventory is closed
	 *
	 * @param onClose the event consumer
	 */
	public void setOnClose(Consumer<InventoryCloseEvent> onClose) {
		this.onClose = onClose;
	}

	/**
	 * @return which team this GUI is displaying
	 */
	public Team getDisplayTeam() {
		return displayTeam;
	}

	/**
	 * Sets all {@link Coordinate} spaces belonging to the {@link #getDisplayTeam()} by getting the item from {@link
	 * #createItem(Coordinate)}
	 */
	public void setFleetItems() {
		getInventory().clear();
		List<Coordinate> coordinates = map.getCoordinates(displayTeam);
		for (Coordinate coordinate : coordinates) {
			setItem(getSlot(coordinate), createItem(coordinate));
		}
	}

	/**
	 * Creates a generic item showing who owns the {@link Coordinate} and whether the {@link Coordinate} is hit, missed
	 * or not hit. If {@link #showShips} is true, it will create a special item for any {@link Coordinate} that a ship
	 * is placed on.
	 *
	 * @param coordinate to create an item for
	 * @return the created item
	 */
	public ItemStack createItem(Coordinate coordinate) {
		ItemStack item = new ItemStack(Material.LIGHT_BLUE_TERRACOTTA);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(
				ChatColor.BLUE + ChatColor.BOLD.toString() + Character.toUpperCase(coordinate.getLetter())
						+ coordinate.getNumber());

		String status;
		if (map.isHit(coordinate)) {
			if (map.getShip(coordinate) != null) {
				status = ChatColor.GOLD + ChatColor.BOLD.toString() + "Hit";
				item.setType(Material.GREEN_WOOL);
			} else {
				status = ChatColor.BLUE + ChatColor.BOLD.toString() + "Miss";
				item.setType(Material.RED_WOOL);
			}
		} else {
			status = ChatColor.GREEN + ChatColor.BOLD.toString() + "Not Hit";
		}

		Battleship ship = map.getShip(coordinate);
		String ownedByText = ChatColor.WHITE + "Owned by the " + displayTeam.getFormattedName();

		if (ship != null && showShips) {
			item.setType(Material.ORANGE_TERRACOTTA);
			meta.setLore(Arrays.asList("", ship.getFormattedName(), ownedByText, "",
					ChatColor.WHITE + "Status: " + status));
		} else {
			meta.setLore(Arrays.asList("", ownedByText, "",
					ChatColor.WHITE + "Status: " + status));
		}
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Gets the slot that a {@link Coordinate} should be placed in
	 *
	 * @param coordinate to get the slot for
	 * @return the slot that the given Coordinate should go into
	 */
	public int getSlot(Coordinate coordinate) {
		int slot = 0;
		slot += coordinate.getNumber() - 1; // Representing the X value -1 to bring down to 0
		slot += (((int) coordinate.getLetter()) - 96) * 9; // Representing the Y value in lower case
		slot -= 9; // Take away 9 to bring back to zero index from Y value
		if (!coordinates.containsKey(slot)) {
			coordinates.put(slot, coordinate);
		}
		return slot;
	}

	/**
	 * Retrieves the {@link Coordinate} that would go into {@link #getSlot(Coordinate)} to get the given slot
	 *
	 * @param slot to get the Coordinate of
	 * @return the attached Coordinate or null if the Coordinate has no slot in this GUI
	 */
	public Coordinate getCoordinate(int slot) {
		return coordinates.get(slot);
	}

	/**
	 * Whether an entire {@link Battleship} can be placed on the given {@link Coordinate} assuming that the {@link
	 * Coordinate} given is the center of the ship
	 *
	 * @param ship to be placed
	 * @param centralCoordinate center of the ship
	 * @param isHorizontal whether the ship is being placed horizontally or vertically
	 */
	boolean canPlaceShip(Battleship ship, Coordinate centralCoordinate, boolean isHorizontal) {
		// The list of coordinates will exclude invalid coordinates meaning if an invalid coordinate was excluded,
		// the size would differ from list to ship
		return getCoordinateSpread(ship, centralCoordinate, isHorizontal).size() == ship.getSize();
	}

	/**
	 * Updates all tiles attached to all {@link Coordinate}s to show that a ship is placed on it. This method will do
	 * nothing if {@link #canPlaceShip(Battleship, Coordinate, boolean)} returns false with the given values.
	 *
	 * @param player that this ship belongs to
	 * @param centralCoordinate center of the ship
	 * @param isHorizontal whether this ship is being placed horizontally or vertically
	 */
	void placeShip(GamePlayer player, Coordinate centralCoordinate, boolean isHorizontal) {
		Battleship ship = ShipStore.get(player.getTeam()).getShip(player);
		if (canPlaceShip(ship, centralCoordinate, isHorizontal)) {
			for (Coordinate coordinate : getCoordinateSpread(ship, centralCoordinate, isHorizontal)) {
				map.updateTilePlayers(player, coordinate); // Updates tiles to show this coordinate is owned
				map.updateTileShips(ship, coordinate);
			}
		}
	}

	/**
	 * Gets a list of {@link Coordinate} that the ship will be placed on, excluding invalid values that are already
	 * owned by another. By this logic, if the given list is of a size lower then the size of your {@link Battleship}
	 * then one slot on it's spread was invalid.
	 *
	 * @param ship that you wish to place
	 * @param centralCoordinate of the location you're placing it
	 * @param isHorizontal whether you're placing it horizontally or vertically
	 * @return the list of coordinate from the center that the ship can be placed on
	 */
	private List<Coordinate> getCoordinateSpread(Battleship ship, Coordinate centralCoordinate, boolean isHorizontal) {
		List<Coordinate> list = new ArrayList<>();
		list.add(centralCoordinate); // This is included in the spread

		boolean goHigher = true;
		int modifier = 1;
		for (int i = 1; i < ship.getSize(); ) {
			int slot;
			if (goHigher) {
				if (isHorizontal) {
					slot = getSlot(centralCoordinate) + modifier;
				} else {
					slot = getSlot(centralCoordinate) + (modifier * 9); // Going vertically
				}
			} else {
				if (isHorizontal) {
					slot = getSlot(centralCoordinate) - modifier;
				} else {
					slot = getSlot(centralCoordinate) - (modifier * 9);
				}
				modifier++;
			}
			i++;

			goHigher = !goHigher; // This creates an effect that makes the central coordinate central

			if (getCoordinate(slot) == null || !map.isApplicableCoordinate(displayTeam, getCoordinate(slot)) || slot < 0
					|| slot >= getInventory().getSize() || map.getOwningPlayer(getCoordinate(slot)) != null) {
				continue;
			}

			list.add(getCoordinate(slot));
		}

		return list;
	}

}
