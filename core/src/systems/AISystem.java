package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.miv.Mappers;

import components.AIComponent;

/**
 * Created by Miv on 6/22/2017.
 */
public class AISystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(AIComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(AIComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(Entity e : entities) {
            Mappers.ai.get(e).getAi().update(deltaTime);
        }
    }
}
