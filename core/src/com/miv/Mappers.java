package com.miv;

import com.badlogic.ashley.core.ComponentMapper;

import components.BossComponent;
import components.CustomOnCollisionComponent;
import components.EnemyBulletComponent;
import components.EnemyComponent;
import components.HealthComponent;
import components.HitboxComponent;
import components.PlayerBulletComponent;
import components.PlayerComponent;

/**
 * Created by Miv on 5/25/2017.
 */
public class Mappers {
    public static ComponentMapper<BossComponent> boss = ComponentMapper.getFor(BossComponent.class);
    public static ComponentMapper<CustomOnCollisionComponent> customOnCollision = ComponentMapper.getFor(CustomOnCollisionComponent.class
    );
    public static ComponentMapper<EnemyBulletComponent> enemyBullet = ComponentMapper.getFor(EnemyBulletComponent.class);
    public static ComponentMapper<EnemyComponent> enemy = ComponentMapper.getFor(EnemyComponent.class);
    public static ComponentMapper<HealthComponent> health = ComponentMapper.getFor(HealthComponent.class);
    public static ComponentMapper<HitboxComponent> hitbox = ComponentMapper.getFor(HitboxComponent.class);
    public static ComponentMapper<PlayerBulletComponent> playerBullet = ComponentMapper.getFor(PlayerBulletComponent.class);
    public static ComponentMapper<PlayerComponent> player = ComponentMapper.getFor(PlayerComponent.class);
}
