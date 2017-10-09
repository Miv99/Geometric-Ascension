package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 10/9/2017.
 */

public class Explosive extends MapAreaModifier {
    public Explosive() {

    }

    public Explosive(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {
        // Explode into ring of bullets
        int bulletCount = MathUtils.random(8, 16);
        float deltaAngle = MathUtils.PI2/bulletCount;
        for(int i = 0; i < bulletCount; i++) {

        }
    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {

    }

    @Override
    public void onEntityEnter(Entity entity) {

    }

    @Override
    public void onPlayerLeave() {

    }

    @Override
    public void update(float deltaTime) {

    }
}
