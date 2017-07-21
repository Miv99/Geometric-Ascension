package utils;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

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

    public static float normalizeAngle(float angleInRadians) {
        if(angleInRadians < 0) {
            return normalizeAngle(angleInRadians + MathUtils.PI2);
        } else if(angleInRadians > MathUtils.PI2) {
            return normalizeAngle(angleInRadians - MathUtils.PI2);
        } else {
            return angleInRadians;
        }
    }
}
