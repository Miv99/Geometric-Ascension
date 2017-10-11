package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 10/11/2017.
 */

public class Undersized extends MapAreaModifier {
    public Undersized() {

    }

    public Undersized(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Undersized";
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        // Every enemy's circle radius and attack pattern bullets' radii gets multiplied by a random number
        float sizeMultiplier = MathUtils.random(0.25f, 0.6f);
        for (CircleHitbox c : ecd.getCircleHitboxes()) {
            c.setOriginalPosX(c.getOriginalPosX() * sizeMultiplier);
            c.setOriginalPosY(c.getOriginalPosY() * sizeMultiplier);
            c.set(c.x * sizeMultiplier, c.y * sizeMultiplier, c.radius * sizeMultiplier);
        }

        ecd.multiplyPpGain(1.3f);
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
