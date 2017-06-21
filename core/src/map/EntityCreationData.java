package map;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Circle;

import java.util.ArrayList;

import components.EnemyComponent;
import components.PlayerComponent;

/**
 * Class used by {@link map.MapArea} that contains the data to create an entity upon entering the MapArea.
 * Cannot be used to create player entities.
 * Created by Miv on 5/23/2017.
 */
public class EntityCreationData {
    private int maxHealth;
    private boolean isEnemy;
    private ArrayList<Circle> circleHitboxes;

    public EntityCreationData() {
        circleHitboxes = new ArrayList<Circle>();
    }

    public Entity createEntity(PooledEngine engine) {
        Entity e = engine.createEntity();

        if(isEnemy) {
            e.add(engine.createComponent(EnemyComponent.class));
        }

        return e;
    }
}
