package uk.co.harieo.battleships.animation;

import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;
import uk.co.harieo.GamesCore.players.GamePlayer;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.BattleshipAbility;
import uk.co.harieo.battleships.Battleships;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.BattleshipsTile;
import uk.co.harieo.battleships.maps.Coordinate;

public class ShootingAnimation {

	// This shows the height left on the particle animation for each tile
	private Map<BattleshipsTile, Integer> stageOneTiles = new HashMap<>(); // Values are removed in stage 1 so are not valid in stage 2
	private List<BattleshipsTile> stageTwoTiles = new ArrayList<>();

	private Battleships game;
	private Coordinate coordinate;
	private Consumer<Void> onEnd;

	private boolean stageOneOver = false; // Prevents duplicate method calls

	public ShootingAnimation(Battleships game, Coordinate coordinate) {
		this.game = game;
		this.coordinate = coordinate;
		for (BattleshipsTile tile : game.getMap().getTiles(coordinate)) {
			stageOneTiles.put(tile, 7); // This puts all starting at 7 blocks high
			stageTwoTiles.add(tile);
		}

		stageOne(); // Begin the animation
	}

	/**
	 * This stage shows the "bomb dropping" effect
	 */
	private void stageOne() {
		stageOneTiles.forEach((tile, height) -> {
			BukkitRunnable runnable = new BukkitRunnable() {
				@Override
				public void run() {
					if (stageOneTiles.get(tile) == null) {
						cancel();
						if (stageOneTiles.isEmpty()
								&& !stageOneOver) { // Once there are no more tiles to handle in stage one
							stageTwo(); // Progress to stage two
							stageOneOver = true; // Make sure no other runnables do this after this one
						}
						return; // Progressing will now cause an NPE
					}

					int height = stageOneTiles.get(tile);

					if (height < 1) {
						stageOneTiles.remove(tile); // This will end the stage one cycle on next run
						return;
					}

					// Spawn the particle
					Location spawningLocation = tile.getLocation().clone().add(0, height * 2, 0);
					World world = spawningLocation.getWorld();
					world.spawnParticle(Particle.FLAME, spawningLocation, 3);
					// Play a dramatic sound
					world.playSound(spawningLocation, Sound.BLOCK_LAVA_EXTINGUISH, 0.5F, 1);

					stageOneTiles.replace(tile, height - 1);
				}
			};
			runnable.runTaskTimer(game, 0, 20);
		});
	}

	/**
	 * This stage shows the explosion of the coordinate and sets the block accordingly. This is the final stage.
	 */
	private void stageTwo() {
		stageTwoTiles.forEach(tile -> {
			Location location = tile.getLocation();
			World world = location.getWorld();
			BattleshipsMap map = game.getMap();

			if (BattleshipAbility.isAbilityActive(BattleshipAbility.SIGNAL_JAMMER, coordinate.getTeam())) {
				location.getBlock().setType(Material.PURPLE_STAINED_GLASS);
			} else if (map.getShip(coordinate) != null) { // We're about to hit a ship, simulate explosion
				world.createExplosion(location.getX(), location.getY() + 1, location.getZ(), 3F, false, false);
				location.getBlock().setType(Material.BLACK_STAINED_GLASS);
			} else {
				world.spawnParticle(Particle.WATER_SPLASH, tile.getLocation().clone().add(0.25, 1, 0.75), 100, 2, 5, 2);
				world.playSound(location, Sound.ENTITY_BOAT_PADDLE_WATER, 1, 1);
				location.getBlock().setType(Material.YELLOW_STAINED_GLASS);
			}
		});

		if (onEnd != null) {
			onEnd.accept(null);
		}
	}

	public void setOnEnd(Consumer<Void> onEnd) {
		this.onEnd = onEnd;
	}

}
