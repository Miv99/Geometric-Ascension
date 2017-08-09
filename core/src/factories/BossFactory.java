package factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Options;

import java.util.ArrayList;

import ai.AI;
import ai.SimpleStalkTarget;
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
    public static ArrayList<EntityCreationData> getBossById(int id, float mapAreaRadius, float pp) {
        ArrayList<EntityCreationData> ecds = new ArrayList<EntityCreationData>();

        // Make boss 10x harder than a normal enemy
        float scaledPP = pp * 10f;

        if(id == 0) {
            EntityCreationData e1 = new EntityCreationData(true);
            ecds.add(e1);

            float mainRadius = 450f;
            float subRadius = 50f;

            ArrayList<CircleHitbox> ca1 = new ArrayList<CircleHitbox>();
            CircleHitbox c1 = new CircleHitbox(true);
            ca1.add(c1);
            c1.setPosition(0, 0);
            c1.setAttackPattern(AttackPatternFactory.getAttackPattern("BOSS_1_1"));
            c1.setHealth(scaledPP * 15f);
            c1.setMaxHealth(scaledPP * 15f);
            c1.setPpGain(pp * Options.PP_GAIN_MULTIPLIER * 10f);
            c1.setRadius(mainRadius);
            for(int i = 0; i < 16; i++) {
                CircleHitbox c = new CircleHitbox(true);
                ca1.add(c);
                float angle = i * MathUtils.degreesToRadians * (360/16f);
                c.setPosition((mainRadius + subRadius) * MathUtils.cos(angle), (mainRadius + subRadius) * MathUtils.sin(angle));
                c.setAttackPattern(AttackPatternFactory.getAttackPattern("BOSS_1_2"));
                c.setHealth(scaledPP);
                c.setMaxHealth(scaledPP);
                c.setPpGain(pp * Options.PP_GAIN_MULTIPLIER);
                c.setRadius(subRadius);
            }

            e1.setSpawnPosition(0, 0);
            Map.randomizeSimpleWanderAI(e1, mapAreaRadius);
            e1.setMaxSpeed(2f);
            e1.setCircleHitboxes(ca1);

            // Give subentities higher speed and stalker AI
            HitboxComponent.SubEntityStats subStats = new HitboxComponent.SubEntityStats();
            subStats.maxSpeed = 3.5f;
            subStats.aiData = new EntityCreationData(true);
            Map.randomizeSimpleStalkTargetAI(subStats.aiData);
            e1.setSubEntityStats(subStats);
        } else if(id == 1) {

        } else {
            return getBossById(0, mapAreaRadius, pp);
        }

        return ecds;
    }

    public static ArrayList<EntityCreationData> getBossByFloor(int floor, float mapAreaRadius, float pp) {
        //TODO: getBossById(floor % (number of bosses in the game), mapAreaRadius, pp)
        return getBossById(floor, mapAreaRadius, pp);
    }
}
