package uk.co.harieo.battleships;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.FurCore.scoreboards.ConstantElement;
import uk.co.harieo.FurCore.scoreboards.GameBoard;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.games.GameStore;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.timers.GameStartTimer;
import uk.co.harieo.battleships.listeners.BattleshipsChatModule;
import uk.co.harieo.battleships.listeners.ConnectionsListener;

public class Battleships extends JavaPlugin implements Game {

	private static Battleships INSTANCE;
	private static GameState STATE = GameState.LOBBY;
	private static int GAME_NUMBER;

	private GameBoard lobbyScoreboard;
	private ChatModule chatModule;
	private GameStartTimer gameStartTimer;
	private Team redTeam;
	private Team blueTeam;

	@Override
	public void onEnable() {
		INSTANCE = this;

		gameStartTimer = new GameStartTimer(this, 60, 15);
		gameStartTimer.beginTimer(); // It will keep ticking but won't progress until players join
		chatModule = new BattleshipsChatModule(this);
		blueTeam = new Team(this, "Blue Team", ChatColor.BLUE);
		redTeam = new Team(this, "Red Team", ChatColor.RED);

		Bukkit.getPluginManager().registerEvents(new ConnectionsListener(), this);

		setupLobbyScoreboard();
		GameStore.instance().registerGame(this);
	}

	private void setupLobbyScoreboard() {
		lobbyScoreboard = new GameBoard(ChatColor.GOLD + ChatColor.BOLD.toString() + getGameName(),
				DisplaySlot.SIDEBAR);
		lobbyScoreboard.addBlankLine();

		// Team section //
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

		// Player Count section //
		lobbyScoreboard.addLine(new ConstantElement(ChatColor.AQUA + ChatColor.BOLD.toString() + "Players"));
		lobbyScoreboard.addLine((Player player) -> Bukkit.getOnlinePlayers().size() + " of " + getMaximumPlayers());
		lobbyScoreboard.addBlankLine();

		// Time Left section //
		lobbyScoreboard.addLine(new ConstantElement(ChatColor.YELLOW + ChatColor.BOLD.toString() + "Time to Start"));
		lobbyScoreboard.addLine((Player player) -> {
			if (Bukkit.getOnlinePlayers().size() < getMinimumPlayers()) {
				return "Needs " + getMinimumPlayers() + " Players";
			} else {
				return String.valueOf(gameStartTimer.getTimeLeft());
			}
		});
		lobbyScoreboard.addBlankLine();

		lobbyScoreboard.addLine(
				new ConstantElement(ChatColor.YELLOW + ChatColor.BOLD.toString() + "patreon.com/harieo"));
	}

	public GameBoard getLobbyScoreboard() {
		return lobbyScoreboard;
	}

	public Team getRedTeam() {
		return redTeam;
	}

	public Team getBlueTeam() {
		return blueTeam;
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
