package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.miv.Mappers;

import java.util.ArrayList;

import components.AIComponent;
import components.ExpirationComponent;

/**
 * Created by Miv on 9/25/2017.
 */

public class ExpirationSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;
    private ArrayList<Entity> removalQueue;

    public ExpirationSystem() {
        removalQueue = new ArrayList<Entity>();
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(ExpirationComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(ExpirationComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(Entity e : entities) {
            ExpirationComponent expirationComponent = Mappers.expiration.get(e);
            expirationComponent.setTime(expirationComponent.getTime() - deltaTime);
            if(expirationComponent.getTime() <= 0) {
                removalQueue.add(e);
            }
        }
        for(Entity e : removalQueue) {
            getEngine().removeEntity(e);
        }
        removalQueue.clear();
    }
}
