package uk.co.harieo.battleships.commands;

import org.bukkit.entity.Player;

import app.ashcon.intake.Command;
import app.ashcon.intake.bukkit.parametric.annotation.Sender;
import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.FurBridge.rank.Rank;
import uk.co.harieo.FurCore.ranks.RankCache;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.utils.AdvertisementUtils;

public class SwitchTeamCommand {

	@Command(aliases = {"team", "selectteam"},
			 desc = "Switch your team")
	public void switchTeam(@Sender Player sender, String rawTeam) {
		Battleships game = Battleships.getInstance();
		ChatModule module = game.chatModule();

		if (RankCache.getCachedInfo(sender).hasPermission(Rank.PATRON)) {
			GamePlayer player = GamePlayerStore.instance(game).get(sender);
			Team team;
			if (rawTeam.equalsIgnoreCase("blue")) {
				team = game.getBlueTeam();
			} else {
				team = game.getRedTeam();
			}

			if (team.getTeamMembers().size() >= game.getMaximumPlayers() / 2) {
				sender.sendMessage(module.formatSystemMessage(ChatColor.RED + "That team is full!"));
			} else if (!player.hasTeam() || !player.getTeam().equals(team)) {
				if (player.hasTeam()) {
					player.getTeam().removeTeamMember(player);
				}
				team.addTeamMember(player);
				sender.sendMessage(module.formatSystemMessage("You have joined the " + team.getFormattedName()));
			}
		} else {
			AdvertisementUtils.sendAdvertisement(sender, "Choose your Team");
		}
	}

}
