package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 10/10/2017.
 */

public class ShrinkingMap extends MapAreaModifier {
    private float minMapAreaRadius;
    private float time;
    private float speed;

    public ShrinkingMap() {

    }

    public ShrinkingMap(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Shrinking map";

        minMapAreaRadius = mapArea.getRadius()/MathUtils.random(2.2f, 3.5f);
        time = MathUtils.random(30f, 60f);
        speed = (mapArea.getRadius() - minMapAreaRadius)/time;
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        ecd.multiplyPpGain(1.25f);
    }

    @Override
    public void onEntityEnter(Entity entity) {

    }

    @Override
    public void onPlayerLeave() {

    }

    @Override
    public void update(float deltaTime) {
        if(time > 0) {
            mapArea.setRadius(mapArea.getRadius() - speed*deltaTime);
            time -= deltaTime;
        }
    }
}
