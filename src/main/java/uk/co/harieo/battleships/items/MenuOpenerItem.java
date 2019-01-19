package uk.co.harieo.battleships.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import uk.co.harieo.FurCore.items.InteractiveItem;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.guis.ShipPlacementGUI;
import uk.co.harieo.battleships.tasks.InGameTasks;
import uk.co.harieo.battleships.tasks.RoundTasks;

public class MenuOpenerItem extends InteractiveItem {

	public MenuOpenerItem() {
		super(Material.IRON_SWORD);
		setName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Open Battle Interface" + InteractiveItem.RIGHT_CLICK_SUFFIX);
	}

	public void onRightClick(PlayerInteractEvent event) {
		Battleships game = Battleships.getInstance();
		ChatModule module = game.chatModule();

		Player player = event.getPlayer();
		GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);

		if (gamePlayer.isPlaying() && gamePlayer.hasTeam()) {
			Team team = gamePlayer.getTeam();
			if (game.getState() == GameState.PRE_GAME) {
				ShipPlacementGUI gui = ShipPlacementGUI.get(gamePlayer);
				if (gui.getGui().isRegistered()) { // This means they can still place their ship
					player.openInventory(gui.getInventory());
				} else {
					player.sendMessage(module.formatSystemMessage(
							"It's too late to place your ship, " + ChatColor.RED + "the battle is starting!"));
				}
			} else if (game.getState() == GameState.IN_GAME) {
				RoundTasks taskManager = InGameTasks.getTaskManager();
				if (taskManager != null) {
					if (taskManager.getCurrentlyPlaying().equals(team) && taskManager.getVote(team).isOpen()) {
						player.openInventory(taskManager.getGUI(team).getInventory());
					} else {
						player.openInventory(taskManager.getPassiveGUI(team).getInventory());
					}
				}
			}
		}
	}

}
