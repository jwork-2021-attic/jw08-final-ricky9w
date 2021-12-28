package com.ricky.components;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.ricky.TankWarType;

import java.io.ObjectInputValidation;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;

import javax.print.attribute.standard.RequestingUserName;

import static com.ricky.TankWarType.*;

public class MoveComponent extends Component {
    
    // 碰撞体积大小, 用于碰撞检测
    private BoundingBoxComponent bbox;

    private Direction moveDir;

    // 同一帧当中只能有一次移动
    private boolean movedThisFrame = false;

    private LazyValue<EntityGroup> blocks = new LazyValue<>(() -> {
        return entity.getWorld().getGroup(WALL, BRICK);
    });

    public Direction getMoveDir() {
        return moveDir;
    }

    public void setMoveDirection(Direction moveDir) {
        if (movedThisFrame)
            return;
        
        movedThisFrame = true;

        this.moveDir = moveDir;

        switch (moveDir) {
            case UP:
                up();
                break;
            case DOWN:
                down();
                break;
            case LEFT:
                left();
                break;
            case RIGHT:
                right();
                break;
        }
    }
    private double speed = 0;

    // 初始状态下设置随机朝向
    @Override
    public void onAdded() {
        moveDir = FXGLMath.random(Direction.values()).get();
    }


    // 每一帧开始时的更新
    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60;

        movedThisFrame = false;
    }

    private void up() {
        getEntity().setRotation(270);
        move(0, -5 * speed);
    }

    private void down() {
        getEntity().setRotation(90);
        move(0, 5 * speed);
    }

    private void left() {
        getEntity().setRotation(180);
        move(-5 * speed, 0);
    }

    private void right() {
        getEntity().setRotation(0);
        move(5 * speed, 0);
    }

    private Vec2 velocity = new Vec2();

    //  移动到指定地点
    private void move(double dx, double dy) {
        if (!getEntity().isActive())
            return;

        velocity.set((float) dx, (float) dy);

        // 移动距离
        int distance = Math.round(velocity.length());

        velocity.normalizeLocal();

        var cpBlocks = blocks.get().getEntitiesCopy();

        for (int i = 0; i < distance; i++) {
            entity.translate(velocity.x, velocity.y);

            // FIXME: 碰撞检测
            boolean collision = false;

            for (int j = 0; j < cpBlocks.size(); j++) {
                if (cpBlocks.get(j).getBoundingBoxComponent().isCollidingWith(bbox)) {
                    collision = true;
                    break;
                }
            }

            if (collision) {
                entity.translate(-velocity.x, -velocity.y);
                break;
            }
        }
        
    }

}
