package com.ricky.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.ViewComponent;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.spawn;
import static com.almasb.fxgl.dsl.FXGL.texture;

import static com.ricky.components.Direction.*;
import com.ricky.Config;


public class TankComponent extends Component {
    
    // 移动组件
    private MoveComponent moveComponent;

    // 图形展示组件
    private ViewComponent view;

    private Texture texture;

    // 单个坦克图片宽度x高度
    private double frameWidth;
    private double frameHeight;

    // 射击冷却计时器
    private LocalTimer shootTimer = FXGL.newLocalTimer();

    @Override
    public void onAdded() {
        texture = texture("player.png").multiplyColor(Color.LIGHTBLUE);
        view.addChild(texture);

        // 坦克移动图片由8帧组成
        frameWidth = texture.getWidth() / 8;
        frameHeight = texture.getHeight();
    }

    private double speed = 0;
    private int frames = 0;

    @Override
    public void onUpdate(double tpf) {
        speed = tpf * 60;

        // 显示坦克移动时动图效果
        int frame = frames / 10;
        if (frame >= 8) {
            frame = 0;
            frames = 0;
        }

        texture.setViewport(new Rectangle2D(frame * frameHeight, 0, frameWidth, frameHeight));
    }

    // TODO: 添加坦克行动相关处理: 上下左右和射击
    public void up() {
        moveComponent.setMoveDirection(Direction.UP);
        frames++;
    }

    public void down() {
        moveComponent.setMoveDirection(Direction.DOWN);
        frames++;
    }

    public void left() {
        moveComponent.setMoveDirection(Direction.LEFT);
        frames++;
    }

    public void right() {
        moveComponent.setMoveDirection(Direction.RIGHT);
        frames++;
    }

    public void shoot() {
        // 射击冷却中
        if (!shootTimer.elapsed(Config.SHOOT_DELAY))
            return;

        // 在当前位置生成子弹, 设置方向为当前朝向, 拥有者为自身
        spawn("Bullet", new SpawnData(getEntity().getCenter())
                .put("direction", angleToVector())
                .put("owner", entity)
        );

        // 重置射击计时器
        shootTimer.capture();
    }

    // 根据物体旋转判断此时坦克的方向
    private Point2D angleToVector() {
        double angle = getEntity().getRotation();

        if (angle == 0) { // 向右
            return new Point2D(1, 0);
        } else if (angle == 90) { // 向下
            return new Point2D(0, 1);
        } else if (angle == 180) { // 向左
            return new Point2D(-1, 0);
        } else { // 向上
            return new Point2D(0, -1);
        }
    }

}
