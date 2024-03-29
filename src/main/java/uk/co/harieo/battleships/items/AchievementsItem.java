package uk.co.harieo.battleships.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import uk.co.harieo.FurCore.items.InteractiveItem;
import uk.co.harieo.battleships.guis.AchievementGUI;

public class AchievementsItem extends InteractiveItem {

	/**
	 * An extension of {@link InteractiveItem} that displays {@link AchievementGUI} to the player
	 */
	public AchievementsItem() {
		super(Material.BOOK);
		setName(ChatColor.GREEN + ChatColor.BOLD.toString() + "Achievements" + InteractiveItem.RIGHT_CLICK_SUFFIX);
	}

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		player.openInventory(new AchievementGUI(player).getInventory());
	}

}
