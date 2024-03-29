package uk.co.harieo.battleships.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurCore.ranks.RankCache;

public class MiscListener implements Listener {

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!RankCache.getCachedInfo(event.getPlayer()).hasPermission(Rank.ADMINISTRATOR)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!RankCache.getCachedInfo(event.getPlayer()).hasPermission(Rank.ADMINISTRATOR)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHungerDiminish(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		event.setCancelled(true);
	}

}
