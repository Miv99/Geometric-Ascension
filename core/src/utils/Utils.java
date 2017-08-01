package utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Mappers;

import java.util.ArrayList;

import ai.AI;
import ai.SimpleFollowTarget;
import components.AIComponent;
import components.EnemyComponent;
import components.HitboxComponent;

/**
 * Created by Miv on 6/26/2017.
 */
public class Utils {
    /**
     * Checks if c overlaps with any circle in circles
     */
    public static boolean overlaps(CircleHitbox c, ArrayList<CircleHitbox> circles) {
        for(CircleHitbox a : circles) {
            if(c.overlaps(a)) {
                return true;
            }
        }
        return false;
    }

    public static float getDistance(Point p1, Point p2) {
        return (float)Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y));
    }

    public static float getDistance(Point p1, int x2, int y2) {
        return (float)Math.sqrt((p1.x - x2)*(p1.x - x2) + (p1.y - y2)*(p1.y - y2));
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
    }

    public static float normalizeAngle(float angleInRadians) {
        if(angleInRadians < 0) {
            return normalizeAngle(angleInRadians + MathUtils.PI2);
        } else if(angleInRadians > MathUtils.PI2) {
            return normalizeAngle(angleInRadians - MathUtils.PI2);
        } else {
            return angleInRadians;
        }
    }

    public static Entity cloneEnemy(PooledEngine engine, Entity original, ArrayList<CircleHitbox> circles) {
        Entity e = engine.createEntity();

        HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
        HitboxComponent originalHitbox = Mappers.hitbox.get(original);
        hitbox.setMaxSpeed(originalHitbox.getMaxSpeed());
        hitbox.setOrigin(originalHitbox.getOrigin().x, originalHitbox.getOrigin().y);
        hitbox.setIsShooting(true);
        hitbox.setLastFacedAngle(originalHitbox.getLastFacedAngle());
        hitbox.setVelocity(originalHitbox.getVelocity().x, originalHitbox.getVelocity().y);
        hitbox.setAcceleration(originalHitbox.getAcceleration().x, originalHitbox.getAcceleration().y);
        for(CircleHitbox c : circles) {
            hitbox.addCircle(c);
        }
        //TODO: check if it's recenterCirlces() instead or even both
        hitbox.recenterCircles();
        hitbox.recenterOriginalCirclePositions();
        e.add(hitbox);

        e.add(engine.createComponent(AIComponent.class).setAi(Mappers.ai.get(original).getAi().clone(e)));
        e.add(engine.createComponent(EnemyComponent.class));

        return e;
    }
}
