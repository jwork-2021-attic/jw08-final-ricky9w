package com.ricky;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.text.TextLevelLoader;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.input.virtual.VirtualButton;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.ui.UI;
import com.ricky.collision.BulletBrickHandler;
import com.ricky.collision.BulletEnemyHandler;
import com.ricky.components.TankComponent;
import com.ricky.ui.TankWarUIController;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Map;

import javax.swing.plaf.basic.BasicTreeUI.SelectionModelPropertyChangeHandler;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.ricky.Config.*;
import static com.ricky.TankWarType.*;


public class App extends GameApplication {


    // 初始化游戏窗口设置
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(MAP_SIZE * BLOCK_SIZE + UI_SIZE);
        settings.setHeight(MAP_SIZE * BLOCK_SIZE);
        settings.setTitle("Title");
        settings.setVersion("1.0");
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
    }

    private TankComponent playerTank;

    // 初始化输入处理
    // WASD和方向键控制移动, J控制开火
    @Override
    protected void initInput() {
        Input input = getInput();

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                playerTank.left();
            }
        }, KeyCode.A, VirtualButton.LEFT);

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                playerTank.right();
            }
        }, KeyCode.D, VirtualButton.RIGHT);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                playerTank.up();
            }
        }, KeyCode.W, VirtualButton.UP);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                playerTank.down();
            }
        }, KeyCode.S, VirtualButton.DOWN);

        input.addAction(new UserAction("Shoot") {
            @Override
            protected void onAction() {
                playerTank.shoot();
            }
        }, KeyCode.J);
    }

    // TODO: 初始化游戏数据相关变量
    protected void initGameVars(Map<String, Object> vars) {
        
    }

    // 初始化游戏设置
    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.LIGHTGRAY);

        // FIXME: 直接用 addEntity()方法添加实体, 不用Factory
        getGameWorld().addEntityFactory(new TankWarFactory());

        // FIXME: 不行就用原来坦克大战的地图, 可以用地图2
        setLevelFromMap("world0.tmx");

        playerTank = getGameWorld().getSingleton(PLAYER).getComponent(TankComponent.class);
        
    }

    // 初始化物理引擎设置
    @Override
    protected void initPhysics() {
        var bulletEnemyHandler = new BulletEnemyHandler();

        getPhysicsWorld().addCollisionHandler(bulletEnemyHandler);
        getPhysicsWorld().addCollisionHandler(bulletEnemyHandler.copyFor(BULLET, PLAYER));

        getPhysicsWorld().addCollisionHandler(new BulletBrickHandler());
    }

    // 侧边显示部分初始化
    // UI 控制逻辑定义在 TankWarUIController
    // UI 布局定义在 resources/assets/ui/tankwar_ui.fxml
    @Override
    protected void initUI() {
        UI ui = getAssetLoader().loadUI("tankwar_ui.fxml", new TankWarUIController());
        ui.getRoot().setTranslateX(MAP_SIZE * BLOCK_SIZE);

        getGameScene().addUI(ui);
    }

    @Override
    protected void onUpdate(double tpf) {
        
    }

    // TODO: 当前客户端中的玩家死亡处理
    private boolean playerKilled = false;


    // TODO: 添加一组游戏状态变量
    // 1. 四个玩家当前占领的领地情况
    // 2. 四个玩家的血量

    public static void main(String[] args) {
        System.out.println("Hello World");
        launch(args);
    }

}