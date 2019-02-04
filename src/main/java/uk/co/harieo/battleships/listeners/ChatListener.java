package uk.co.harieo.battleships.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurCore.ranks.RankCache;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.BattleshipAbility;
import uk.co.harieo.battleships.Battleships;

public class ChatListener implements Listener {

	@EventHandler
	public void onAsyncChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		Battleships game = Battleships.getInstance();

		if (game.getState() != GameState.LOBBY && game.getState() != GameState.END_GAME) {
			GamePlayer gamePlayer = GamePlayerStore.instance(game).get(player);
			if (gamePlayer.hasTeam()) {
				Team enemy = game.getBlueTeam().equals(gamePlayer.getTeam()) ? game.getRedTeam() : game.getBlueTeam();
				if (BattleshipAbility.isAbilityActive(BattleshipAbility.SIGNAL_JAMMER, enemy)) {
					player.sendMessage(game.chatModule().formatSystemMessage(
							"The enemy has deployed a " + ChatColor.RED + BattleshipAbility.SIGNAL_JAMMER.getName()
									+ ChatColor.WHITE + " to stop your message!"));
					event.setMessage(ChatColor.MAGIC + event.getMessage());
					return;
				}

				// Removes any recipients who are not on the same team as the player
				event.getRecipients().removeIf(recipient -> {
					GamePlayer gameRecipient = GamePlayerStore.instance(game).get(recipient);
					return (!gameRecipient.hasTeam() || !gameRecipient.getTeam().equals(gamePlayer.getTeam()))
							&& !RankCache.getCachedInfo(gameRecipient.toBukkit()).hasPermission(Rank.MODERATOR)
							&& !RankCache.getCachedInfo(player).hasPermission(Rank.MODERATOR);
				});
			}
		}
	}

}
