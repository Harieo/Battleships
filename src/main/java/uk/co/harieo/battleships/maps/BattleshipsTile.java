package uk.co.harieo.battleships.maps;

import org.bukkit.Location;

import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.ships.Battleship;

/**
 * This class represents one single {@link Location} on the board that as a collection made a single {@link Coordinate}.
 * Therefore, 1 {@link Coordinate} may represent multiple instances of this class but NOT visa versa.
 */
public class BattleshipsTile {

	private Location locations;
	private Team team;
	private char letter;
	private int number;

	private GamePlayer player;
	private Battleship ship;
	private boolean isHit = false;

	BattleshipsTile(Location locations, Team team) {
		this.locations = locations;
		this.team = team;
	}

	/**
	 * @return the location that this tile stores data for
	 */
	public Location getLocation() {
		return locations;
	}

	/**
	 * @return the team that owns the side of the board that this tile is located on
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * @return the letter of this tile's coordinates
	 */
	public char getLetter() {
		return letter;
	}

	/**
	 * Sets the letter value of this tile's coordinates
	 *
	 * @param letter to set the letter to
	 */
	void setLetter(String letter) {
		this.letter = letter.charAt(0);
	}

	/**
	 * @return the number coordinate of this tile
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Sets the number coordinate of this tile
	 *
	 * @param number to set the value to
	 */
	void setNumber(int number) {
		this.number = number;
	}

	/**
	 * @return the player that owns this tile by having their ship located on it
	 */
	public GamePlayer getOwningPlayer() {
		return player;
	}

	/**
	 * Sets the player that owns this tile
	 *
	 * @param playerUsing to set the value to
	 */
	public void setPlayerUsing(GamePlayer playerUsing) {
		this.player = playerUsing;
	}

	/**
	 * @return the type of ship that is on this tile, which will be null if the tile {@link #isWater()}
	 */
	public Battleship getShip() {
		return ship;
	}

	/**
	 * Sets the type of ship that is on this tile
	 *
	 * @param ship to set the value to
	 */
	public void setShip(Battleship ship) {
		this.ship = ship;
	}

	/**
	 * @return whether the {@link #getShip()} on this tile is null
	 */
	public boolean isWater() {
		return ship == null; // Player could be offline so we'll avoid checking that
	}

	/**
	 * @return whether this tile has been hit by any party in the game
	 */
	public boolean isHit() {
		return isHit;
	}

	/**
	 * Sets whether this tile has been hit by any party in the game
	 *
	 * @param hit to set the value to
	 */
	public void setIsHit(boolean hit) {
		this.isHit = hit;
	}

}
