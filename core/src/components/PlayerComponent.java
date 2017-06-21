package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 5/23/2017.
 */
public class PlayerComponent implements Component, Pool.Poolable {
    // Indicates whether or not the entity is travelling to a new map area
    private boolean travelling;

    @Override
    public void reset() {

    }

    public boolean isTravelling() {
        return travelling;
    }

    public void setTravelling(boolean travelling) {
        this.travelling = travelling;
    }
}
