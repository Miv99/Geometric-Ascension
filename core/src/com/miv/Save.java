package com.miv;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;

import components.HitboxComponent;
import components.PlayerComponent;
import factories.AttackPatternFactory;
import map.Map;
import systems.RenderSystem;
import utils.CircleHitbox;

/**
 * Created by Miv on 7/10/2017.
 */

public class Save {
    private static final String SAVE_DATA_PATH = "Geometric Ascension\\save_data.json";

    private static class SaveData {
        private ArrayList<CircleHitbox> playerCircles;
        private PlayerComponent playerPlayerComponent;
        private Map map;
        private float playerMaxSpeed;

        public SaveData() {

        }

        private SaveData(ArrayList<CircleHitbox> playerCircles, float playerMaxSpeed, PlayerComponent playerPlayerComponent, Map map) {
            this.playerCircles = playerCircles;
            this.playerMaxSpeed = playerMaxSpeed;
            this.playerPlayerComponent = playerPlayerComponent;
            this.map = map;
        }
    }

    /**
     * Loads the saved player entity and map data into {@link Main#player} and {@link Main#map}.
     * If no save data exists, the default player and map are loaded.
     */
    public static void load(Main main) {
        PooledEngine engine = main.getEngine();

        if(Gdx.files.local(SAVE_DATA_PATH).exists()) {
            Json save = new Json();
            SaveData data = save.fromJson(SaveData.class, Gdx.files.local(SAVE_DATA_PATH));

            // Load player data
            Entity player = engine.createEntity();
            HitboxComponent hitbox = engine.createComponent(HitboxComponent.class);
            for(CircleHitbox c : data.playerCircles) {
                hitbox.addCircle(c);
            }
            hitbox.recenterOriginalCirclePositions();
            hitbox.setMaxSpeed(data.playerMaxSpeed);
            player.add(hitbox);
            player.add(data.playerPlayerComponent);
            main.setPlayer(player);

            // Load map
            main.setMap(data.map);
        } else {
            // Create new player entity
            Entity player = engine.createEntity();
            HitboxComponent hitboxComponent = engine.createComponent(HitboxComponent.class);
            hitboxComponent.setMaxSpeed(5f);
            CircleHitbox c = new CircleHitbox();
            c.setHitboxTextureType(RenderSystem.HitboxTextureType.PLAYER);
            c.setRadius(40f);
            c.setMaxHealth(500f);
            c.setHealth(500f);
            c.setAttackPattern(AttackPatternFactory.getAttackPattern("PLAYER_STARTING"));
            hitboxComponent.addCircle(c);
            player.add(hitboxComponent);
            player.add(engine.createComponent(PlayerComponent.class));
            main.setPlayer(player);

            // Create new map
            main.setMap(new Map(main));
        }
    }

    public static void save(Main session) {
        // TODO: auto save every time player kills all enemies in MapArea or enters new floor
        SaveData saveData = new SaveData(Mappers.hitbox.get(session.getPlayer()).getCircles(), Mappers.hitbox.get(session.getPlayer()).getMaxSpeed(),
                Mappers.player.get(session.getPlayer()), session.getMap());

        Json save = new Json();
        Gdx.files.local(SAVE_DATA_PATH).writeString(save.toJson(saveData), false);
        System.out.println("SAVED");
    }
}
