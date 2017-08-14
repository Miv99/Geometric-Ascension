package factories;

import com.badlogic.gdx.math.MathUtils;
import com.miv.AttackPart;
import com.miv.AttackPattern;

import java.util.HashMap;

/**
 * Created by Miv on 5/24/2017.
 */
public class AttackPatternFactory {
    public static AttackPattern getAttackPattern(String name) {
        if(name.equalsIgnoreCase("PLAYER_STARTING")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(0.25f);
            ap.addAttackPart(new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_RELATIVE_TO_PARENT_ROTATION)
                            .setAngleInDegrees(0f)
                            .setOriginX(0).setOriginY(0)
                            .setDelay(0)
                            .setSpeed(20f)
                            .setDamage(10f)
                            .setRadius(15f));
            return ap;
        }
        // Shoots a single bullet at the player
        else if(name.equalsIgnoreCase("SINGLE")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(4f);
            ap.addAttackPart(new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                            .setAngleInDegrees(0f)
                            .setOriginX(0).setOriginY(0)
                            .setDelay(0)
                            .setSpeed(4f)
                            .setDamage(10f)
                            .setRadius(15f));
            return ap;
        }
        // Shoots a single bullet near the player
        else if(name.equalsIgnoreCase("RANDOM_SINGLE")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(2.5f);
            ap.addAttackPart(new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                            .setRandomAngleInDegrees(-30f, 30f)
                            .setOriginX(0).setOriginY(0)
                            .setDelay(0)
                            .setSpeed(5f)
                            .setDamage(10f)
                            .setRadius(15f));
            return ap;
        }
        // Shoots one bullet at the player and another near the player at the same time
        else if(name.equalsIgnoreCase("DOUBLE")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(2.4f);
            ap.addAttackPart(new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                            .setAngleInDegrees(0f)
                            .setOriginX(0).setOriginY(0)
                            .setDelay(0)
                            .setSpeed(4f)
                            .setDamage(10f)
                            .setRadius(15f));
            ap.addAttackPart(new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                            .setRandomAngleInDegrees(-20f, 20f)
                            .setDelay(0)
                            .setSpeed(4f)
                            .setDamage(10f)
                            .setRadius(15f));
            return ap;
        }
        // Shoots two bullets on either side of the player
        else if(name.equalsIgnoreCase("PINCERS")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(2f);
            ap.addAttackPart(new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                            .setRandomAngleInDegrees(5f, 20f)
                            .setOriginX(0).setOriginY(0)
                            .setDelay(0)
                            .setSpeed(5f)
                            .setDamage(10f)
                            .setRadius(15f));
            ap.addAttackPart(new AttackPart()
                            .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                            .setRandomAngleInDegrees(-20f, -5f)
                            .setOriginX(0).setOriginY(0)
                            .setDelay(0)
                            .setSpeed(5f)
                            .setDamage(10f)
                            .setRadius(15f));
            return ap;
        }
        else if(name.equalsIgnoreCase("BOSS_1_1")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(1f);
            ap.addAttackPart(new AttackPart()
                    .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.NONE)
                    .setRandomAngleInDegrees(0f, 360f)
                    .setOriginX(0).setOriginY(0)
                    .setDelay(0)
                    .setSpeed(1.5f)
                    .setDamage(1f)
                    .setRadius(60f));
            return ap;
        }
        else if(name.equalsIgnoreCase("BOSS_1_2")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(4f);
            ap.addAttackPart(new AttackPart()
                    .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.AIM_AT_PLAYER)
                    .setAngleInDegrees(0f)
                    .setOriginX(0).setOriginY(0)
                    .setDelay(0)
                    .setSpeed(4.5f)
                    .setDamage(5f)
                    .setRadius(17f));
            return ap;
        }
        // Shoots a single bullet near the player
        else if(name.equalsIgnoreCase("BOSS_2_1")) {
            AttackPattern ap = new AttackPattern();
            ap.setDuration(5f);
            ap.addAttackPart(new AttackPart()
                    .setAttackPartAngleDeterminant(AttackPart.AttackPartAngleDeterminant.NONE)
                    .setRandomAngleInDegrees(0f, 360f)
                    .setOriginX(0).setOriginY(0)
                    .setDelay(0)
                    .setSpeed(3f)
                    .setDamage(5f)
                    .setRadius(20f));
            return ap;
        }
        return null;
    }

    public static AttackPattern getAttackPatternById(int id) {
        switch (id) {
            case 0:
                return getAttackPattern("PLAYER_STARTING");
            case 1:
                return getAttackPattern("SINGLE");
            default:
                return getAttackPattern("SINGLE");
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
