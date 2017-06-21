package factories;

import com.miv.AttackPart;
import com.miv.AttackPattern;

import java.util.HashMap;

/**
 * Enemy attack pattern factory.
 * Uses a Singleton factory design so that only one instance of each type of attack pattern is created at max.
 * Created by Miv on 5/24/2017.
 */
public class AttackPatternFactory {
    static HashMap<String, AttackPattern> attackPatterns = new HashMap<String, AttackPattern>();

    public static AttackPattern getAttackPattern(String name) {
        if(name.equalsIgnoreCase("SINGLE_SHOT")) {
            if(attackPatterns.containsKey(name)) return attackPatterns.get(name);
            else {
                AttackPattern ap = new AttackPattern(1);
                ap.setAttackPart(0,
                        new AttackPart()
                        .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                        .setAngleInRadians(0f)
                        .setBulletSpriteName("")
                        .setOriginX(0).setOriginY(0)
                        .setDelay(0)
                        .setSpeed(20f)
                        .setDamage(10f)
                        .scaleRadiusToDamage());
                attackPatterns.put(name, ap);
                return ap;
            }
        }
        return null;
    }
}
