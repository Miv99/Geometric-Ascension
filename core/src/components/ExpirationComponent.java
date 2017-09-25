package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 9/25/2017.
 */

public class ExpirationComponent implements Component, Pool.Poolable {
    // Time before entity gets deleted
    float time;

    @Override
    public void reset() {

    }

    public float getTime() {
        return time;
    }

    public ExpirationComponent setTime(float time) {
        this.time = time;
        return this;
    }
}
