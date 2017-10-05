package map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.miv.Main;
import com.miv.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import components.AIComponent;
import components.BossComponent;
import components.EnemyComponent;
import components.HitboxComponent;
import map.mods.MapAreaModifier;
import map.mods.Mod;
import map.mods.Windy;
import utils.CircleHitbox;

/**
 * @see Map
 * Created by Miv on 5/23/2017.
 */
public class MapArea {
    public static final float MAP_AREA_MIN_SIZE = 800f;
    public static final float MAP_AREA_MAX_SIZE = 1600f;

    public static final float BOSS_MAP_AREA_SIZE = 1500f;

    private static final float CHANCE_OF_RARE_MAP = 0.04f;
    private static final float CHANCE_OF_UNCOMMON_MAP = 0.08f;

    public ArrayList<EntityCreationData> entityCreationDataArrayList;
    private float radius;

    private int enemyCount;
    private int originalEnemyCount;

    private ArrayList<MapAreaModifier> mods;

    private transient ArrayList<Entity> enemies;

    /**
     * Set to -1 if no stairs exist in this MapArea. Otherwise, an entity with an OnCollision event will be spawned in the middle of the MapArea
     * when {@link MapArea#spawnEntities(PooledEngine, Entity, boolean)} is called.
     */
    private int stairsDestination = -1;

    /**
     * For Json files
     */
    public MapArea() {}

    public MapArea(float radius) {
        this.radius = radius;
        entityCreationDataArrayList = new ArrayList<EntityCreationData>();
        enemies = new ArrayList<Entity>();
        mods = new ArrayList<MapAreaModifier>();
    }

    public void addStairs(int destinationFloor) {
        stairsDestination = destinationFloor;
    }

    /**
     * Spawns all entities in {@link map.MapArea#entityCreationDataArrayList}
     */
    public void spawnEntities(final PooledEngine engine, Entity player, boolean clearEntityCreationDataAfterSpawning) {
        enemies.clear();

        enemyCount = entityCreationDataArrayList.size();

        // Entities from entityCreationDataArrayList
        for(EntityCreationData ecd : entityCreationDataArrayList) {
            Entity e = engine.createEntity();

            if(ecd.isEnemy()) {
                e.add(engine.createComponent(EnemyComponent.class));
            }

            if(ecd.isBoss()) {
                e.add(engine.createComponent(BossComponent.class));
            }

            HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
            for(CircleHitbox c : ecd.getCircleHitboxes()) {
                c.randomizeAttackPatternTime();
                hitbox.addCircle(c, true);
            }
            hitbox.recenterOriginalCirclePositions();
            hitbox.setOrigin(ecd.getSpawnX(), ecd.getSpawnY());
            hitbox.setMaxSpeed(ecd.getMaxSpeed());
            // Have enemy always be shooting
            hitbox.setIsShooting(true);
            e.add(hitbox);

            if(ecd.getSubEntityStats() != null) {
                hitbox.setSubEntityStats(ecd.getSubEntityStats());
            }

            AIComponent aiComponent = Map.createAIComponent(engine, e, ecd, player);
            if(aiComponent != null) {
                e.add(aiComponent);
            }

            engine.addEntity(e);
            if(ecd.isEnemy()) {
                enemies.add(e);
            }
            onEntityEnter(e);
        }

        if(clearEntityCreationDataAfterSpawning && !isBossArea()) {
            entityCreationDataArrayList.clear();
        }
    }

    public void randomizeRarity(PooledEngine engine, AssetManager assetManager, Entity player) {
        List<Mod> mods = null;
        float rand = MathUtils.random();
        if(rand < 1f) { // Rare map area
            mods = pickNRandomMods(Arrays.asList(Mod.values()), 5);
        } else if(rand < CHANCE_OF_UNCOMMON_MAP + CHANCE_OF_RARE_MAP) { // Uncommon map area
            mods = pickNRandomMods(Arrays.asList(Mod.values()), MathUtils.random(2, 3));
        }

        if(mods != null) {
            for(Mod mod : mods) {
                try {
                    MapAreaModifier m = mod.getImpl().getConstructor(PooledEngine.class, AssetManager.class, MapArea.class, Entity.class).newInstance(new Object[]{engine, assetManager, this, player});
                    this.mods.add(m);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<Mod> pickNRandomMods(List<Mod> list, int n) {
        List<Mod> copy = new LinkedList<Mod>(list);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    public void storeExistingEnemies(PooledEngine engine, boolean deleteEntitiesAfterwards) {
        entityCreationDataArrayList.clear();

        for (Entity e : engine.getEntitiesFor(Family.all(EnemyComponent.class, HitboxComponent.class).get())) {
            EntityCreationData ecd = new EntityCreationData();
            ecd.setIsEnemy(true);
            if (Mappers.boss.has(e)) {
                ecd.setIsBoss(true);
            }

            // Save AI
            if(Mappers.ai.has(e)) {
                Mappers.ai.get(e).getAi().saveToEntityCreationData(ecd);
            }

            // Save position
            ecd.setSpawnPosition(Mappers.hitbox.get(e).getOrigin().x, Mappers.hitbox.get(e).getOrigin().y);

            // Restore health
            for(CircleHitbox c : Mappers.hitbox.get(e).getCircles()) {
                c.setHealth(c.getMaxHealth());
            }

            ArrayList<CircleHitbox> circles = new ArrayList<CircleHitbox>();
            circles.addAll(Mappers.hitbox.get(e).getCircles());
            ecd.setCircleHitboxes(circles);

            ecd.setGravitationalRadius(Mappers.hitbox.get(e).getGravitationalRadius());

            entityCreationDataArrayList.add(ecd);

            if(deleteEntitiesAfterwards) {
                engine.removeEntity(e);
            }
        }
    }

    /**
     * Called from {@link Map#randomlyPopulate(MapArea)}
     */
    public void onEnemyDataCreation(EntityCreationData ecd) {
        for(MapAreaModifier m : mods) {
            m.onEnemyDataCreation(ecd);
        }
    }

    /**
     * Called from {@link systems.MovementSystem#handleBulletCollision(Entity, CircleHitbox, Entity)}
     */
    public void onEnemyDeath(Entity enemy) {
        enemies.remove(enemy);
        for(MapAreaModifier m : mods) {
            m.onEnemyDeath(enemy);
        }
    }

    /**
     * Called from {@link #spawnEntities(PooledEngine, Entity, boolean)} (for enemies) and {@link Map#enterNewArea(PooledEngine, Entity, int, int, boolean)} (for player)
     */
    public void onEntityEnter(Entity entity) {
        for(MapAreaModifier m : mods) {
            m.onEntityEnter(entity);
        }
    }

    /**
     * Called from {@link Map#enterNewArea(PooledEngine, Entity, int, int, boolean)} (for player)
     */
    public void onPlayerLeave() {
        for(MapAreaModifier m : mods) {
            m.onPlayerLeave();
        }
    }

    /**
     * Called from {@link systems.MovementSystem#handleBulletCollision(Entity, CircleHitbox, Entity)}
     */
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {
        for(MapAreaModifier m : mods) {
            m.onEnemyCircleDeath(enemy, circle);
        }
    }

    /**
     * Called from {@link Main#render()}
     */
    public void update(float deltaTime) {
        for(MapAreaModifier m : mods) {
            m.update(deltaTime);
        }
    }

    public float getRadius() {
        return radius;
    }

    public int getEnemyCount() {
        return enemyCount;
    }

    public void addEnemy(Entity e) {
        enemies.add(e);
    }

    /**
     * Used for when killing enemies
     */
    public void setEnemyCount(Main main, int enemyCount) {
        this.enemyCount = enemyCount;
        main.updateScreenActors();

        if(enemyCount == 0) {
            if(stairsDestination != -1) {
                main.getHud().getMoveToNextFloorButton().setVisible(false);
            }
        }
    }

    public void setEnemyCount(int enemyCount) {
        this.enemyCount = enemyCount;
        if(enemyCount == 0) {
            System.out.println("asdjioafh24 DON'T USE THIS FUNCTION WHEN SETTING ENEMY COUNT TO 0 YOU FOOL");
        }
    }

    public int getStairsDestination() {
        return stairsDestination;
    }

    public int getOriginalEnemyCount() {
        return originalEnemyCount;
    }

    public void setOriginalEnemyCount(int originalEnemyCount) {
        this.originalEnemyCount = originalEnemyCount;
    }

    public boolean isBossArea() {
        return stairsDestination != -1;
    }

    public ArrayList<Entity> getEnemies() {
        return enemies;
    }

    public ArrayList<MapAreaModifier> getMods() {
        return mods;
    }
}
