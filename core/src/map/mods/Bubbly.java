package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import map.Map;
import map.MapArea;
import systems.RenderSystem;
import utils.CircleHitbox;

/**
 * Created by Miv on 10/10/2017.
 */

public class Bubbly extends MapAreaModifier {
    private float time;

    public Bubbly() {

    }

    public Bubbly(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Bubbly";
        time = MathUtils.random(3f, 7f);
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        ecd.multiplyPpGain(1.15f);
    }

    @Override
    public void onEntityEnter(Entity entity) {

    }

    @Override
    public void onPlayerLeave() {

    }

    @Override
    public void update(float deltaTime) {
        if(time < 0) {
            float radius = MathUtils.random(Map.MIN_OBSTACLE_RADIUS, Map.MAX_OBSTACLE_RADIUS);

            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(false);
            ecd.setObstacle(true);

            CircleHitbox c = new CircleHitbox();
            // Set health to be > 0 to prevent death instantly
            float hp = mapArea.getMaxPixelPoints() * Map.OBSTACLE_HEALTH_PP_SCALE * MathUtils.random(Map.MIN_OBSTACLE_HEALTH_MULTIPLIER, Map.MAX_OBSTACLE_HEALTH_MULTIPLIER);
            c.setBaseMaxHealth(hp);
            c.setHealth(hp);
            c.setRadius(radius);
            c.setHitboxTextureType(RenderSystem.HitboxTextureType.OBSTACLE);
            ecd.getCircleHitboxes().add(c);

            float angle = MathUtils.random(MathUtils.PI2);
            float distance = MathUtils.random(radius, mapArea.getRadius() - radius);
            ecd.setSpawnPosition(distance * MathUtils.cos(angle), distance * MathUtils.sin(angle));

            mapArea.entityCreationDataArrayList.add(ecd);
            mapArea.onEnemyDataCreation(ecd);

            time = MathUtils.random(3f, 7f);
        }
    }
}
