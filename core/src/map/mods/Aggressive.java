package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;

import ai.AI;
import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * All enemies have SIMPLE_FOLLOW_TARGET AI
 * Created by Miv on 9/29/2017.
 */
public class Aggressive extends MapAreaModifier {
    public Aggressive(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Aggressive";
    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        ecd.setAiType(AI.AIType.SIMPLE_FOLLOW_TARGET);
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
