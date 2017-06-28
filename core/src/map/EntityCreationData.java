package map;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Circle;

import java.util.ArrayList;

import components.EnemyComponent;
import components.PlayerComponent;
import utils.CircleHitbox;

/**
 * Class used by {@link map.MapArea} that contains the data to create an entity upon entering the MapArea.
 * Cannot be used to create player entities.
 * Created by Miv on 5/23/2017.
 */
public class EntityCreationData {
    private float maxHealth;
    private boolean isEnemy;
    private boolean isBoss;
    private ArrayList<CircleHitbox> circleHitboxes;
    private float spawnX;
    private float spawnY;

    public EntityCreationData() {
        circleHitboxes = new ArrayList<CircleHitbox>();
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public boolean isEnemy() {
        return isEnemy;
    }

    public void setIsEnemy(boolean isEnemy) {
        this.isEnemy = isEnemy;
    }

    public boolean isBoss() {
        return isBoss;
    }

    public void setIsBoss(boolean isBoss) {
        this.isBoss = isBoss;
    }

    public ArrayList<CircleHitbox> getCircleHitboxes() {
        return circleHitboxes;
    }

    public void setCircleHitboxes(ArrayList<CircleHitbox> circleHitboxes) {
        this.circleHitboxes = circleHitboxes;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public void setSpawnPosition(float x, float y) {
        spawnX = x;
        spawnY = y;
    }
}
