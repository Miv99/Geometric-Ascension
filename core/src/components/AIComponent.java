package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;

import ai.AI;

/**
 * Created by Miv on 6/20/2017.
 */
public class AIComponent implements Component, Pool.Poolable {
    private AI ai;

    @Override
    public void reset() {
        ai = null;
    }

    public AI getAi() {
        return ai;
    }

    /**
     * AI must be set after adding HitboxComponent to the entity
     */
    public AIComponent setAi(AI ai) {
        this.ai = ai;
        return this;
    }
}
