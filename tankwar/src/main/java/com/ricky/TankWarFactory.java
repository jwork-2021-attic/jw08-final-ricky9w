package com.ricky;

import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.action.ActionComponent;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.state.StateComponent;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.geometry.Point2D;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import kotlin.ExtensionFunctionType;

import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

import com.ricky.components.*;
import static com.ricky.TankWarType.*;

import static com.ricky.Config.BLOCK_SIZE;
import static com.ricky.Config.BULLET_SPEED;
import static com.ricky.Config.TANK_SPEED;
import static com.ricky.Config.TANK_HEALTH;

public class TankWarFactory implements EntityFactory {

    // 生成新玩家
    @Spawns("player,playerSpawnPoint")
    public Entity newPlayer(SpawnData data) {
        return newTank(data)
                .type(PLAYER)
                .with(new PlayerArrowViewComponent())
                .rotationOrigin(new Point2D(18, 18))
                .build();
    }

    // FIXME: 在游戏控制模块中生成敌人或在工厂中生成敌人


    // 创建玩家并设置相关属性
    private EntityBuilder newTank(SpawnData data) {
        return entityBuilder(data)
                .bbox(new HitBox(new Point2D(5, 5), BoundingShape.box(26, 26)))
                .collidable()
                .with(new HealthIntComponent(TANK_HEALTH))
                .with(new MoveComponent())
                .with(new TankComponent())
                .with(new CellMoveComponent(BLOCK_SIZE / 2, BLOCK_SIZE / 2, TANK_SPEED).allowRotation(true))
                .with(new ActionComponent())
                .with(new StateComponent());
    }

    @Spawns("Bullet")
    public Entity newBullet(SpawnData data) {
        // TODO: 根据owner信息做碰撞判断
        Entity owner = data.get("owner");

        return entityBuilder(data)
                .at(data.getX() - 8, data.getY() - 8)
                .viewWithBBox("tank_bullet.png")
                .scale(0.5, 0.5)
                .with(new OffscreenCleanComponent())
                .with(new ProjectileComponent(data.get("direction"), BULLET_SPEED))
                .build();
    }

    @Spawns("wall")
    public Entity newWall(SpawnData data) {
        return entityBuilder(data)
                .type(WALL)
                .viewWithBBox(new Rectangle(data.<Integer>get("width"), data.<Integer>get("height")))
                .collidable()
                .build();
    }

    @Spawns("brick")
    public Entity newBrick(SpawnData data) {
        return entityBuilder(data)
                .type(BRICK)
                .viewWithBBox(texture("brick.png", data.<Integer>get("width"), data.<Integer>get("height")))
                .collidable()
                .with(new BrickComponent())
                .build();
    }

}
