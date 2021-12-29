package com.ricky;

import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.components.RandomAStarMoveComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.IDComponent;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.texture.AnimatedTexture;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Map;
import java.util.function.Supplier;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.texture;

import static com.ricky.PacType.*;
import static com.ricky.Config.*;
import com.ricky.components.PlayerComponent;
import com.ricky.components.ai.DelayChaseComponent;

public class PacFactory implements EntityFactory {
    
    @Spawns("1")
    public Entity newBlock(SpawnData data) {
        var rect = new Rectangle(38, 38, Color.BLACK);
        rect.setArcWidth(25);
        rect.setArcHeight(25);
        rect.setStrokeWidth(1);
        rect.setStroke(Color.GRAY);

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
                .with(new IDComponent(data.get("name"), data.get("id")))
                .build();
    }

    private Supplier<Component> aiComponents = new Supplier<>() {
        private Map<Integer, Supplier<Component>> components = Map.of(
            0, () -> new DelayChaseComponent().withDelay(),
            1, RandomAStarMoveComponent::new,
            2, DelayChaseComponent::new
        );

        private int index = 0;

        @Override
        public Component get() {
            if (index == 3)
                index = 0;

            return components.get(index++).get();
        }
    };

    @Spawns("E")
    public Entity newEnemy(SpawnData data) {
        return entityBuilder(data)
                .type(ENEMY)
                .bbox(new HitBox(new Point2D(2, 2), BoundingShape.box(32, 36)))
                .anchorFromCenter()
                .with(new CollidableComponent(true))
                .viewWithBBox(texture("enemy.png", 36, 36))
                .with(new CellMoveComponent(BLOCK_SIZE, BLOCK_SIZE, 125))
                .with(new AStarMoveComponent(new LazyValue<>(() -> geto("grid"))))
                .with(aiComponents.get())
                .build();
    }

}
