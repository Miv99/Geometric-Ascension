package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Entities with this component will not be spawned again if the player leaves and reenters the MapArea the entity is in
 * Created by Miv on 6/22/2017.
 */
public class IgnoreRespawnOnAreaResetComponent implements Component, Pool.Poolable {
    @Override
    public void reset() {

    }
}
