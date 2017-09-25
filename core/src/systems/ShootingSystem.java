package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.miv.Mappers;

import components.HitboxComponent;
import map.Map;

/**
 * Created by Miv on 6/17/2017.
 */
public class ShootingSystem extends EntitySystem {
    private Map map;
    private PooledEngine engine;
    private Entity player;
    private ImmutableArray<Entity> entities;

    public ShootingSystem(Map map, PooledEngine engine) {
        this.map = map;
        this.engine = engine;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        for(Entity e : entities) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            if(hitbox.isShooting() && !hitbox.isTravelling()) {
                hitbox.update(engine, e, player, map.getCurrentArea().getRadius(), deltaTime);
            }
        }
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public void setMap(Map map) {
        this.map = map;
    }
}
