package uk.co.harieo.battleships.guis;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import uk.co.harieo.FurCore.guis.GUI;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.battleships.BattleshipAbility;
import uk.co.harieo.battleships.ships.Battleship;

public class ShipChoosingGUI extends GUI {

	private static final Map<GamePlayer, Battleship> selections = new HashMap<>();

	private GamePlayer gamePlayer;

	/**
	 * An extension of {@link GUI} that allows certain players to choose which ship they would like to use in-game
	 *
	 * @param gamePlayer that the GUI is being used for
	 */
	public ShipChoosingGUI(GamePlayer gamePlayer) {
		super("Choose your Ship", 6);
		this.gamePlayer = gamePlayer;
		updateItems();
	}

	@Override
	public void onClick(InventoryClickEvent event) {
		int slot = event.getSlot();
		if (slot == (6 * 9) - 1) { // This is the reset item
			selections.remove(gamePlayer);
			updateItems();
			return;
		}

		Battleship battleship = getShipFromSlot(slot);
		if (battleship == null) { // They probably clicked something else
			return;
		}

		if (selections.containsKey(gamePlayer)) {
			selections.replace(gamePlayer, battleship);
		} else {
			selections.put(gamePlayer, battleship);
		}
		updateItems();
	}

	@Override
	public void onClose(InventoryCloseEvent event) {
		unregister();
	}

	/**
	 * Retrieves the {@link Battleship} represented by a given slot, if any. If the slot does not represent any {@link
	 * Battleship} then this will return null.
	 *
	 * @param slot that is being checked
	 * @return the {@link Battleship} that this slot represents or null if not applicable
	 */
	@Nullable
	private Battleship getShipFromSlot(int slot) {
		if (slot < 45 || slot % 2 == 0) {
			return null;
		}

		int index = (slot - 45) / 2;
		if (index < Battleship.values().length) {
			return Battleship.values()[index];
		} else {
			return null;
		}
	}

	/**
	 * Updates all relevant items, displaying all the options the player has and a reset button to reset their
	 * selection
	 */
	private void updateItems() {
		Battleship[] values = Battleship.values();
		int slot = 0;
		int mediumIncrement = 2;
		for (int i = 0; i < values.length; i++) {
			Battleship battleship = values[i];
			ItemStack battleshipItem = getShipItem(battleship);
			for (int increment = 1; increment <= battleship.getSize(); increment++) {
				setItem(slot, battleshipItem);
				slot += 9;
			}
			slot = (9 * 5) + i + mediumIncrement - 2;
			setItem(slot, getSelectItem(battleship));
			slot = i + mediumIncrement; // Start at the correct column
			mediumIncrement++;
		}

		ItemStack resetItem = new ItemStack(Material.STAINED_GLASS);
		resetItem.setDurability((byte) 14);
		ItemMeta meta = resetItem.getItemMeta();
		meta.setDisplayName(ChatColor.RED + ChatColor.BOLD.toString() + "Random Ship");
		meta.setLore(Arrays.asList("",
				ChatColor.WHITE + "Get a " + ChatColor.YELLOW + "Random Ship " + ChatColor.WHITE + "on game start", "",
				ChatColor.YELLOW + "Click to Select"));
		resetItem.setItemMeta(meta);
		setItem((6 * 9) - 1, resetItem); // Placed on last slot, zero indexed
	}

	/**
	 * Gets a formatted instance of {@link ItemStack} that represents the specified {@link Battleship}
	 *
	 * @param battleship to get a formatted item for
	 * @return the formatted item
	 */
	private ItemStack getShipItem(Battleship battleship) {
		BattleshipAbility ability = BattleshipAbility.fromBattleship(battleship);

		ItemStack item = new ItemStack(battleship.getMaterial());
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(battleship.getFormattedName());
		meta.setLore(
				Arrays.asList("", ChatColor.WHITE + "This represents the size of the " + battleship.getFormattedName(),
						"", ChatColor.WHITE + "Ability: " + ChatColor.YELLOW + ability.getName(),
						ChatColor.GRAY + ability.getDescription()));
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Gets a formatted instance of {@link ItemStack} which will act as a button to select the specified {@link
	 * Battleship} and which displays the selection the player has made
	 *
	 * @param battleship to get the button for
	 * @return a formatted item
	 */
	private ItemStack getSelectItem(Battleship battleship) {
		boolean isSelected = hasMadeSelection(gamePlayer) && getSelection(gamePlayer).equals(battleship);
		ItemStack item = new ItemStack(Material.STAINED_GLASS);
		item.setDurability(isSelected ? (byte) 13 : (byte) 11);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Select the " + battleship.getName());
		meta.setLore(Arrays.asList("",
				isSelected ? ChatColor.GREEN + "You have this ship selected!" : ChatColor.YELLOW + "Click to Select"));
		item.setItemMeta(meta);
		return item;
	}

	/**
	 * Checks whether the specified {@link GamePlayer} has selected their ship or not. Selecting random will count
	 * as no selection (or false).
	 *
	 * @param gamePlayer to check the selection of
	 * @return whether the specified player has made a ship selection that is not random
	 */
	public static boolean hasMadeSelection(GamePlayer gamePlayer) {
		return selections.containsKey(gamePlayer);
	}

	/**
	 * Retrieves whichever {@link Battleship} the player has selected or null if {@link #hasMadeSelection(GamePlayer)}
	 * is false
	 *
	 * @param gamePlayer to get the selection of
	 * @return the selection the player has made or null if no selection was made
	 */
	public static Battleship getSelection(GamePlayer gamePlayer) {
		return selections.get(gamePlayer);
	}

	/**
	 * @return a Map of all players and their corresponding selections
	 */
	public static Map<GamePlayer, Battleship> getSelections() {
		return selections;
	}

}
