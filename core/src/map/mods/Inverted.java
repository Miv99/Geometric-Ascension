package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.miv.Mappers;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 10/10/2017.
 */

public class Inverted extends MapAreaModifier {
    public Inverted() {

    }

    public Inverted(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Inverted controls";
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {

    }

    @Override
    public void onEntityEnter(Entity entity) {
        if(Mappers.player.has(entity)) {
            Mappers.hitbox.get(entity).setInvertMovementAndShooting(true);
        }
    }

    @Override
    public void onPlayerLeave() {
        Mappers.hitbox.get(player).setInvertMovementAndShooting(false);
    }

    @Override
    public void update(float deltaTime) {

    }
}
