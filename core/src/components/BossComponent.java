package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * All entities with BossComponent also should have EnemyComponent.
 * Created by Miv on 5/23/2017.
 */
public class BossComponent implements Component, Pool.Poolable {
    @Override
    public void reset() {

    }
}
