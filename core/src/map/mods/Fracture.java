package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Mappers;

import ai.AI;
import components.AIComponent;
import components.EnemyComponent;
import components.HitboxComponent;
import components.PpOrbComponent;
import map.EntityCreationData;
import map.MapArea;
import systems.RenderSystem;
import utils.CircleHitbox;
import utils.Point;

/**
 * Enemy circles explode into 2-3 smaller, unfracturable circles upon death
 * Created by Miv on 10/1/2017.
 */
public class Fracture extends MapAreaModifier {
    public Fracture() {

    }

    public Fracture(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Fracture";
    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {
        if(!circle.isResultOfFracture()) {
            AI ai = Mappers.ai.get(enemy).getAi();
            float maxSpeed = Mappers.hitbox.get(enemy).getMaxSpeed();
            Point origin = Mappers.hitbox.get(enemy).getOrigin();

            //TODO: create an ECD and spawn the entity using that; call mapArea.onEntityDataCreation()

            int subCirclesCount = MathUtils.random(2, 3);
            for (int i = 0; i < subCirclesCount; i++) {
                // Scale orb radius to orb's percent of total pp
                float subCircleRadius = Math.max(10f, circle.radius / subCirclesCount);
                float travelDistance = MathUtils.random(subCircleRadius, Math.max(20f, circle.radius * 2f));
                travelDistance /= 24f;

                Entity e = engine.createEntity();

                e.add(engine.createComponent(EnemyComponent.class));

                HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
                hitbox.setOrigin(origin.x + circle.x, origin.y + circle.y);
                CircleHitbox c = circle.clone();
                c.setRadius(subCircleRadius);
                c.setBaseMaxHealth(circle.getMaxHealth() / (subCirclesCount * 3f));
                c.setBaseMaxHealth(c.getMaxHealth());
                c.setHealth(c.getMaxHealth());
                c.setPpGain(circle.getPpGain()/(2f * subCirclesCount));
                c.setIsResultOfFracture(true);
                hitbox.setMaxSpeed(maxSpeed);
                hitbox.setIsShooting(true);
                hitbox.addCircle(c, true);
                hitbox.calculateGravitationalRadius();
                e.add(hitbox);

                if(Mappers.ai.has(enemy)) {
                    e.add(engine.createComponent(AIComponent.class).setAi(ai.getSubEntityAI().clone(e)));
                }

                // Set velocity and deceleration for orbs such that it stops right at travelDistance
                float angle = MathUtils.random(MathUtils.PI2);
                float timeToDestination = MathUtils.random(1f, 2f);
                float initialSpeed = MathUtils.random(30, 65);
                hitbox.setVelocity(initialSpeed * MathUtils.cos(angle), initialSpeed * MathUtils.sin(angle), true);
                float acceleration = (2f * (travelDistance - (initialSpeed * timeToDestination))) / (timeToDestination * timeToDestination);

                // Ensure acceleration is always in the opposite direction of velocity
                float a = 1f;
                float b = 1f;
                if ((hitbox.getVelocity().x > 0 && acceleration * MathUtils.cos(angle) > 0) || (hitbox.getVelocity().x < 0 && acceleration * MathUtils.cos(angle) < 0)) {
                    a = -1f;
                }
                if ((hitbox.getVelocity().y > 0 && acceleration * MathUtils.sin(angle) > 0) || (hitbox.getVelocity().y < 0 && acceleration * MathUtils.sin(angle) < 0)) {
                    b = -1f;
                }
                hitbox.setAcceleration(acceleration * MathUtils.cos(angle) * a, acceleration * MathUtils.sin(angle) * b, timeToDestination / 2f);

                engine.addEntity(e);
                mapArea.addEnemy(e);
                mapArea.onEntityEnter(e);
            }
        }
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        // No pp multiplier for this mod because fractured circles already give more pp than the original
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
