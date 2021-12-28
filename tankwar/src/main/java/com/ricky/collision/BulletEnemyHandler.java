package com.ricky.collision;

import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;

import static com.ricky.TankWarType.BULLET;
import static com.ricky.TankWarType.ENEMY;

public class BulletEnemyHandler extends CollisionHandler {

    public BulletEnemyHandler() {
        super(BULLET, ENEMY);
    }
    
    @Override
    protected void onCollisionBegin(Entity bullet, Entity tank) {
        bullet.removeFromWorld();

        var hp = tank.getComponent(HealthIntComponent.class);

        hp.damage(1);

        if (hp.isZero()) {
            tank.removeFromWorld();
        }
    }
}
