package factories;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Options;

import java.util.ArrayList;

import javax.swing.text.html.Option;

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
    /**
     * MAKE SURE EVERY NEW ECD CREATED IS DONE USING  new EntityCreationData(true); AND NOT  new EntityCreationData();
     */
    public static ArrayList<EntityCreationData> getBossById(int id, float mapAreaRadius, float pp) {
        ArrayList<EntityCreationData> ecds = new ArrayList<EntityCreationData>();

        // Big circle surrounded by smaller circles
        if(id == 0) {
            float mainRadius = 450f;
            float subRadius = 50f;

            EntityCreationData e1 = new EntityCreationData(true);
            ecds.add(e1);

            ArrayList<CircleHitbox> ca1 = new ArrayList<CircleHitbox>();
            e1.setCircleHitboxes(ca1);
            ca1.add(new CircleHitbox(
                    RenderSystem.HitboxTextureType.ENEMY,
                    AttackPatternFactory.getAttackPattern("BOSS_1_1").addRandomAttackPatternStatModifiers(pp),
                    0, 0,
                    mainRadius,
                    pp * 150f,
                    pp * 10f * Options.PP_GAIN_MULTIPLIER
            ));
            for(int i = 0; i < 16; i++) {
                float angle = i * MathUtils.degreesToRadians * (360/16f);
                ca1.add(new CircleHitbox(
                        RenderSystem.HitboxTextureType.ENEMY,
                        AttackPatternFactory.getAttackPattern("BOSS_1_1").addRandomAttackPatternStatModifiers(pp),
                        (mainRadius + subRadius) * MathUtils.cos(angle), (mainRadius + subRadius) * MathUtils.sin(angle),
                        subRadius,
                        pp * 10f,
                        pp * Options.PP_GAIN_MULTIPLIER
                ));
            }

            e1.setSpawnPosition(0, 0);
            Map.randomizeSimpleWanderAI(e1, mapAreaRadius);
            e1.setMaxSpeed(2f);

            // Give subentities higher speed and stalker AI
            HitboxComponent.SubEntityStats subStats = new HitboxComponent.SubEntityStats();
            subStats.maxSpeed = 3.5f;
            subStats.aiData = new EntityCreationData(true);
            Map.randomizeSimpleStalkTargetAI(subStats.aiData);
            e1.setSubEntityStats(subStats);
        }
        // Square made of circles
        else if(id == 1) {
            float radius = 50f;
            int squareLength = 5;

            EntityCreationData e1 = new EntityCreationData(true);
            ecds.add(e1);

            ArrayList<CircleHitbox> ca1 = new ArrayList<CircleHitbox>();
            e1.setCircleHitboxes(ca1);
            for(int x = 0; x < squareLength; x++) {
                for(int y = 0; y < squareLength; y++) {
                    ca1.add(new CircleHitbox(
                            RenderSystem.HitboxTextureType.ENEMY,
                            AttackPatternFactory.getAttackPattern("BOSS_2_1").addRandomAttackPatternStatModifiers(pp),
                            x * radius*2f, y * radius*2f,
                            radius,
                            //pp * 10f,
                            pp,
                            pp * Options.PP_GAIN_MULTIPLIER
                    ));
                }
            }

            e1.setSpawnPosition(0, 0);
            Map.randomizeSimpleStalkTargetAI(e1);
            e1.setMaxSpeed(1.5f);
        }
        else if(id == 2) {
            float radius = 50f;
            int squareLength = 7;

            EntityCreationData e1 = new EntityCreationData(true);
            ecds.add(e1);

            ArrayList<CircleHitbox> ca1 = new ArrayList<CircleHitbox>();
            e1.setCircleHitboxes(ca1);
        }
        else {
            return getBossById(0, mapAreaRadius, pp);
        }

        return ecds;
    }

    public static ArrayList<EntityCreationData> getBossByFloor(int floor, float mapAreaRadius, float pp) {
        //TODO: getBossById(floor % (number of bosses in the game), mapAreaRadius, pp)
        return getBossById(floor, mapAreaRadius, pp);
    }
}
