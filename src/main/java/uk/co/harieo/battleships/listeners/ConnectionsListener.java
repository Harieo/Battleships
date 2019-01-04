package uk.co.harieo.battleships.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import uk.co.harieo.FurBridge.players.PlayerInfo;
import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurBridge.rank.RankInfo;
import uk.co.harieo.FurBridge.sql.InfoCore;
import uk.co.harieo.FurCore.FurCore;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.battleships.Battleships;

public class ConnectionsListener implements Listener {

	private void delayedKick(Player player, String reason) {
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				player.kickPlayer(reason);
			}
		};
		runnable.runTaskLater(Battleships.getInstance(), 5);
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		int playerCount = Bukkit.getOnlinePlayers().size();
		Battleships core = Battleships.getInstance();

		PlayerInfo.loadPlayerInfo(player.getName(), player.getUniqueId()).whenComplete((info, error) -> {
			if (error != null) {
				error.printStackTrace();
				delayedKick(player, ChatColor.RED + "Failed to load your player information, please contact an Administrator");
				return;
			}

			InfoCore.get(RankInfo.class, info).whenComplete((rankInfo, error1) -> {
				if (error1 != null) {
					error1.printStackTrace();
					delayedKick(player, ChatColor.RED + "Unable to retrieve your rank information");
					return;
				}

				if (!rankInfo.hasPermission(Rank.MODERATOR)) { // Staff may not be stopped
					// Accounts for playerCount + this extra player we're handling
					if (playerCount >= core.getMaximumPlayers()) {
						delayedKick(player, ChatColor.RED + "Server is totally full!");
					} else if (playerCount >= core.getMaximumPlayers() - core.getReservedSlots() && !rankInfo
							.hasPermission(Rank.PATRON)) {
						delayedKick(player, "We only have " + ChatColor.GREEN + "Reserved Slots " + ChatColor.WHITE + " left for "
								+ Rank.PATRON.getPrefix() + ChatColor.WHITE + "s or higher!");
					} else if (core.getState() != GameState.LOBBY) {
						delayedKick(player, ChatColor.RED + "This game has already started!");
					} else {
						core.chatModule().announcePlayerJoin(player);
					}
				} else {
					core.chatModule().announcePlayerJoin(player);
				}
			});
		});

		event.setJoinMessage(null);
		player.setGameMode(GameMode.ADVENTURE);
		player.setFoodLevel(20);
		player.setAllowFlight(true);

		if (core.getState() == GameState.LOBBY) {
			core.getLobbyScoreboard().render(core, event.getPlayer(), 1);
			player.teleport(FurCore.getInstance().getPrimaryWorld().getWorld().getSpawnLocation()); // Set by the core
		} else {
			GamePlayer gamePlayer = GamePlayerStore.instance(core).get(player);
			gamePlayer.setIsPlaying(false); // They are a spectator now
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.setQuitMessage(null);
	}

}
