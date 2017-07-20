package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by Miv on 5/23/2017.
 */
public class PlayerComponent implements Component, Pool.Poolable {
    private float pixelPoints;

    @Override
    public void reset() {
        pixelPoints = 0;
    }

    public float getPixelPoints() {
        return pixelPoints;
    }

    public void setPixelPoints(float pixelPoints) {
        this.pixelPoints = pixelPoints;
    }
}
