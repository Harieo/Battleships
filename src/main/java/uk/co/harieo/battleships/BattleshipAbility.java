package uk.co.harieo.battleships;

import java.util.ArrayList;
import java.util.List;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.ships.Battleship;

public enum BattleshipAbility {

	// Handled in AbilityItem
	AERIAL_RECON("Aerial Reconnaissance", "Show a list of destroyed ships to the activator",
			Battleship.AIRCRAFT_CARRIER),
	// Handled in RoundTasks
	PRESSURE("Pressure under Fire", "Reduce enemy shooting time to 5 seconds for 1 turn", Battleship.DREADNOUGHT),
	// Handled in RoundTasks
	SIGNAL_JAMMER("Signal Jammer", "Disrupts enemy chat for 1 turn", Battleship.CRUISER),
	// Handled in AbilityItem
	SHORT_CIRCUIT("Short-Circuit", "Clear the enemy chat", Battleship.FRIGATE);

	private static final List<BattleshipAbility> blueActiveAbilities = new ArrayList<>();
	private static final List<BattleshipAbility> redActiveAbilities = new ArrayList<>();
	// As they only have 1 ability, no point storing the ability that was used
	private static final List<GamePlayer> usedAbilities = new ArrayList<>();

	private String name;
	private String description;
	private Battleship battleship;
	private boolean showAbility;

	BattleshipAbility(String name, String description, Battleship attachedBattleship, boolean shouldShowAbility) {
		this.name = name;
		this.description = description;
		this.battleship = attachedBattleship;
		this.showAbility = shouldShowAbility;
	}

	BattleshipAbility(String name, String description, Battleship attachedBattleship) {
		this(name, description, attachedBattleship, true);
	}

	/**
	 * @return the name of this ability
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return a description of what this ability does
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return whether activation of this ability should be shown to the enemy
	 */
	public boolean shouldShowAbilityOnUse() {
		return showAbility;
	}

	/**
	 * @return the {@link Battleship} this ability may be used with
	 */
	public Battleship getBattleship() {
		return battleship;
	}

	/**
	 * Checks whether an ability has been activated by the specified team
	 *
	 * @param ability to check whether it has been activated
	 * @param team to check if the ability is activated for them
	 * @return whether the ability is active
	 */
	public static boolean isAbilityActive(BattleshipAbility ability, Team team) {
		Battleships game = Battleships.getInstance();
		if (team.equals(game.getBlueTeam())) {
			return blueActiveAbilities.contains(ability);
		} else {
			return redActiveAbilities.contains(ability);
		}
	}

	/**
	 * Checks whether a player has used their ability
	 *
	 * @param player to check
	 * @return whether the player has used their ability
	 */
	public static boolean hasUsedAbility(GamePlayer player) {
		return usedAbilities.contains(player);
	}

	/**
	 * Adds an ability to the list of active abilities for the specified team
	 *
	 * @param ability to add
	 * @param team to add for
	 */
	public static void addActiveAbility(GamePlayer player, BattleshipAbility ability, Team team) {
		Battleships game = Battleships.getInstance();
		if (team.equals(game.getBlueTeam())) {
			blueActiveAbilities.add(ability);
		} else {
			redActiveAbilities.add(ability);
		}
		usedAbilities.add(player);
	}

	/**
	 * Resets all active abilities
	 */
	public static void resetAbilities() {
		redActiveAbilities.clear();
		blueActiveAbilities.clear();
	}

	/**
	 * Finds a {@link BattleshipAbility} that has a {@link Battleship} matching the specified parameter. Throws an
	 * {@link IllegalArgumentException} on passing a {@link Battleship} that has no matching ability, which would be
	 * unfair on a player.
	 *
	 * @param battleship to find the ability of
	 * @return the attached ability
	 */
	public static BattleshipAbility fromBattleship(Battleship battleship) {
		for (BattleshipAbility ability : values()) {
			if (ability.getBattleship().equals(battleship)) {
				return ability;
			}
		}
		throw new IllegalArgumentException(
				"A ship was passed ( " + battleship.getName() + ") that has no ability, which is illegal");
	}

}
