package uk.co.harieo.battleships.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import uk.co.harieo.FurBridge.players.PlayerInfo;
import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurBridge.rank.RankInfo;
import uk.co.harieo.FurBridge.sql.InfoCore;
import uk.co.harieo.FurCore.FurCore;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.battleships.Battleships;

public class ConnectionsListener implements Listener {

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		int playerCount = Bukkit.getOnlinePlayers().size();
		PlayerInfo.loadPlayerInfo(player.getName(), player.getUniqueId()).whenComplete((info, error) -> {
			if (error != null) {
				error.printStackTrace();
				event.setResult(Result.KICK_OTHER);
				event.setKickMessage(
						ChatColor.RED + "Failed to load your player information, please contact an Administrator");
				return;
			}

			InfoCore.get(RankInfo.class, info).whenComplete((rankInfo, error1) -> {
				if (error1 != null) {
					error1.printStackTrace();
				}

				if (!rankInfo.hasPermission(Rank.MODERATOR)) { // Staff may not be stopped
					Game game = Battleships.getInstance();
					// Accounts for playerCount + this extra player we're handling
					if (playerCount >= game.getMaximumPlayers()) {
						event.setResult(Result.KICK_FULL);
						event.setKickMessage(ChatColor.RED + "Server is totally full!");
					} else if (playerCount >= game.getMaximumPlayers() - game.getReservedSlots() && !rankInfo
							.hasPermission(Rank.PATRON)) {
						event.setResult(Result.KICK_FULL);
						event.setKickMessage(
								"We only have " + ChatColor.GREEN + "Reserved Slots " + ChatColor.WHITE + " left for "
										+ Rank.PATRON.getPrefix() + ChatColor.WHITE + "s or higher!");
					}
				}
			});
		});
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Battleships core = Battleships.getInstance();
		Player player = event.getPlayer();
		player.setGameMode(GameMode.ADVENTURE);
		player.setFoodLevel(20);

		if (core.getState() == GameState.LOBBY) {
			core.getLobbyScoreboard().render(core, event.getPlayer(), 1);
			player.teleport(FurCore.getInstance().getPrimaryWorld().getWorld().getSpawnLocation()); // Set by the core
		}
	}

}
