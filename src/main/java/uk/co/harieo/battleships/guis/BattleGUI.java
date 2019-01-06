package uk.co.harieo.battleships.guis;

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

	public BattleGUI(Team displayTeam, BattleshipsMap map, boolean showShips) {
		super(displayTeam.getFormattedName() + "'s Board",
				(map.getHighestY() >= 5 ? map.getHighestY() : 5)); // Y axis will be the one going down each row

		this.displayTeam = displayTeam;
		this.map = map;
		this.showShips = showShips;
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		if (onClick != null) {
			onClick.accept(event);
		}
	}

	public void setOnClick(Consumer<InventoryClickEvent> onClick) {
		this.onClick = onClick;
	}

	@Override
	public void onClose(InventoryCloseEvent event) {
		if (onClose != null) {
			onClose.accept(event);
		}
	}

	public void setOnClose(Consumer<InventoryCloseEvent> onClose) {
		this.onClose = onClose;
	}

	public Team getDisplayTeam() {
		return displayTeam;
	}

	public void setFleetItems() {
		getInventory().clear();
		List<Coordinate> coordinates = map.getCoordinates(displayTeam);
		for (Coordinate coordinate : coordinates) {
			setItem(getSlot(coordinate), createItem(coordinate));
		}
	}

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

	// char 'a' is 97
	public int getSlot(Coordinate coordinate) {
		int slot = 0;
		slot += coordinate.getNumber() - 1; // Representing the X value -1 to bring down to 0
		slot += (((int) coordinate.getLetter()) - 96) * 9; // Representing the Y value in lower case
		slot -= 9; // Take away 9 to bring back to zero index from Y value
		coordinates.put(slot, coordinate);
		return slot;
	}

	public Coordinate getCoordinate(int slot) {
		return coordinates.get(slot);
	}

	boolean canPlaceShip(Battleship ship, Coordinate centralCoordinate, boolean isHorizontal) {
		// The list of coordinates will exclude invalid coordinates meaning if an invalid coordinate was excluded,
		// the size would differ from list to ship
		return getCoordinateSpread(ship, centralCoordinate, isHorizontal).size() == ship.getSize();
	}

	void placeShip(GamePlayer player, Coordinate centralCoordinate, boolean isHorizontal) {
		Battleship ship = ShipStore.get(player.getTeam()).getShip(player);
		if (canPlaceShip(ship, centralCoordinate, isHorizontal)) {
			for (Coordinate coordinate : getCoordinateSpread(ship, centralCoordinate, isHorizontal)) {
				map.updateTilePlayers(player, coordinate); // Updates tiles to show this coordinate is owned
				map.updateTileShips(ship, coordinate);
			}
		}
	}

	private List<Coordinate> getCoordinateSpread(Battleship ship, Coordinate centralCoordinate, boolean isHorizontal) {
		List<Coordinate> list = new ArrayList<>();
		list.add(centralCoordinate); // This is included in the spread

		boolean goHigher = true;
		for (int i = 1; i < ship.getSize(); i++) {
			int slot;
			if (goHigher) {
				if (isHorizontal) {
					slot = getSlot(centralCoordinate) + i;
				} else {
					slot = getSlot(centralCoordinate) + (i * 9); // Going vertically
				}
			} else {
				if (isHorizontal) {
					slot = getSlot(centralCoordinate) - (i - 1);
				} else {
					slot = getSlot(centralCoordinate) - ((i - 1) * 9);
				}
			}

			if (!map.isApplicableCoordinate(displayTeam, getCoordinate(slot)) || slot < 0
					|| slot >= getInventory().getSize() || map.getOwningPlayer(getCoordinate(slot)) != null) {
				continue;
			}

			list.add(getCoordinate(slot));
			goHigher = !goHigher; // This creates an effect that makes the central coordinate central
		}

		return list;
	}

}
