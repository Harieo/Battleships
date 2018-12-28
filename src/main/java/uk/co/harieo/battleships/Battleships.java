package uk.co.harieo.battleships;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;

import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.games.GameStore;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.scoreboards.ConstantElement;
import uk.co.harieo.GamesCore.scoreboards.GameBoard;
import uk.co.harieo.battleships.listeners.BattleshipsChatModule;
import uk.co.harieo.battleships.listeners.ConnectionsListener;

public class Battleships extends JavaPlugin implements Game {

	private static Battleships INSTANCE;

	private static GameState STATE = GameState.LOBBY;
	private static int GAME_NUMBER;
	private GameBoard lobbyScoreboard;
	private ChatModule chatModule;

	@Override
	public void onEnable() {
		INSTANCE = this;

		setupLobbyScoreboard();
		chatModule = new BattleshipsChatModule(this);

		Bukkit.getPluginManager().registerEvents(new ConnectionsListener(), this);

		GameStore.instance().registerGame(this);
	}

	private void setupLobbyScoreboard() {
		lobbyScoreboard = new GameBoard(ChatColor.GOLD + ChatColor.BOLD.toString() + getGameName(),
				DisplaySlot.SIDEBAR);
		lobbyScoreboard.addBlankLine();
		lobbyScoreboard.addLine(new ConstantElement(ChatColor.GREEN + ChatColor.BOLD.toString() + "Team"));
		lobbyScoreboard.addLine((Player player) -> {
			GamePlayer gamePlayer = GamePlayerStore.instance(this).get(player);
			if (gamePlayer.getTeam() == null) {
				return "None";
			} else {
				return gamePlayer.getTeam().getFormattedName();
			}
		});
		lobbyScoreboard.addBlankLine();
		lobbyScoreboard.addLine(new ConstantElement(ChatColor.AQUA + ChatColor.BOLD.toString() + "Players"));
		lobbyScoreboard.addLine((Player player) -> Bukkit.getOnlinePlayers().size() + " of " + getMaximumPlayers());
		lobbyScoreboard.addBlankLine();
		lobbyScoreboard.addLine(
				new ConstantElement(ChatColor.YELLOW + ChatColor.BOLD.toString() + "patreon.com/harieo"));
	}

	public GameBoard getLobbyScoreboard() {
		return lobbyScoreboard;
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
	public ChatModule chatModule() {
		return chatModule;
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

	public static Battleships getInstance() {
		return INSTANCE;
	}

}
