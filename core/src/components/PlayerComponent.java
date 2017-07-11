package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 5/23/2017.
 */
public class PlayerComponent implements Component, Pool.Poolable {
    private float pixelPoints;

    // Indicates whether or not the entity is travelling to a new map area
    private transient boolean travelling;

    @Override
    public void reset() {
        travelling = false;
        pixelPoints = 0;
    }

    public boolean isTravelling() {
        return travelling;
    }

    public void setTravelling(boolean travelling) {
        this.travelling = travelling;
    }

    public float getPixelPoints() {
        return pixelPoints;
    }

    public void setPixelPoints(float pixelPoints) {
        this.pixelPoints = pixelPoints;
    }
}
