package uk.co.harieo.battleships.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.battleships.Battleships;

public class AdvertisementUtils {

	private static final Cache<String, Player> cache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS)
			.build();

	/**
	 * Sends an advertisement for the Patreon to a player directed towards unlocking the specified feature. Caches
	 * adverts so features can only be advertised once every 15 seconds to prevent spam.
	 *
	 * @param player to send the advertisement to
	 * @param feature that they want to unlock
	 */
	public static void sendAdvertisement(Player player, String feature) {
		Player cached = cache.getIfPresent(feature);
		if (cached != null && cached.equals(player)) {
			return;
		}

		ChatModule module = Battleships.getInstance().chatModule();
		player.sendMessage("");
		player.sendMessage(module.formatSystemMessage("Want to be able to " + ChatColor.GREEN + feature));
		player.sendMessage(module.formatSystemMessage(
				"This feature is " + ChatColor.YELLOW + "only " + ChatColor.WHITE + "for the " + Rank.PATRON
						.getPrefix() + ChatColor.WHITE + "rank"));
		player.spigot().sendMessage(
				new ComponentBuilder(module.getPrefix() + " ").append("Get the rank on Patreon: ")
						.color(net.md_5.bungee.api.ChatColor.YELLOW)
						.append("patreon.com/harieo").color(net.md_5.bungee.api.ChatColor.GOLD).underlined(true)
						.event(new ClickEvent(Action.OPEN_URL, "https://www.patreon.com/harieo"))
						.create());
		player.sendMessage("");
		cache.put(feature, player);
	}

}
