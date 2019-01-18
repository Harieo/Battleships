package uk.co.harieo.battleships.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import app.ashcon.intake.Command;
import app.ashcon.intake.bukkit.parametric.annotation.Sender;
import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurCore.ranks.RankCache;
import uk.co.harieo.battleships.Battleships;

public class ForceStartCommand {

	@Command(aliases = {"forcestart", "start"},
			 desc = "Start a game forcefully")
	public void forceStart(@Sender Player player) {
		Battleships game = Battleships.getInstance();
		if (RankCache.getCachedInfo(player).hasPermission(Rank.ADMINISTRATOR)) {
			game.startGame();
		} else {
			player.sendMessage(game.chatModule().formatSystemMessage(
					"You must be an " + Rank.ADMINISTRATOR.getPrefix() + ChatColor.WHITE + " to use this!"));
		}
	}

}
