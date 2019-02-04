package uk.co.harieo.battleships.achievements;

import uk.co.harieo.FurCore.achievements.Achievable;

public enum BattleshipsAchievement implements Achievable {

	WINS_50("Captain of Strategy", "Win 50 games", "wins50", 50),
	WINS_150("Greatest Pirate I've Ever Seen", "Win 150 games", "wins150", 150),
	WIN_UNDAMAGED("A Drop in the Ocean", "Win a game without being hit", "winsUndamaged"),
	WIN_FORFEIT("Famed Warlord", "Win due to the entire enemy team leaving", "winsForfeit"),
	COMPLETE_FULL_GAME("Bloodbath", "Complete a game with maximum players", "completeFull"),
	COMPLETE_GAME("Distant Traveller", "Play a game until the end", "completeFirst"),
	PLAY_WITH_ADMIN("Praise our Creator", "Play with an Admin", "playWithAdmin");

	private String name;
	private String description;
	private String id;
	private int progressMax;

	/**
	 * An enumerated extension of {@link Achievable} for all achievements that can be earned in this game
	 *
	 * @param name of the achievement, displayed to players
	 * @param description of the achievement, displayed to players
	 * @param id that will be used to identify the achievement, recognisable by a human developer
	 * @param progressMax the amount of progress required for this achievement to be completed
	 */
	BattleshipsAchievement(String name, String description, String id, int progressMax) {
		this.name = name;
		this.description = description;
		this.id = id;
		this.progressMax = progressMax;
	}

	/**
	 * An enumerated extension of {@link Achievable} for all achievements that can be earned in this game with {@link
	 * #progressMax} defaulted to 1
	 *
	 * @param name of the achievement, displayed to players
	 * @param description of the achievement, displayed to players
	 * @param id that will be used to identify the achievement, recognisable by a human developer
	 */
	BattleshipsAchievement(String name, String description, String id) {
		this(name, description, id, 1);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getGameName() {
		return "battleships";
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getProgressMax() {
		return progressMax;
	}

}
