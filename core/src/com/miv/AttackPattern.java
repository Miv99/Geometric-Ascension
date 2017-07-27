package com.miv;

/**
 * An attack pattern consists of multiple {@link AttackPart} that have a {@link AttackPart#delay} time value. After that much time has passed, the attack part is fired,
 * spawning a bullet. After {@link AttackPattern#duration} seconds pass, the fields in {@link utils.CircleHitbox} relevant to AttackPattern (time and which attack parts have been fired)
 * are reset.
 * Created by Miv on 5/24/2017.
 */
public class AttackPattern {
    /**
     * MUST BE IN ASCENDING ORDER BY {@link AttackPart#delay}
     */
    private AttackPart[] attackParts;
    // Duration of attack pattern in seconds before it repeats
    private float duration;
    // Total damage of all bullets in all attack parts
    private float totalDamage;
    private float totalRadius;

    /**
     * For Json files
     */
    public AttackPattern() {}

    public AttackPattern(int attackPartsCount) {
        attackParts = new AttackPart[attackPartsCount];
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public AttackPart[] getAttackParts() {
        return attackParts;
    }

    public void setAttackPart(int index, AttackPart attackPart) {
        attackParts[index] = attackPart;
        totalDamage += attackPart.getDamage();
        totalRadius += attackPart.getRadius();

        // Sanity check to make sure attackParts is in correct order
        for(int i = 0; i < index; i++) {
            if(attackParts[i].getDelay() > attackPart.getDelay()) {
                System.out.println("you messed up #035230523");
            }
        }
    }

    public float getTotalDamage() {
        return totalDamage;
    }

    public float getTotalRadius() {
        return totalRadius;
    }
}
