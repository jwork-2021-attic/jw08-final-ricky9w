package com.ricky;

import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.components.RandomAStarMoveComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.AnimatedTexture;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import kotlin.ExtensionFunctionType;

import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;

import javax.sound.midi.Receiver;
import javax.swing.plaf.TextUI;

import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.PacType.*;
import static com.ricky.Config.*;
import com.ricky.components.PlayerComponent;

public class PacFactory implements EntityFactory {
    
    // TODO: 细化texture等信息
    @Spawns("1")
    public Entity newBlock(SpawnData data) {
        var rect = new Rectangle(38, 38, Color.BLACK);
        rect.setArcWidth(25);
        rect.setArcHeight(25);
        rect.setStrokeWidth(1);
        rect.setStroke(Color.BLUE);

        return entityBuilder(data)
                .type(BLOCK)
                .viewWithBBox(rect)
                .zIndex(-1)
                .build();
    }

    @Spawns("0")
    public Entity newCoin(SpawnData data) {
        var view = texture("coin.png");
        view.setTranslateX(5);
        view.setTranslateY(5);

        return entityBuilder(data)
                .type(COIN)
                .bbox(new HitBox(new Point2D(5, 5), BoundingShape.box(30, 30)))
                .view(view)
                .zIndex(-1)
                .collidable()
                .with(new CellMoveComponent(BLOCK_SIZE, BLOCK_SIZE, 50))
                .scale(0.5, 0.5)
                .build();
    }

    @Spawns("P")
    public Entity newPlayer(SpawnData data) {
        AnimatedTexture view = texture("player.png").toAnimatedTexture(2, Duration.seconds(0.33));

        return entityBuilder(data)
                .type(PLAYER)
                .bbox(new HitBox(new Point2D(4, 4), BoundingShape.box(32, 32)))
                .anchorFromCenter()
                .view(view.loop())
                .with(new CollidableComponent(true))
                .with(new CellMoveComponent(BLOCK_SIZE, BLOCK_SIZE, 200).allowRotation(true))
                .with(new AStarMoveComponent(new LazyValue<>(() -> geto("grid"))))
                .with(new PlayerComponent())
                .rotationOrigin(35 / 2.0, 40 / 2.0)
                .build();
    }

    // TODO: 设置敌人移动操控
    @Spawns("E")
    public Entity newEnemy(SpawnData data) {
        return entityBuilder(data)
                .type(ENEMY)
                .bbox(new HitBox(new Point2D(2, 2), BoundingShape.box(36, 36)))
                .anchorFromCenter()
                .with(new CollidableComponent(true))
                .with(new CellMoveComponent(BLOCK_SIZE, BLOCK_SIZE, 125))
                .with(new AStarMoveComponent(new LazyValue<>(() -> geto("grid"))))
                .build();
    }



}