package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPart;
import com.miv.AttackPattern;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 10/5/2017.
 */
public class HomingBullets extends MapAreaModifier {
    public HomingBullets() {

    }

    public HomingBullets(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Homing bullets";
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        if(ecd.isEnemy()) {
            for(CircleHitbox c : ecd.getCircleHitboxes()) {
                AttackPattern attackPattern = c.getAttackPattern();
                for(AttackPart ap : attackPattern.getAttackParts()) {
                    ap.setPlayerAttractionLerpFactor(MathUtils.random(0.12f, 0.24f));
                }
            }
        }
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
