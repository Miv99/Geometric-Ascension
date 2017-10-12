package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPart;
import com.miv.AttackPattern;
import com.miv.Mappers;

import components.HitboxComponent;
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
        displayName = "Explosive";
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {
        // Explode into ring of bullets
        int bulletCount = MathUtils.random(8, 16);

        // Create new EntityCreationData that contains a single circle hitbox that wraps only an attack pattern so that
        // other map area modifiers can modify the attack pattern accordingly
        EntityCreationData ecd = new EntityCreationData(false);
        CircleHitbox circleWrapper = new CircleHitbox();
        circleWrapper.setHitboxTextureType(circle.getHitboxTextureType());
        ecd.getCircleHitboxes().add(circleWrapper);
        AttackPattern ap = new AttackPattern();
        circleWrapper.setAttackPattern(ap);
        ap.setDuration(10f, true);
        ap.addRandomAttackPatternStatModifiers(circle.getBasePpGain());
        float deltaAngle = MathUtils.PI2/bulletCount;
        float radius = MathUtils.random(10f, 30f);
        float speed = MathUtils.random(7.5f, 12f);
        for(int i = 0; i < bulletCount; i++) {
            ap.addAttackPart(new AttackPart()
                    .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.NONE)
                    .setAngleInRadians(deltaAngle/2f + deltaAngle*i)
                    .setOriginX(0).setOriginY(0)
                    .setDelay(0)
                    .setSpeed(speed)
                    .setDamage(3f)
                    .setRadius(radius));
        }
        mapArea.onEnemyDataCreation(ecd);

        HitboxComponent hitbox = Mappers.hitbox.get(enemy);
        for(AttackPart a : ap.getAttackParts()) {
            a.fire(engine, enemy, null, hitbox.getOrigin().x + circle.x, hitbox.getOrigin().y + circle.y, hitbox.getAimingAngle(), mapArea.getRadius());
        }
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

    }
}
