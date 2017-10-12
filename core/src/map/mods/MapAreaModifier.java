package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;

import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * Created by Miv on 9/28/2017.
 */
public abstract class MapAreaModifier {
    protected transient PooledEngine engine;
    protected transient AssetManager assetManager;
    protected transient MapArea mapArea;
    protected transient Entity player;
    protected String displayName;

    // For Json
    public MapAreaModifier() {

    }

    public MapAreaModifier(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        this.engine = engine;
        this.assetManager = assetManager;
        this.mapArea = mapArea;
        this.player = player;
    }

    // Called when an enemy dies
    public abstract void onEnemyDeath(Entity enemy);
    // Called when an enemy circle dies
    public abstract void onEnemyCircleDeath(Entity enemy, CircleHitbox circle);
    // Called when an EntityCreationData is added to the map area
    public abstract void onEnemyDataCreation(EntityCreationData ecd);
    // Called when an entity enters the map area (includes enemies spawning)
    public abstract void onEntityEnter(Entity entity);
    // Called when the player leaves the map area
    public abstract void onPlayerLeave();
    public abstract void update(float deltaTime);

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public void setMapArea(MapArea mapArea) {
        this.mapArea = mapArea;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setEngine(PooledEngine engine) {
        this.engine = engine;
    }
}
