package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPart;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 10/9/2017.
 */

public class Bulky extends MapAreaModifier {
    public Bulky() {

    }

    public Bulky(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Bulky";
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
        float healthMultiplier = MathUtils.random(1.25f, 2f);
        for(CircleHitbox c : ecd.getCircleHitboxes()) {
            c.setBaseMaxHealth(c.getBaseMaxHealth() * healthMultiplier);
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
