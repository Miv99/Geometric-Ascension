package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

import utils.Point;

/**
 * Only the MapArea that is being focused on {@link map.Map#focus} has its entities in the engine.
 * Every time a new area is entered, all non-player entities are removed from the engine and stored into {@link EntityCreationData} objects.
 * This results in all non-player entities regaining maximum health after the player leaves the MapArea.
 * Every time the player enters a new point on the world map that isn't in {@link map.Map#areas}, a new MapArea
 * is generated on the spot and populated with entities depending on {@link map.Map#floor}.
 * Created by Miv on 5/23/2017.
 */
public class Map {
    private int floor;
    private Point focus;
    // Maps location on the world map to a specific MapArea
    public HashMap<Point, MapArea> areas;

    public void enterNewFloor(int floor) {
        this.floor = floor;
        setFocus(0, 0);
        areas.clear();
    }

    public Map() {
        areas = new HashMap<Point, MapArea>();
        focus = new Point(0, 0);
    }

    public void setFocus(int x, int y) {
        focus.x = x;
        focus.y = y;
    }

    public Point getFocus() {
        return focus;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }
}
