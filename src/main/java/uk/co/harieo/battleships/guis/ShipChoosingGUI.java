package uk.co.harieo.battleships.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import uk.co.harieo.FurCore.guis.GUI;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.battleships.BattleshipAbility;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.ships.Battleship;

public class ShipChoosingGUI extends GUI {

	private static final Map<GamePlayer, Battleship> selections = new HashMap<>();

	private GamePlayer gamePlayer;

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

		ItemStack resetItem = new ItemStack(Material.RED_STAINED_GLASS);
		ItemMeta meta = resetItem.getItemMeta();
		meta.setDisplayName(ChatColor.RED + ChatColor.BOLD.toString() + "Random Ship");
		meta.setLore(Arrays.asList("",
				ChatColor.WHITE + "Get a " + ChatColor.YELLOW + "Random Ship " + ChatColor.WHITE + "on game start", "",
				ChatColor.YELLOW + "Click to Select"));
		resetItem.setItemMeta(meta);
		setItem((6 * 9) - 1, resetItem); // Placed on last slot, zero indexed
	}

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

	private ItemStack getSelectItem(Battleship battleship) {
		boolean isSelected = hasMadeSelection(gamePlayer) && getSelection(gamePlayer).equals(battleship);
		ItemStack item = new ItemStack(isSelected ? Material.GREEN_STAINED_GLASS : Material.BLUE_STAINED_GLASS);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Select the " + battleship.getName());
		meta.setLore(Arrays.asList("",
				isSelected ? ChatColor.GREEN + "You have this ship selected!" : ChatColor.YELLOW + "Click to Select"));
		item.setItemMeta(meta);
		return item;
	}

	public static boolean hasMadeSelection(GamePlayer gamePlayer) {
		return selections.containsKey(gamePlayer);
	}

	public static Battleship getSelection(GamePlayer gamePlayer) {
		return selections.get(gamePlayer);
	}

	public static Map<GamePlayer, Battleship> getSelections() {
		return selections;
	}

}
