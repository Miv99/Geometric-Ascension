package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * All enemy circles and bullets are larger than normal
 * Created by Miv on 9/29/2017.
 */
public class Oversized extends MapAreaModifier {
    public Oversized(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Oversized";
    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        if(ecd.isEnemy()) {
            // Every enemy's circle radius and attack pattern bullets' radii gets multiplied by a random number
            float sizeMultiplier = MathUtils.random(1.5f, 2.5f);
            for (CircleHitbox c : ecd.getCircleHitboxes()) {
                c.setOriginalPosX(c.getOriginalPosX() * sizeMultiplier);
                c.setOriginalPosY(c.getOriginalPosY() * sizeMultiplier);
                c.set(c.x * sizeMultiplier, c.y * sizeMultiplier, c.radius * sizeMultiplier);

                c.getAttackPattern().setBulletRadiusPpMultiplier(c.getAttackPattern().getBulletRadiusPpMultiplier() * sizeMultiplier);
            }
        }
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

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
