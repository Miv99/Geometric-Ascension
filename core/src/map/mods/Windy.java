package map.mods;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.miv.Main;
import com.miv.Mappers;
import com.miv.Options;

import components.HitboxComponent;
import map.EntityCreationData;
import map.MapArea;
import utils.CircleHitbox;

/**
 * A wind current blowing at a random angle with a random magnitude appears every few seconds, pushing the player and all enemies
 * Created by Miv on 9/29/2017.
 */
public class Windy extends MapAreaModifier {
    private transient float timeUntilNewWindCurrent;
    private transient float windX;
    private transient float windY;
    private transient Sound windWooshSound;

    public Windy() {

    }

    public Windy(PooledEngine engine, AssetManager assetManager, MapArea mapArea, Entity player) {
        super(engine, assetManager, mapArea, player);
        displayName = "Windy";
        windWooshSound = assetManager.get(assetManager.getFileHandleResolver().resolve(Main.WIND_WOOSH_SOUND_PATH).path());
        newWindCurrent();
    }

    @Override
    public void onEntityEnter(Entity entity) {
        Vector2 vel2 = Mappers.hitbox.get(entity).getVelocity2();
        Mappers.hitbox.get(entity).setVelocity2(vel2.x + windX, vel2.y + windY);
    }

    @Override
    public void onPlayerLeave() {
        // No need to stop wind on enemies since a new HitboxComponent is constructed for each enemy when player enters new area
        HitboxComponent playerHitbox = Mappers.hitbox.get(player);
        Vector2 playerVel2 = playerHitbox.getVelocity2();
        playerHitbox.setVelocity2(playerVel2.x - windX, playerVel2.y - windY);
    }

    private void newWindCurrent() {
        // Stop wind on all entities
        for(Entity e : mapArea.getEnemies()) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            Vector2 vel2 = hitbox.getVelocity2();
            hitbox.setVelocity2(vel2.x - windX, vel2.y - windY);
        }
        HitboxComponent playerHitbox = Mappers.hitbox.get(player);
        Vector2 playerVel2 = playerHitbox.getVelocity2();
        playerHitbox.setVelocity2(playerVel2.x - windX, playerVel2.y - windY);

        // New wind direction and magnitude
        float angle = MathUtils.random(MathUtils.PI2);
        float magnitude = MathUtils.random(1f, Options.PLAYER_BASE_MAX_SPEED/2f);
        windX = magnitude * MathUtils.cos(angle);
        windY = magnitude * MathUtils.sin(angle);

        // Apply wind
        for(Entity e : mapArea.getEnemies()) {
            HitboxComponent hitbox = Mappers.hitbox.get(e);
            Vector2 vel2 = hitbox.getVelocity2();
            hitbox.setVelocity2(vel2.x + windX, vel2.y + windY);
        }
        playerHitbox.setVelocity2(playerVel2.x + windX, playerVel2.y + windY);

        // Play wind woosh sound
        windWooshSound.play(Options.MASTER_VOLUME * Options.SOUND_VOLUME);

        timeUntilNewWindCurrent = MathUtils.random(6f, 12f);
    }

    @Override
    public void update(float deltaTime) {
        timeUntilNewWindCurrent -= deltaTime;
        if(timeUntilNewWindCurrent <= 0) {
            newWindCurrent();
        }
    }

    @Override
    public void onEnemyDeath(Entity enemy) {

    }

    @Override
    public void onEnemyCircleDeath(Entity enemy, CircleHitbox circle) {

    }

    @Override
    public void onEnemyDataCreation(EntityCreationData ecd) {
        ecd.multiplyPpGain(1.25f);
    }
}
