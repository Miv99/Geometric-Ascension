package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

import utils.OnCollisionEvent;

/**
 * Created by Miv on 6/21/2017.
 */
public class CustomOnCollisionComponent implements Component, Pool.Poolable {
    private OnCollisionEvent onCollisionEvent;

    @Override
    public void reset() {

    }

    public OnCollisionEvent getOnCollisionEvent() {
        return onCollisionEvent;
    }

    public CustomOnCollisionComponent setOnCollisionEvent(OnCollisionEvent onCollisionEvent) {
        this.onCollisionEvent = onCollisionEvent;
        return this;
    }
}
