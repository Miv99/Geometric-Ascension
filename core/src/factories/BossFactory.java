package factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;

import ai.AI;
import components.HitboxComponent;
import components.PlayerComponent;
import map.EntityCreationData;
import map.Map;
import systems.RenderSystem;
import utils.CircleHitbox;

import static map.MapArea.BOSS_MAP_AREA_SIZE;

/**
 * Created by Miv on 8/2/2017.
 */

public class BossFactory {
    public static ArrayList<EntityCreationData> getBoss(int floor, float mapAreaRadius, float pp) {
        ArrayList<EntityCreationData> ecds = new ArrayList<EntityCreationData>();



        // Make boss 10x harder than a normal enemy
        pp *= 10f;

        if(floor == 0) {
            EntityCreationData e1 = new EntityCreationData(true);
            ecds.add(e1);

            float mainRadius = 450f;
            float subRadius = 50f;

            ArrayList<CircleHitbox> ca1 = new ArrayList<CircleHitbox>();
            CircleHitbox c1 = new CircleHitbox();
            ca1.add(c1);
            c1.setPosition(0, 0);
            c1.setAttackPattern(AttackPatternFactory.getAttackPattern("BOSS_1_1"));
            c1.setRadius(mainRadius);
            for(int i = 0; i < 16; i++) {
                CircleHitbox c = new CircleHitbox();
                ca1.add(c);
                float angle = i * MathUtils.degreesToRadians * (360/16f);
                c.setPosition((mainRadius + subRadius) * MathUtils.cos(angle), (mainRadius + subRadius) * MathUtils.sin(angle));
                c.setAttackPattern(AttackPatternFactory.getAttackPattern("BOSS_1_2"));
                c.setRadius(subRadius);
            }

            e1.setSpawnPosition(0, 0);
            Map.randomizeSimpleWanderAI(e1, mapAreaRadius);
            e1.setMaxSpeed(2f);
            e1.setMaxHealth(pp * 12.5f);
            e1.setCircleHitboxes(ca1);
        }

        return ecds;
    }
}
