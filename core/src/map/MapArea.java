package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.miv.EntityActions;
import com.miv.Mappers;

import java.util.ArrayList;

import ai.AI;
import ai.SimpleFollowTarget;
import ai.SimpleStalkTarget;
import ai.SimpleWander;
import components.AIComponent;
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
     * when {@link MapArea#spawnEntities(PooledEngine, Map, Entity)}} is called.
     */
    private int stairsDestination = -1;

    /**
     * For Json files
     */
    public MapArea() {}

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
    public void spawnEntities(final PooledEngine engine, final Map map, Entity player) {
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
            hitbox.setMaxSpeed(ecd.getMaxSpeed());
            e.add(hitbox);

            //TODO: add to this as more AI types are added
            if(ecd.getAiType() == AI.AIType.SIMPLE_FOLLOW_TARGET) {
                e.add(engine.createComponent(AIComponent.class).setAi(new SimpleFollowTarget(e, player)));
            } else if(ecd.getAiType() == AI.AIType.SIMPLE_STALK_TARGET) {
                e.add(engine.createComponent(AIComponent.class).setAi(new SimpleStalkTarget(e, player, ecd.getSimpleStalkMinSpeedDistance(), ecd.getSimpleStalkMaxSpeedDistance(), 0)));
            } else if(ecd.getAiType() == AI.AIType.SIMPLE_WANDER) {
                e.add(engine.createComponent(AIComponent.class).setAi(new SimpleWander(e, ecd.getSimpleWanderRadius(), ecd.getSimpleWanderMinInterval(), ecd.getSimpleWanderMaxInterval(), ecd.getSimpleWanderMinAcceleration(), ecd.getSimpleWanderMaxAcceleration())));
            }

            engine.addEntity(e);
        }

        // Stairs, if any
        if(stairsDestination != -1) {
            Entity e = engine.createEntity();
            HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
            hitbox.addCircle(new CircleHitbox().setHitboxTextureType(RenderSystem.HitboxTextureType.STAIRS));
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
