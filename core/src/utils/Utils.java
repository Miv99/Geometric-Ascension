package utils;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

import javafx.scene.shape.Circle;

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
}
