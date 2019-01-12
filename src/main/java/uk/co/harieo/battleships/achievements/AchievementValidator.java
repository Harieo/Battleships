package uk.co.harieo.battleships.achievements;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurBridge.sql.InfoCore;
import uk.co.harieo.FurCore.achievements.Achievable;
import uk.co.harieo.FurCore.achievements.AchievementsCore;
import uk.co.harieo.FurCore.ranks.RankCache;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;
import uk.co.harieo.battleships.tasks.PreGameTasks;

public class AchievementValidator {

	public static void checkEndGameAchievements(Team winners, boolean scoreWin) {
		BattleshipsMap map = Battleships.getInstance().getMap();
		for (GamePlayer gamePlayer : GamePlayerStore.instance(Battleships.getInstance()).getAll()) {
			Player player = gamePlayer.toBukkit();
			if (player.isOnline()) {
				InfoCore.get(AchievementsCore.class, player.getUniqueId()).whenComplete((achievementsInfo, error) -> {
					if (error != null) {
						error.printStackTrace();
						player.sendMessage(
								ChatColor.RED + "An error occurred checking if you'd completed any achievements");
					} else if (achievementsInfo.hasErrorOccurred()) {
						player.sendMessage(ChatColor.RED
								+ "An unknown error occurred checking if you'd completed any achievements");
					} else {
						List<BattleshipsAchievement> completedAchievements = new ArrayList<>();

						// 50 wins
						if (!achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.WINS_50) && gamePlayer
								.hasTeam() && gamePlayer.getTeam().equals(winners)) {
							incrementProgress(player, achievementsInfo, BattleshipsAchievement.WINS_50);
							// They may not have completed it yet
							if (achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.WINS_50)) {
								completedAchievements.add(BattleshipsAchievement.WINS_50);
							}
						}
						// 150 wins
						if (!achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.WINS_150) && gamePlayer
								.hasTeam() && gamePlayer.getTeam().equals(winners)) {
							incrementProgress(player, achievementsInfo, BattleshipsAchievement.WINS_150);
							// They may not have completed it yet
							if (achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.WINS_150)) {
								completedAchievements.add(BattleshipsAchievement.WINS_150);
							}
						}
						// Win without damage
						if (!achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.WIN_UNDAMAGED) && gamePlayer
								.hasTeam() && gamePlayer.getTeam().equals(winners)) {
							List<Coordinate> shipCoordinates = map.getCoordinates(winners).stream()
									.filter(coordinate -> {
										GamePlayer owner = map.getOwningPlayer(coordinate);
										return owner != null && owner.equals(gamePlayer);
									}).collect(Collectors.toList());

							boolean undamaged = true;

							for (Coordinate coordinate : shipCoordinates) {
								if (map.isHit(coordinate)) {
									undamaged = false;
								}
							}

							if (undamaged) {
								incrementProgress(player, achievementsInfo, BattleshipsAchievement.WIN_UNDAMAGED);
								completedAchievements.add(BattleshipsAchievement.WIN_UNDAMAGED);
							}
						}
						// Win a game by the enemy team leaving
						if (!achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.WIN_FORFEIT) && !scoreWin
								&& gamePlayer.hasTeam() && gamePlayer.getTeam().equals(winners)) {
							if (gamePlayer.hasTeam() && gamePlayer.getTeam().equals(winners)) {
								incrementProgress(player, achievementsInfo, BattleshipsAchievement.WIN_FORFEIT);
								completedAchievements.add(BattleshipsAchievement.WIN_FORFEIT);
							}
						}
						// Stay to the end of a full game
						if (!achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.COMPLETE_FULL_GAME)) {
							if (PreGameTasks.wasFullStart()) {
								incrementProgress(player, achievementsInfo, BattleshipsAchievement.COMPLETE_FULL_GAME);
								completedAchievements.add(BattleshipsAchievement.COMPLETE_FULL_GAME);
							}
						}
						// First game to stay to the end of
						if (!achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.COMPLETE_GAME)) {
							incrementProgress(player, achievementsInfo, BattleshipsAchievement.COMPLETE_GAME);
							completedAchievements.add(BattleshipsAchievement.COMPLETE_GAME);
						}
						// Play with an admin
						if (!achievementsInfo.hasUnlockedAchievement(BattleshipsAchievement.PLAY_WITH_ADMIN)) {
							for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
								if (RankCache.getCachedInfo(onlinePlayer).hasPermission(Rank.ADMINISTRATOR)) {
									incrementProgress(player, achievementsInfo, BattleshipsAchievement.PLAY_WITH_ADMIN);
									completedAchievements.add(BattleshipsAchievement.PLAY_WITH_ADMIN);
									break; // No point continuing after this
								}
							}
						}

						for (BattleshipsAchievement achievement : completedAchievements) {
							sendAchievementUnlockedMessage(achievement, player);
						}
					}
				});
			}
		}
	}

	private static void sendAchievementUnlockedMessage(BattleshipsAchievement achievement, Player player) {
		ChatModule module = Battleships.getInstance().chatModule();
		player.sendMessage("");
		player.sendMessage(
				module.formatSystemMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + "Achievement Completed"));
		player.sendMessage(module.formatSystemMessage(ChatColor.GRAY + achievement.getDescription()));
		player.sendMessage("");
		player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
	}

	private static void incrementProgress(Player player, AchievementsCore core, BattleshipsAchievement achievement) {
		core.setProgressMade(achievement, core.getProgressMade(achievement) + 1).whenComplete((success, error) -> {
			if (error != null) {
				error.printStackTrace();
				player.sendMessage(ChatColor.RED + "An error occurred giving you an achievement!");
			} else if (!success) {
				player.sendMessage(ChatColor.RED + "An error occurred giving you an achievement");
			}
		});
	}

}
