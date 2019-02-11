package uk.co.harieo.battleships.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import uk.co.harieo.FurCore.items.InteractiveItem;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;

public class TeamSelectItem extends InteractiveItem {

	private Battleships game;
	private Team team;

	/**
	 * An extension of {@link InteractiveItem} that performs the '/team' command to switch teams for the player, rather
	 * than the player having to type the command out themselves
	 *
	 * @param game that is being run
	 * @param team which team this item can be used to switch to
	 */
	public TeamSelectItem(Battleships game, Team team) {
		super(Material.BANNER);
		getItem().setDurability(team.equals(game.getBlueTeam()) ? (byte) 4 : (byte) 1);
		this.game = game;
		this.team = team;
		setName(ChatColor.WHITE + "Join the " + team.getFormattedName() + InteractiveItem.RIGHT_CLICK_SUFFIX);
	}

	@Override
	public void onRightClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (team.equals(game.getBlueTeam())) {
			player.chat("/team blue");
		} else {
			player.chat("/team red");
		}
	}

}
