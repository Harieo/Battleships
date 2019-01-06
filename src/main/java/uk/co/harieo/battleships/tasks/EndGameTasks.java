package uk.co.harieo.battleships.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.timers.GenericTimer;
import uk.co.harieo.battleships.Battleships;

class EndGameTasks {

	static void beginEndGame(Battleships game, Team winners) {
		game.getLogger().info("Beginning end-game tasks");
		game.setState(GameState.END_GAME);

		GenericTimer endTimer = new GenericTimer(game, 10, end -> Bukkit.getServer().shutdown());

		closeAllInventories();

		Consumer<Firework> fireworkMeta = firework -> {
			FireworkMeta meta = firework.getFireworkMeta();
			Color color = winners.equals(game.getBlueTeam()) ? Color.BLUE : Color.RED;
			FireworkEffect effect = FireworkEffect.builder().with(Type.BURST).withColor(color).trail(true).build();
			meta.addEffect(effect);
			meta.setPower(1);
			firework.setFireworkMeta(meta);
		};

		endTimer.setOnRun(timeLeft -> {
			if (timeLeft == 10) {
				spawnFireworks(winners, fireworkMeta);
			} else if (timeLeft == 5) {
				broadcastAdvert(game);
			}
		});

		endTimer.beginTimer();
	}

	private static void closeAllInventories() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.getOpenInventory().close();
		}
	}

	private static void spawnFireworks(Team winners, Consumer<Firework> updateFirework) {
		for (GamePlayer gamePlayer : winners.getTeamMembers()) {
			Player player = gamePlayer.toBukkit();
			if (player.isOnline()) { // The chances of a player leaving during end game are astronomical
				Firework firework = (Firework) player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK);
				updateFirework.accept(firework);
			}
		}
	}

	private static void broadcastAdvert(Battleships game) {
		ChatModule module = game.chatModule();
		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(module.formatSystemMessage(ChatColor.GREEN + "Thanks for playing!"));
		Bukkit.broadcastMessage(module.formatSystemMessage("Want to support this project and get some extra perks?"));
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.spigot().sendMessage(
					new ComponentBuilder(module.getPrefix() + " ").append("Check out our Patreon: ").color(ChatColor.YELLOW)
							.append("patreon.com/harieo").color(ChatColor.GOLD).underlined(true)
							.event(new ClickEvent(Action.OPEN_URL, "https://www.patreon.com/harieo"))
							.create());
		}
	}

}
