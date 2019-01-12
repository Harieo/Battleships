package uk.co.harieo.battleships.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.FurBridge.sql.InfoCore;
import uk.co.harieo.FurCore.achievements.AchievementsCore;
import uk.co.harieo.FurCore.guis.GUI;
import uk.co.harieo.battleships.achievements.BattleshipsAchievement;

public class AchievementGUI extends GUI {

	public AchievementGUI(Player player) {
		super("Achievements", (BattleshipsAchievement.values().length / 9) + 1);
		InfoCore.get(AchievementsCore.class, player.getUniqueId()).whenComplete((achievementsInfo, error1) -> {
			if (error1 != null) {
				error1.printStackTrace();
				player.sendMessage(ChatColor.RED + "An error occurred loading your achievement information!");
			} else if (achievementsInfo.hasErrorOccurred()) {
				player.sendMessage(
						ChatColor.RED + "An unknown error occurred loading your achievement information!");
			} else {
				BattleshipsAchievement[] achievements = BattleshipsAchievement.values();
				for (int i = 0; i < achievements.length; i++) {
					BattleshipsAchievement achievement = achievements[i];
					int progress = achievementsInfo.getProgressMade(achievement);

					Material material;
					ChatColor color;
					if (progress <= 0) {
						material = Material.RED_TERRACOTTA;
						color = ChatColor.RED;
					} else if (progress < achievement.getProgressMax()) {
						material = Material.YELLOW_TERRACOTTA;
						color = ChatColor.YELLOW;
					} else {
						material = Material.GREEN_TERRACOTTA;
						color = ChatColor.GREEN;
					}

					ItemStack item = new ItemStack(material);
					ItemMeta meta = item.getItemMeta();
					meta.setDisplayName(color + ChatColor.BOLD.toString() + achievement.getName());
					meta.setLore(Arrays.asList("", ChatColor.GRAY + achievement.getDescription(), "",
							ChatColor.WHITE + "Progress: " + color + progress + "/" + achievement
									.getProgressMax()));
					item.setItemMeta(meta);
					setItem(i, item);
				}
			}
		});
	}

	@Override
	public void onClick(InventoryClickEvent event) {

	}

	@Override
	public void onClose(InventoryCloseEvent event) {
		unregister(); // This inventory can never be shared so once it is closed, it is closed forever
	}

}
