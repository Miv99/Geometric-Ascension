package com.miv;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;

import components.HitboxComponent;
import components.PlayerComponent;
import factories.AttackPatternFactory;
import map.Map;
import systems.RenderSystem;
import utils.CircleHitbox;
import utils.Point;

/**
 * Game autosaves when entering a new map area (only via travelling) and when a map area is cleared of all enemies.
 * Created by Miv on 7/10/2017.
 */

public class Save {
    private static final String SAVE_DATA_PATH = "Geometric Ascension\\save_data.json";

    private static class SaveData {
        private ArrayList<CircleHitbox> playerCircles;
        private PlayerComponent playerPlayerComponent;
        private Map map;
        private float playerMaxSpeed;
        private Point playerOrigin;

        public SaveData() {

        }

        private SaveData(ArrayList<CircleHitbox> playerCircles, float playerMaxSpeed, PlayerComponent playerPlayerComponent, Map map, Point playerOrigin) {
            this.playerCircles = playerCircles;
            this.playerMaxSpeed = playerMaxSpeed;
            this.playerPlayerComponent = playerPlayerComponent;
            this.map = map;
            this.playerOrigin = playerOrigin;
        }
    }

    /**
     * Loads the saved player entity and map data into {@link Main#player} and {@link Main#map}.
     * If no save data exists, the default player and map are loaded.
     * @return True if a new save file was created; false if a pre-existing one was loaded
     */
    public static boolean load(Main main) {
        PooledEngine engine = main.getEngine();
        engine.removeAllEntities();

        if(Gdx.files.local(SAVE_DATA_PATH).exists()) {
            try {
                Json save = new Json();
                SaveData data = save.fromJson(SaveData.class, Gdx.files.local(SAVE_DATA_PATH));

                // Load player data
                Entity player = engine.createEntity();
                HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
                for (CircleHitbox c : data.playerCircles) {
                    hitbox.addCircle(c, false);
                }
                hitbox.setLastFacedAngle(MathUtils.PI / 2f);
                hitbox.recenterOriginalCirclePositions();
                hitbox.setMaxSpeed(data.playerMaxSpeed);
                hitbox.setOrigin(data.playerOrigin.x, data.playerOrigin.y);
                player.add(hitbox);
                player.add(data.playerPlayerComponent);
                main.setPlayer(player);

                // Load map
                main.setMap(data.map);

                return false;
            } catch(Exception e) {
                createNewSave(engine, main);
                return true;
            }
        } else {
            createNewSave(engine, main);
            return true;
        }
    }

    private static void createNewSave(PooledEngine engine, Main main) {
        System.out.println("CREATED NEW SAVE DATA");

        // Create new player entity
        Entity player = engine.createEntity();
        HitboxComponent hitboxComponent = engine.createComponent(HitboxComponent.class);
        hitboxComponent.setMaxSpeed(Options.PLAYER_BASE_MAX_SPEED);
        CircleHitbox c = new CircleHitbox();
        c.setHitboxTextureType(RenderSystem.HitboxTextureType.PLAYER);
        c.setRadius(Options.DEFAULT_NEW_CIRCLE_RADIUS);
        c.setMaxHealth(Options.DEFAULT_NEW_CIRCLE_MAX_HEALTH);
        c.setHealth(c.getMaxHealth());
        c.setAttackPattern(AttackPatternFactory.getAttackPattern("PLAYER_DEFAULT_1"));
        hitboxComponent.addCircle(c, true);
        hitboxComponent.recenterOriginalCirclePositions();
        player.add(hitboxComponent);
        player.add(engine.createComponent(PlayerComponent.class));
        main.setPlayer(player);

        // Create new map
        main.setMap(new Map(main));
    }

    public static void deleteSave() {
        if(Gdx.files.local(SAVE_DATA_PATH).exists()) {
            Gdx.files.local(SAVE_DATA_PATH).delete();
        }
    }

    public static void save(Main session) {
        // TODO: auto save every time player kills all enemies in MapArea or enters new floor
        if(!session.isPlayerDead()) {
            SaveData saveData = new SaveData(Mappers.hitbox.get(session.getPlayer()).getCircles(), Mappers.hitbox.get(session.getPlayer()).getMaxSpeed(),
                    Mappers.player.get(session.getPlayer()), session.getMap(), Mappers.hitbox.get(session.getPlayer()).getOrigin());

            Json save = new Json();
            System.out.println(save.toJson(saveData));
            Gdx.files.local(SAVE_DATA_PATH).writeString(save.toJson(saveData), false);
            System.out.println("SAVED");
        }
    }
}
