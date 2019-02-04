package uk.co.harieo.battleships.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurCore.items.InteractiveItem;
import uk.co.harieo.FurCore.ranks.RankCache;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.guis.ShipChoosingGUI;
import uk.co.harieo.battleships.utils.AdvertisementUtils;

public class ShipChoosingItem extends InteractiveItem {

	/**
	 * An extension of {@link InteractiveItem} which opens {@link ShipChoosingGUI} for the player
	 */
	public ShipChoosingItem() {
		super(Material.EMERALD);
		setName(ChatColor.GOLD + ChatColor.BOLD.toString() + "Choose your Ship" + InteractiveItem.RIGHT_CLICK_SUFFIX);
	}

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		Battleships game = Battleships.getInstance();
		Player player = event.getPlayer();
		GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
		ChatModule module = game.chatModule();
		if (RankCache.getCachedInfo(player).hasPermission(Rank.PATRON)) {
			player.openInventory(new ShipChoosingGUI(gamePlayer).getInventory());
		} else {
			AdvertisementUtils.sendAdvertisement(player, "Choose your Ship");
		}
	}

}
