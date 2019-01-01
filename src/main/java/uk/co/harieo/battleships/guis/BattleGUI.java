package uk.co.harieo.battleships.guis;

import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import uk.co.harieo.FurCore.guis.GUI;
import uk.co.harieo.GamesCore.teams.Team;
import uk.co.harieo.battleships.maps.BattleshipsMap;
import uk.co.harieo.battleships.maps.Coordinate;

public class BattleGUI extends GUI {

	public BattleGUI(Team team, BattleshipsMap map) {
		super("Enemy Territories", map.getHighestY()); // Y axis will be the one going down each row
		List<Coordinate> coordinates = map.getCoordinates();
	}

	@Override
	public void onClick(InventoryClickEvent event) {

	}

}
