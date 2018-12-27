package uk.co.harieo.battleships;

import org.bukkit.plugin.java.JavaPlugin;

import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;

public class Battleships extends JavaPlugin implements Game {

	private static GameState STATE = GameState.LOBBY;
	private static int GAME_NUMBER;

	@Override
	public void onEnable() {

	}

	@Override
	public String getGameName() {
		return "Battleships";
	}

	@Override
	public GameState getState() {
		return STATE;
	}

	@Override
	public void setState(GameState state) {
		STATE = state;
	}

	@Override
	public JavaPlugin getPlugin() {
		return this;
	}

	@Override
	public int getMaximumPlayers() {
		return 10;
	}

	@Override
	public int getReservedSlots() {
		return 2;
	}

	@Override
	public int getHigherBoundPlayerAmount() {
		return 8;
	}

	@Override
	public int getMinimumPlayers() {
		return 4;
	}

	@Override
	public boolean isBeta() {
		return true;
	}

	@Override
	public void assignGameNumber(int i) {
		GAME_NUMBER = i;
	}

	public int getGameNumber() {
		return GAME_NUMBER;
	}

}
