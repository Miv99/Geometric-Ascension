package utils;

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
}
