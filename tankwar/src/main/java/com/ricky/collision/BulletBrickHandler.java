package com.ricky.collision;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ricky.TankWarType;

public class BulletBrickHandler extends CollisionHandler {
    
    public BulletBrickHandler() {
        super(TankWarType.BULLET, TankWarType.BRICK);
    }

    @Override
    protected void onCollisionBegin(Entity bullet, Entity brick) {
        bullet.removeFromWorld();
        brick.call("onHit");
    }
}
