package uk.co.harieo.battleships.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.battleships.Battleships;

public class BattleshipsChatModule implements ChatModule {

	@Override
	public String getPrefix() {
		// Alt 175 is for »
		return ChatColor.GOLD + ChatColor.BOLD.toString() + "Battleships " + ChatColor.DARK_GRAY + "»";
	}

	/**
	 * Sends a broadcast announcing that a player has joined and gives that player messages about the server
	 *
	 * @param player to announce the join of
	 */
	@Override
	public void announcePlayerJoin(Player player) {
		Game game = Battleships.getInstance();

		player.sendMessage("");
		player.sendMessage(formatSystemMessage(
				"Welcome, " + ChatColor.GREEN + player.getName() + ChatColor.WHITE + ", to " + ChatColor.GOLD
						+ ChatColor.GOLD.toString() + "Battleships"));
		if (game.isBeta()) {
			player.sendMessage(
					formatSystemMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Warning: " + ChatColor.WHITE
							+ "This game is currently in " + ChatColor.YELLOW + ChatColor.BOLD.toString() + "Beta "
							+ ChatColor.WHITE + "and may contain bugs"));
			player.sendMessage(
					formatSystemMessage(
							"If you find any bugs, please report them to Harieo via "
									+ ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Discord"));
		}
		player.sendMessage("");

		Bukkit.broadcastMessage(
				formatSystemMessage(ChatColor.GREEN + player.getName() + ChatColor.WHITE + " has joined the game!"));

		int playerCount = Bukkit.getOnlinePlayers().size();
		if (playerCount < game.getMinimumPlayers()) {
			Bukkit.broadcastMessage(
					formatSystemMessage(
							"We need " + ChatColor.GREEN + (game.getMinimumPlayers() - playerCount) + " more "
									+ ChatColor.WHITE + "players to start the game..."));
		}
		Bukkit.broadcastMessage("");
	}

	/**
	 * Announces a player leaving the game
	 *
	 * @param player that is leaving
	 */
	public void announcePlayerLeave(Player player) {
		Game game = Battleships.getInstance();

		game.chatModule()
				.formatSystemMessage(ChatColor.GREEN + player.getName() + ChatColor.WHITE + " has left the game");
	}

}
