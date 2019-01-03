package uk.co.harieo.battleships.ships;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.Battleships;

public class ShipStore {

	private static Map<Team, ShipStore> CACHE = new HashMap<>();

	private Team team;
	private boolean isDestroyed = false;
	private Map<GamePlayer, Battleship> ships = new HashMap<>();

	private ShipStore(Team team) {
		this.team = team;
	}

	public Team getTeam() {
		return team;
	}

	public Battleship getShip(GamePlayer player) {
		return ships.get(player);
	}

	public Map<GamePlayer, Battleship> getShips() {
		return ships;
	}

	public boolean isDestroyed() {
		return isDestroyed;
	}

	public void setDestroyed(boolean isDestroyed) {
		this.isDestroyed = isDestroyed;
	}

	public void assignShips() {
		ships.clear();

		int i = 0;
		int quantity = 1;
		for (GamePlayer gamePlayer : team.getTeamMembers()) {
			if (i >= Battleship.values().length) {
				i = 0; // Start at the beginning
				Battleships.getInstance().getLogger().warning("Ran out of ships for players, starting from beginning");
			}

			Battleship battleship = Battleship.values()[i];
			ships.put(gamePlayer, battleship);

			Player player = gamePlayer.toBukkit();
			if (player.isOnline()) {
				ChatModule module = Battleships.getInstance().chatModule();
				player.sendMessage(module.formatSystemMessage(
						"You are now commander of the team's " + ChatColor.GOLD + ChatColor.BOLD.toString() + battleship
								.getName()));
				player.sendMessage(module.formatSystemMessage(
						"Your ship is " + ChatColor.GREEN + battleship.getSize() + " spaces " + ChatColor.WHITE
								+ "long"));
			}

			if (quantity < battleship.getMaxPerGame()) {
				quantity++; // Need more of this per game
			} else {
				i++; // Onto the next ship type
			}
		}
	}

	public static ShipStore get(Team team) {
		if (CACHE.containsKey(team)) {
			return CACHE.get(team);
		} else {
			ShipStore store = new ShipStore(team);
			CACHE.put(team, store);
			return store;
		}
	}

}
