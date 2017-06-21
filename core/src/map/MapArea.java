package map;

import java.util.ArrayList;

/**
 * @see Map
 * Created by Miv on 5/23/2017.
 */
public class MapArea {
    public ArrayList<EntityCreationData> entityCreationDataArrayList;
    private float radius;

    public MapArea(float radius) {
        this.radius = radius;
        entityCreationDataArrayList = new ArrayList<EntityCreationData>();
    }

    public float getRadius() {
        return radius;
    }
}
