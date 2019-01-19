package uk.co.harieo.battleships;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;

import net.md_5.bungee.api.ChatColor;
import uk.co.harieo.FurCore.FurCore;
import uk.co.harieo.FurCore.maps.MapImpl;
import uk.co.harieo.GamesCore.chat.ChatModule;
import uk.co.harieo.GamesCore.games.Game;
import uk.co.harieo.GamesCore.games.GameState;
import uk.co.harieo.GamesCore.games.GameStore;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.players.GamePlayerStore;
import uk.co.harieo.GamesCore.scoreboards.ConstantElement;
import uk.co.harieo.GamesCore.scoreboards.GameBoard;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.GamesCore.timers.GameStartTimer;
import uk.co.harieo.battleships.commands.ForceStartCommand;
import uk.co.harieo.battleships.commands.SwitchTeamCommand;
import uk.co.harieo.battleships.listeners.ChatListener;
import uk.co.harieo.battleships.listeners.ConnectionsListener;
import uk.co.harieo.battleships.listeners.MiscListener;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.MapLoader;
import uk.co.harieo.battleships.tasks.PreGameTasks;

public class Battleships extends JavaPlugin implements Game {

	public static final String SCOREBOARD_IP =
			ChatColor.GOLD +  "play" + ChatColor.DARK_GRAY + "." + ChatColor.YELLOW
					.toString() + "harieo" + ChatColor.DARK_GRAY + "."
					+ ChatColor.GOLD + "me";

	private static Battleships INSTANCE;
	private static GameState STATE = GameState.LOBBY;
	private static int GAME_NUMBER;

	private GameBoard lobbyScoreboard;
	private ChatModule chatModule;
	private GameStartTimer gameStartTimer;
	private Team redTeam;
	private Team blueTeam;
	private BattleshipsMap map;

	@Override
	public void onEnable() {
		INSTANCE = this;

		gameStartTimer = new GameStartTimer(this, 60, 15);
		gameStartTimer.beginTimer(); // It will keep ticking but won't progress until players join
		gameStartTimer.setOnRun(timeLeft -> {
			if (timeLeft == 30 || timeLeft == 10 || timeLeft < 4) {
				gameStartTimer.pingTime();
			}
		});
		gameStartTimer.setTimerEndEvent(v -> startGame()); // The game starts here

		chatModule = new BattleshipsChatModule();
		blueTeam = new Team(this, "Blue Team", ChatColor.BLUE);
		redTeam = new Team(this, "Red Team", ChatColor.RED);

		registerListeners(new ConnectionsListener(), new MiscListener(), new ChatListener());
		FurCore.getInstance().registerCommands(new SwitchTeamCommand(), new ForceStartCommand());

		MapImpl spawnMap = FurCore.getInstance().getPrimaryWorld();
		if (spawnMap != null) {
			World world = spawnMap.getWorld();
			world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
			world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
			world.setStorm(false); // Just in-case the map was loaded from an external server
			getLogger().info("Set game rules for spawn world");

			map = MapLoader.parseMap(spawnMap); // This should be changed if maps separate in the future
			if (map.getHighestX() > 5 || map.getHighestY() > 5) {
				getLogger().severe("Map contains more than 5 of either axis, which will cause severe issues with GUIs");
			}
		}

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
		lobbyScoreboard.addLine(new ConstantElement(ChatColor.GOLD + ChatColor.BOLD.toString() + "Time to Start"));
		lobbyScoreboard.addLine((Player player) -> {
			if (Bukkit.getOnlinePlayers().size() < getMinimumPlayers()) {
				return "Needs " + getMinimumPlayers() + " Players";
			} else {
				return String.valueOf(gameStartTimer.getTimeLeft() + 1);
			}
		});
		lobbyScoreboard.addBlankLine();

		lobbyScoreboard.addLine(
				new ConstantElement(SCOREBOARD_IP));
	}

	private void registerListeners(Listener... listeners) {
		for (Listener listener : listeners) {
			Bukkit.getPluginManager().registerEvents(listener, this);
		}
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

	public BattleshipsMap getMap() {
		return map;
	}

	@Override
	public void startGame() {
		if (!gameStartTimer.isCancelled()) {
			gameStartTimer.cancel();
		}
		PreGameTasks.beginPreGame(this);
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
