package uk.co.harieo.battleships.guis;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;
import uk.co.harieo.battleships.ships.Battleship;
import uk.co.harieo.battleships.ships.ShipStore;

public class ShipPlacementGUI {

	private static Map<GamePlayer, ShipPlacementGUI> CACHE = new HashMap<>();

	private GamePlayer player;
	private Team team;
	private BattleGUI gui;
	private BattleshipsMap map;
	private Battleship ship;

	private boolean isHorizontal = true;
	private boolean hasPlaced = false;

	private ShipPlacementGUI(GamePlayer player) {
		// We're throwing exceptions as these are game breaking errors
		this.player = player;
		if (!player.hasTeam()) {
			throw new IllegalStateException("Cannot create ship placement menu without a team");
		}
		this.team = player.getTeam();

		Battleship ship = ShipStore.get(team).getShip(player);
		if (ship == null) {
			throw new NullPointerException("Player has no ship");
		}
		this.ship = ship;

		this.map = Battleships.getInstance().getMap();

		this.gui = new BattleGUI(team, Battleships.getInstance().getMap());
		ChatModule module = Battleships.getInstance().chatModule();
		gui.setOnClick(event -> {
			if (event.getSlot() == 26) {
				isHorizontal = !isHorizontal; // Change to opposite of what this is set to
				updateAlignmentButton(); // Update the button to show that change
			} else if (event.getSlot() == 17) {
				if (hasPlaced) {
					map.resetCoordinates(player);
					player.toBukkit().sendMessage(module.formatSystemMessage(
							"Your ship has been removed from the board so that you can replace it!"));
					hasPlaced = false;
					updateAll();
				}
			} else if (event.getCurrentItem().getType() == Material.LIGHT_BLUE_TERRACOTTA) {
				Coordinate coordinate = gui.getCoordinate(event.getSlot());
				if (!hasPlaced) {
					if (gui.canPlaceShip(ship, coordinate, isHorizontal)) {
						gui.placeShip(player, coordinate, isHorizontal);
						updateAll();
						this.hasPlaced = true;
					} else {
						player.toBukkit().sendMessage(module
								.formatSystemMessage(ChatColor.RED + "There is no space for your ship here!"));
					}
				} else {
					player.toBukkit().sendMessage(
							module.formatSystemMessage(ChatColor.RED + "You've already placed your ship!"));
				}
			}
		});

		setupGUI(); // Sets the GUI options
		setupButtons();
	}

	public GamePlayer getPlayer() {
		return player;
	}

	public Team getTeam() {
		return team;
	}

	public boolean isPlaced() {
		return hasPlaced;
	}

	public BattleGUI getGui() {
		return gui;
	}

	public Inventory getInventory() {
		return gui.getInventory();
	}

	private void updateAlignmentButton() {
		ItemStack item = new ItemStack(Material.ARROW);
		ItemMeta meta = item.getItemMeta();

		String horizontal = ChatColor.YELLOW + ChatColor.BOLD.toString() + "Horizontal";
		String verical = ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Vertical";
		meta.setDisplayName(isHorizontal ? horizontal : verical);
		meta.setLore(
				Arrays.asList("", ChatColor.WHITE + "Change alignment to " + (isHorizontal ? verical : horizontal)));
		item.setItemMeta(meta);
		gui.setItem(26, item);
	}

	private void setupGUI() {
		gui.setFleetItems(); // Sets the board for this player's team
		Battleship battleship = ShipStore.get(team).getShip(player);
		if (battleship == null) {
			throw new NullPointerException("Player has not been assigned a Battleship yet");
		}

		ItemStack shipItem = new ItemStack(Material.ORANGE_TERRACOTTA); // Item to display this player's ship size
		ItemMeta meta = shipItem.getItemMeta();
		meta.setDisplayName(battleship.getFormattedName());
		meta.setLore(Arrays.asList("", ChatColor.WHITE + "This represents the size of your ship on the board",
				ChatColor.WHITE + "A " + battleship.getFormattedName() + ChatColor.WHITE + " is "
						+ ChatColor.YELLOW + battleship.getSize() + " spaces"));
		shipItem.setItemMeta(meta);

		int slot = 6; // Start at the 7th slot
		for (int i = 1; i <= battleship.getSize(); i++) {
			gui.setItem(slot, shipItem);
			slot += 9;
		}
	}

	private void setupButtons() {
		ItemStack resetButton = new ItemStack(Material.BARRIER);
		ItemMeta resetMeta = resetButton.getItemMeta();
		resetMeta.setDisplayName(ChatColor.RED + "Reset Placement");
		resetMeta.setLore(Arrays.asList("", ChatColor.WHITE + "Resets the position of your ship"));
		resetButton.setItemMeta(resetMeta);
		gui.setItem(17, resetButton);

		updateAlignmentButton();
	}

	public void randomlyAssign() {
		List<Coordinate> coordinates = map.getCoordinates(team);
		boolean horizontal = true;
		for (int i = 0; i < coordinates.size(); i++) {
			Coordinate coordinate = coordinates.get(i);
			// On last loop, if the loop has only commenced once, reset back to start and change horizontal
			if (i + 1 >= coordinates.size() && horizontal) {
				i = 0;
				horizontal = false;
			}

			if (gui.canPlaceShip(ship, coordinate, horizontal)) {
				Player bukkitPlayer = player.toBukkit();
				gui.placeShip(player, coordinate, horizontal);
				updateAll();
				if (bukkitPlayer.isOnline()) {
					bukkitPlayer.sendMessage(Battleships.getInstance().chatModule().formatSystemMessage(
							"Your " + ship.getFormattedName() + ChatColor.WHITE
									+ " has been randomly placed on the board"));
				}
				this.hasPlaced = true;
				return; // Once we've placed the ship, we don't need to keep searching
			}
		}

		throw new IllegalStateException("Out of places to put a ship");
	}

	public static ShipPlacementGUI get(GamePlayer player) {
		if (CACHE.containsKey(player)) {
			return CACHE.get(player);
		} else {
			ShipPlacementGUI gui = new ShipPlacementGUI(player);
			CACHE.put(player, gui);
			return gui;
		}
	}

	private static void updateAll() {
		for (ShipPlacementGUI gui : CACHE.values()) {
			gui.getGui().setFleetItems(); // This will update the ships
			gui.setupGUI();
			gui.setupButtons();
		}
	}

}
