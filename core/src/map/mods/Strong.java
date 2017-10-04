package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.AssetManager;
import com.miv.AttackPattern;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 9/28/2017.
 */
public class Strong extends MapAreaModifier {
    public Strong(AssetManager assetManager, MapArea mapArea, Entity player) {
        super(assetManager, mapArea, player);
        displayName = "Strong";
    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        if(ecd.isEnemy()) {
            for (CircleHitbox c : ecd.getCircleHitboxes()) {
                AttackPattern ap = c.getAttackPattern();
                ap.setBulletDamagePpMultiplier(ap.getBulletDamagePpMultiplier() * 2f);
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
