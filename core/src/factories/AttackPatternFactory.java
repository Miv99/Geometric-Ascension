package factories;

import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPart;
import com.miv.AttackPattern;

import java.util.HashMap;

/**
 * Created by Miv on 5/24/2017.
 */
public class AttackPatternFactory {
    static HashMap<String, AttackPattern> attackPatterns = new HashMap<String, AttackPattern>();

    public static AttackPattern getAttackPattern(String name) {
        if(name.equalsIgnoreCase("PLAYER_STARTING")) {
            AttackPattern ap = new AttackPattern(1);
            ap.setDuration(0.25f);
            ap.setAttackPart(0,
                    new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_RELATIVE_TO_PARENT_ROTATION)
                            .setAngleInRadians(0f)
                            .setOriginX(0).setOriginY(0)
                            .setDelay(0)
                            .setSpeed(20f)
                            .setDamage(10f)
                            .setRadius(15f));
            attackPatterns.put(name, ap);
            return ap;
        } else if(name.equalsIgnoreCase("SINGLE_SHOT")) {
            AttackPattern ap = new AttackPattern(1);
            ap.setDuration(7f);
            ap.setAttackPart(0,
                    new AttackPart()
                    .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                    .setAngleInRadians(0f)
                    .setOriginX(0).setOriginY(0)
                    .setDelay(0)
                    .setSpeed(20f)
                    .setDamage(10f)
                    .setRadius(15f));
            attackPatterns.put(name, ap);
            return ap;
        }
        return null;
    }

    public static AttackPattern getAttackPatternById(int id) {
        switch (id) {
            case 0:
                return getAttackPattern("PLAYER_STARTING");
            case 1:
                return getAttackPattern("SINGLE_SHOT");
            default:
                return getAttackPattern("SINGLE_SHOT");
        }
    }

    public static AttackPattern getRandomAttackPatternByFloor(int floor) {
        switch(floor) {
            case 0:
                return getAttackPatternById(MathUtils.random(1, 1));
            default:
                return getAttackPatternById(1);
        }
    }
}
