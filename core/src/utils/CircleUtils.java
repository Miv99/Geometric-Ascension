package utils;

import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

/**
 * Created by Miv on 8/10/2017.
 */

public class CircleUtils {
    /**
     *
     * @param main Circle to be surrounded by smaller circles
     * @param circlesCount Number of circles to encompass the main circle. Must be greater than or equal to 8.
     */
    public static ArrayList<CircleHitbox> createEncompassingCircles(CircleHitbox main, int circlesCount) {
        ArrayList<CircleHitbox> circles = new ArrayList<CircleHitbox>();

        float mainRadius = main.radius;
        float subRadius = (MathUtils.PI * mainRadius)/(circlesCount - MathUtils.PI);
        float combinedRadius = mainRadius + subRadius;
        for(int i = 0; i < circlesCount; i++) {
            CircleHitbox c = new CircleHitbox();
            c.setHitboxTextureType(main.getHitboxTextureType());
            c.set(main.x + combinedRadius*MathUtils.cos(i * MathUtils.PI2/circlesCount), main.y + combinedRadius*MathUtils.sin(i * MathUtils.PI2/circlesCount), subRadius);
            circles.add(c);
        }

        return circles;
    }

    /**
     * Mirror all circles not in ignoredCircles across the line x=0
     * @param ignoredCircles Circles not to be mirrored
     * @return ArrayList of circles with negative y-position of original.
     */
    public static ArrayList<CircleHitbox> createVerticalMirror(ArrayList<CircleHitbox> ignoredCircles, ArrayList<CircleHitbox> allCircles) {
        ArrayList<CircleHitbox> circles = new ArrayList<CircleHitbox>();

        for(CircleHitbox c : allCircles) {
            if(!ignoredCircles.contains(c)) {
                CircleHitbox newC = c.clone();
                newC.y = -c.y;
                circles.add(newC);
            }
        }

        return circles;
    }
}
