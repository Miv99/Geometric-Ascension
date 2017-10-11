package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPart;
import com.miv.AttackPattern;
import com.miv.Mappers;
import com.miv.Options;

import components.ExpirationComponent;
import components.HitboxComponent;
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
    private AttackPattern attackPattern;

    public Bubbly() {

    }

    public Bubbly(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Bubbly";
        time = 2.5f;

        // Create new EntityCreationData that contains a single circle hitbox that wraps only an attack pattern so that
        // other map area modifiers can modify the attack pattern accordingly
        if(attackPattern == null) {
            EntityCreationData ecd = new EntityCreationData(false);
            CircleHitbox circleWrapper = new CircleHitbox();
            circleWrapper.setHitboxTextureType(RenderSystem.HitboxTextureType.ENEMY_BULLET);
            ecd.getCircleHitboxes().add(circleWrapper);
            attackPattern = new AttackPattern();
            circleWrapper.setAttackPattern(attackPattern);
            attackPattern.setDuration(10f, true);
            attackPattern.addRandomAttackPatternStatModifiers(mapArea.getMaxPixelPoints() / mapArea.getOriginalEnemyCount());
            attackPattern.addAttackPart(new AttackPart()
                    .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.NONE)
                    .setOriginX(0).setOriginY(0)
                    .setDelay(0)
                    .setDamage(1.5f));
            mapArea.onEnemyDataCreation(ecd);
        }
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        ecd.multiplyPpGain(1.4f);
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
            for(AttackPart ap : attackPattern.getAttackParts()) {
                float radius = MathUtils.random(50f, 75f);
                float speed = MathUtils.random(3f, 5f);

                float angle = MathUtils.random(MathUtils.PI2);
                float distance = mapArea.getRadius() + 600f;

                // Determine angle of travel
                float a = MathUtils.atan2(distance, mapArea.getRadius()) * 0.8f;
                float travelAngle = MathUtils.random(-a , a) + angle + MathUtils.PI;

                ap.setSpeed(speed);
                ap.setRadius(radius);
                ap.setAngleInRadians(travelAngle);
                ap.fire(engine, null, null, distance * MathUtils.cos(angle), distance * MathUtils.sin(angle), travelAngle, mapArea.getRadius());
            }
            time = MathUtils.random(2.5f, 3.75f);
        }
        time -= deltaTime;
    }
}
