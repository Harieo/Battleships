package uk.co.harieo.battleships.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.Map;
import uk.co.harieo.FurCore.FurCore;
import uk.co.harieo.FurCore.items.InteractiveItem;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.utils.PlayerUtils;
import uk.co.harieo.battleships.BattleshipAbility;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.ships.Battleship;
import uk.co.harieo.battleships.ships.ShipStore;
import uk.co.harieo.battleships.tasks.InGameTasks;

public class AbilityItem extends InteractiveItem {

	private int slot;
	private Battleships game;
	private BattleshipAbility ability;
	private GamePlayer gamePlayer;

	private boolean mayUpdate;

	public AbilityItem(int slot, Player player) {
		super(Material.BLAZE_POWDER);
		this.slot = slot;

		this.game = Battleships.getInstance();
		GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
		this.gamePlayer = gamePlayer;

		if (gamePlayer.isPlaying() && gamePlayer.hasTeam()) {
			Battleship battleship = ShipStore.get(gamePlayer.getTeam()).getShip(gamePlayer);
			if (battleship != null) {
				this.ability = BattleshipAbility.fromBattleship(battleship);
				this.mayUpdate = true;
				updateName();
				updateLore();
				return;
			}
		}

		this.mayUpdate = false;
		setName(ChatColor.RED + ChatColor.BOLD.toString() + "No Ability " + ChatColor.GRAY + FurCore.ARROWS
				+ " Error");
	}

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		if (mayUpdate) {
			ChatModule module = Battleships.getInstance().chatModule();
			Player player = event.getPlayer();
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			if (BattleshipAbility.hasUsedAbility(gamePlayer)) {
				player.sendMessage(module.formatSystemMessage(ChatColor.RED + "You have already used your ability!"));
			} else if (!InGameTasks.getTaskManager().getCurrentlyPlaying().equals(gamePlayer.getTeam())) {
				player.sendMessage(module.formatSystemMessage(
						"Your ability would be wasted here, wait until " + ChatColor.YELLOW + "your turn!"));
			} else if (BattleshipAbility.isAbilityActive(ability, gamePlayer.getTeam())) {
				player.sendMessage(module.formatSystemMessage(
						"Your team is " + ChatColor.RED + "already using " + ChatColor.WHITE + "that ability!"));
			} else {
				BattleshipAbility.addActiveAbility(gamePlayer, ability, gamePlayer.getTeam());

				Team enemy = gamePlayer.getTeam().equals(game.getBlueTeam()) ? game.getRedTeam()
						: game.getBlueTeam();
				// This should come before so that the system explains why the enemy chat was cleared after it
				if (ability.equals(BattleshipAbility.SHORT_CIRCUIT)) {
					if (gamePlayer.getTeam().equals(game.getBlueTeam())) {
						enemy = game.getRedTeam();
					} else {
						enemy = game.getBlueTeam();
					}

					for (GamePlayer enemyPlayer : enemy.getTeamMembers()) {
						for (int i = 0; i < 50; i++) {
							enemyPlayer.toBukkit().sendMessage("");
						}
					}
				}

				if (ability.shouldShowAbilityOnUse()) { // Some abilities may not work if the enemy knows about them
					Bukkit.broadcastMessage("");
					Bukkit.broadcastMessage(module.formatSystemMessage(
							"The " + gamePlayer.getTeam().getFormattedName() + ChatColor.WHITE + " has activated the "
									+ ChatColor.GOLD + ability.getName() + ChatColor.WHITE + " ability!"));
					Bukkit.broadcastMessage(module.formatSystemMessage(ChatColor.GRAY + ability.getDescription()));
					Bukkit.broadcastMessage("");
					PlayerUtils.playLocalizedSound(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
				}

				updateName(); // Show that the ability has been activated to the user

				if (ability.equals(BattleshipAbility.AERIAL_RECON)) { // This one we can do here as it is information
					// We want the enemy ships
					ShipStore store = ShipStore.get(enemy);
					Map<GamePlayer, Battleship> enemyShips = store.getShips();
					enemyShips.forEach((enemyPlayer, enemyShip) -> {
						if (store.isDestroyed(enemyPlayer)) {
							player.sendMessage(module.formatSystemMessage(
									"Your team has destroyed " + enemyShip.getChatColor() + "1 " + enemyShip
											.getName()));
						} else {
							player.sendMessage(module.formatSystemMessage(
									"Your team has NOT yet destroyed an enemy " + enemyShip.getFormattedName()));
						}
					});
				}
			}
		}
	}

	private void updateName() {
		if (mayUpdate) {
			String name;
			ChatColor primaryColor;
			if (BattleshipAbility.hasUsedAbility(gamePlayer)) {
				name = "Ability Used " + ChatColor.GRAY + FurCore.ARROWS + " Unable to Activate";
				primaryColor = ChatColor.RED;
			} else if (ShipStore.get(gamePlayer.getTeam()).isDestroyed(gamePlayer)) {
				name = "Ship Destroyed " + ChatColor.GRAY + FurCore.ARROWS + " Ability Offline";
				primaryColor = ChatColor.RED;
			} else {
				name = ability.getName() + InteractiveItem.RIGHT_CLICK_SUFFIX;
				primaryColor = ChatColor.GREEN;
			}

			setName(primaryColor + ChatColor.BOLD.toString() + name);
			gamePlayer.toBukkit().getInventory().setItem(slot, getItem());
		}
	}

	private void updateLore() {
		if (mayUpdate) {
			setLore(Arrays.asList("", ChatColor.GRAY + ability.getDescription(), "",
					ChatColor.RED + ChatColor.BOLD.toString() + "May only be activated once"));
			gamePlayer.toBukkit().getInventory().setItem(slot, getItem());
		}
	}

}
