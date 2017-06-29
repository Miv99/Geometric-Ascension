package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.miv.EntityActions;
import com.miv.Mappers;

import java.util.ArrayList;

import components.BossComponent;
import components.CustomOnCollisionComponent;
import components.EnemyComponent;
import components.HitboxComponent;
import systems.RenderSystem;
import utils.CircleHitbox;
import utils.OnCollisionEvent;

/**
 * @see Map
 * Created by Miv on 5/23/2017.
 */
public class MapArea {
    public static final float MAP_AREA_MIN_SIZE = 800f;
    public static final float MAP_AREA_MAX_SIZE = 1600f;

    public ArrayList<EntityCreationData> entityCreationDataArrayList;
    private float radius;

    /**
     * Set to -1 if no stairs exist in this MapArea. Otherwise, an entity with an OnCollision event will be spawned in the middle of the MapArea
     * when {@link MapArea#spawnEntities(PooledEngine, Map)} is called.
     */
    private int stairsDestination = -1;

    public MapArea(float radius) {
        this.radius = radius;
        entityCreationDataArrayList = new ArrayList<EntityCreationData>();
    }

    public void addStairs(int destinationFloor) {
        stairsDestination = destinationFloor;
    }

    /**
     * Spawns all entities in {@link map.MapArea#entityCreationDataArrayList}
     * and stairs.
     */
    public void spawnEntities(final PooledEngine engine, final Map map) {
        // Entities from entityCreationDataArrayList
        for(EntityCreationData ecd : entityCreationDataArrayList) {
            Entity e = engine.createEntity();
            if(ecd.isEnemy()) {
                e.add(engine.createComponent(EnemyComponent.class));
            }
            if(ecd.isBoss()) {
                e.add(engine.createComponent(BossComponent.class));
            }
            HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
            for(CircleHitbox c : ecd.getCircleHitboxes()) {
                hitbox.addCircle(c);
            }
            hitbox.setOrigin(ecd.getSpawnX(), ecd.getSpawnY());
            e.add(hitbox);

            engine.addEntity(e);
        }

        // Stairs, if any
        if(stairsDestination != -1) {
            Entity e = engine.createEntity();
            HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
            hitbox.addCircle(new CircleHitbox().setColor(RenderSystem.STAIRS_COLOR));
            e.add(hitbox);

            // Add enemy component so it can collide with the player
            e.add(engine.createComponent(EnemyComponent.class));

            // OnCollision event that moves player to new area
            OnCollisionEvent onCollisionEvent = new OnCollisionEvent() {
                @Override
                public void onCollision(Entity self, Entity other) {
                    if(Mappers.player.has(other)) {
                        EntityActions.playerEnterNewFloor(engine, other, map, stairsDestination);
                    }
                }
            };
            e.add(engine.createComponent(CustomOnCollisionComponent.class).setOnCollisionEvent(onCollisionEvent));
            engine.addEntity(e);
        }
    }

    public float getRadius() {
        return radius;
    }
}
