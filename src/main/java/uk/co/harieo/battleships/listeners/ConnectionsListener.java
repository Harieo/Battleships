package uk.co.harieo.battleships.listeners;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.battleships.Battleships;

public class ConnectionsListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Battleships core = Battleships.getInstance();

		if (core.getState() == GameState.LOBBY) {
			core.getLobbyScoreboard().render(core, event.getPlayer(), 5);
		}
	}

}
