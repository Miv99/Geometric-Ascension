package ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Mappers;

import components.HitboxComponent;
import map.EntityCreationData;
import utils.Point;

/**
 * Created by Miv on 6/20/2017.
 */
public abstract class AI {
    public static enum AIType {
        NONE,
        SIMPLE_FOLLOW_TARGET,
        SIMPLE_STALK_TARGET,
        SIMPLE_WANDER;
    }

    public static class RotationBehaviorParams {
        // From 0 - 1.0; default for enemies is 0.6
        private float lerpFactor;
        private boolean faceTarget;
        // In radians/second
        private float constantRotationVelocity;

        public RotationBehaviorParams(boolean faceTarget, float lerpFactor) {
            this.faceTarget = faceTarget;
            this.lerpFactor = lerpFactor;
        }

        /**
         * @param constantRotationVelocity In degrees/second
         */
        public RotationBehaviorParams(float constantRotationVelocity) {
            this.constantRotationVelocity = constantRotationVelocity * MathUtils.degreesToRadians;
        }
    }

    public class RotationBehavior {
        private RotationBehaviorParams params;

        private HitboxComponent hitboxComponent;

        public RotationBehavior(RotationBehaviorParams params) {
            this.params = params;
        }

        public void update(float deltaTime) {
            update(deltaTime, MathUtils.atan2(targetPos.y - selfPos.y, targetPos.x - selfPos.x));
        }

        public void update(float deltaTime, float angleToTarget) {
            if(params != null) {
                float currentRotationAngle = hitboxComponent.getLastFacedAngle();

                if (params.faceTarget) {
                    // Lerp rotation to target angle
                    angleToTarget += MathUtils.PI2;
                    while(angleToTarget - currentRotationAngle > MathUtils.PI || angleToTarget - currentRotationAngle < -MathUtils.PI) {
                        if (angleToTarget - currentRotationAngle > MathUtils.PI) {
                            angleToTarget -= MathUtils.PI2;
                        } else if (angleToTarget - currentRotationAngle < -MathUtils.PI) {
                            angleToTarget += MathUtils.PI2;
                        }
                    }
                    currentRotationAngle += (angleToTarget - currentRotationAngle) * params.lerpFactor * deltaTime;
                    hitboxComponent.setLastFacedAngle(currentRotationAngle);
                    hitboxComponent.setAimingAngle(currentRotationAngle);
                } else if (params.constantRotationVelocity != 0) {
                    float angle = currentRotationAngle + params.constantRotationVelocity * deltaTime;
                    hitboxComponent.setLastFacedAngle(angle);
                    hitboxComponent.setAimingAngle(angle);
                }
            }
        }

        public void setHitboxComponent(HitboxComponent hitboxComponent) {
            this.hitboxComponent = hitboxComponent;
        }
    }

    protected AI subEntityAI;

    protected HitboxComponent selfHitbox;
    protected Point selfPos;
    protected Point targetPos;

    protected Entity self;
    protected Entity target;

    protected RotationBehavior rotationBehavior;
    protected RotationBehaviorParams rotationBehaviorParams;

    public AI(Entity self, Entity target, RotationBehaviorParams rotationBehaviorParams) {
        this.self = self;
        this.target = target;
        this.rotationBehavior = new RotationBehavior(rotationBehaviorParams);
        this.rotationBehaviorParams = rotationBehaviorParams;
        selfHitbox = Mappers.hitbox.get(self);
        selfPos = selfHitbox.getOrigin();
        targetPos = Mappers.hitbox.get(target).getOrigin();
        rotationBehavior.setHitboxComponent(selfHitbox);
    }

    public abstract void update(float deltaTime);

    public abstract AI clone(Entity newSelf);

    public abstract void saveToEntityCreationData(EntityCreationData ecd);

    public AI getSubEntityAI() {
        if(subEntityAI == null) {
            return this;
        } else {
            return subEntityAI;
        }
    }

    public Entity getTarget() {
        return target;
    }
}
