package systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Options;

import components.HitboxComponent;
import map.Map;
import utils.Point;
import utils.Utils;

/**
 * Created by Miv on 6/17/2017.
 */
public class ShootingSystem extends EntitySystem {
    private Map map;
    private PooledEngine engine;
    private Entity player;
    private ImmutableArray<Entity> entities;

    private Array<Sound> popSounds;

    public ShootingSystem(Map map, PooledEngine engine) {
        this.map = map;
        this.engine = engine;
        popSounds = new Array<Sound>();
    }

    public void loadAssets(AssetManager assetManager) {
        popSounds.clear();
        for(String s : Main.POP_SOUND_PATHS) {
            Sound sound = assetManager.get(assetManager.getFileHandleResolver().resolve(s).path());
            popSounds.add(sound);
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(HitboxComponent.class).get());
    }

    @Override
    public void update(float deltaTime) {
        Point ear = Mappers.hitbox.get(player).getOrigin();

        for(Entity e : entities) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            if(hitbox.isShooting() && !hitbox.isTravelling()) {
                if(hitbox.update(engine, e, player, map.getCurrentArea().getRadius(), deltaTime)) {
                    // Play pop sound
                    Point origin = hitbox.getOrigin();
                    Utils.playDecayingSound(popSounds.random(), Options.BULLET_BUBBLE_POP_VOLUME, origin.x, origin.y, ear);
                }
            }
        }
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public void setMap(Map map) {
        this.map = map;
    }
}
