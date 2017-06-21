package utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Circle;
import com.miv.AttackPart;
import com.miv.AttackPattern;

/**
 * Created by Miv on 6/5/2017.
 */
public class CircleHitbox extends Circle {
    private Color color;

    private AttackPattern attackPattern;
    // Time in seconds since the last iteration of the attack pattern started
    private transient float time;
    /**
     * True if the attack part has been fired off.
     * Is reset (all fields set to false) by {@link components.HitboxComponent#update(PooledEngine, Entity, Entity, float)}
     * after each iteration of the attack pattern
     */
    private boolean[] fired;

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public AttackPattern getAttackPattern() {
        return attackPattern;
    }

    public void setAttackPattern(AttackPattern attackPattern) {
        this.attackPattern = attackPattern;
        fired = new boolean[attackPattern.getAttackParts().length];
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public boolean[] getFired() {
        return fired;
    }

    public void setFired(boolean[] fired) {
        this.fired = fired;
    }
}