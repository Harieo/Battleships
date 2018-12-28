package uk.co.harieo.battleships.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.battleships.Battleships;

public class BattleshipsChatModule implements ChatModule {

	public BattleshipsChatModule(JavaPlugin plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public String getPrefix() {
		// Alt 175 is for »
		return ChatColor.GOLD + ChatColor.BOLD.toString() + "Battleships " + ChatColor.DARK_GRAY + "»";
	}

	@Override
	@EventHandler
	public void announcePlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Game game = Battleships.getInstance();
		event.setJoinMessage(null);

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
							"If you find any bugs, please report them to Harieo via " + ChatColor.LIGHT_PURPLE
									+ "Discord"));
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
	}

}
