package com.ricky;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.FXGLDefaultMenu.MenuContent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.text.TextLevelLoader;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.net.BundleTCPMessageReader;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.ui.UI;
import com.ricky.components.PlayerComponent;
import com.ricky.ui.PacUIController;
import com.ricky.utils.PosData;
import com.almasb.fxgl.core.serialization.Bundle;
import java.util.EnumSet;

import javafx.geometry.Point2D;
import javafx.print.PrintColor;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Menu;
import javafx.scene.effect.ColorInput;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Pair;
import kotlin.ExtensionFunctionType;
import kotlin.contracts.Returns;

import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.Config.*;
import static com.ricky.PacType.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.sql.ConnectionPoolDataSource;


public class App extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setMainMenuEnabled(true);
        settings.setEnabledMenuItems(EnumSet.allOf(MenuItem.class));
        // 显示保存和加载选项
        // settings.setEnabledMenuItems(EnumSet.of(MenuItem.SAVE_LOAD));
        

        settings.setWidth(MAP_SIZE * BLOCK_SIZE + UI_SIZE * 2);
        settings.setHeight(MAP_SIZE * BLOCK_SIZE + 60);
        settings.setTitle("M-Pac-Man");
        settings.setVersion("1.0");
        settings.setManualResizeEnabled(false);
    }

    

    private Entity getPlayer() {
        return getGameWorld().getSingleton(PLAYER);
    }

    private PlayerComponent getPlayerComponent() {
        return getPlayer().getComponent(PlayerComponent.class);
    }

    @Override
    protected void initInput() {
        // 上下左右移动处理
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                getPlayerComponent().up();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                getPlayerComponent().down();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                getPlayerComponent().left();
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                getPlayerComponent().right();
            }
        }, KeyCode.D);

        // 保存和加载游戏
        getInput().addAction(new UserAction("Save") {
            @Override
            protected void onAction() {
                getSaveLoadService().saveAndWriteTask("save.sav").run();
            }
        }, KeyCode.U);

        getInput().addAction(new UserAction("Load") {
            @Override
            protected void onAction() {
                getSaveLoadService().readAndLoadTask("save.sav").run();
            }
        }, KeyCode.L);
    }

    @Override
    protected void onPreInit() {
        getSaveLoadService().addHandler(new SaveLoadHandler() {
            @Override
            public void onSave(DataFile data) {

                var bundle = new Bundle("gameData");

                PosData pos = new PosData();

                // 保存所有硬币坐标
                var coins = getGameWorld().getEntitiesByType(COIN);
                for (var coin : coins) {
                    pos.coins.add(new Pair(coin.getX(), coin.getY()));
                }

                // 保存所有怪物坐标
                var enemies = getGameWorld().getEntitiesByType(ENEMY);
                for (var enemy : enemies) {
                    pos.enemies.add(new Pair(enemy.getX(), enemy.getY()));
                }

                /* 
                // FIXME: 保存所有玩家坐标
                save.player1 = getGameWorld().getSingleton(PLAYER1).getPosition();
                save.player2 = getGameWorld().getSingleton(PLAYER2).getPosition();
                save.player3 = getGameWorld().getSingleton(PLAYER3).getPosition();
                save.player4 = getGameWorld().getSingleton(PLAYER4).getPosition();  */

                // 保存所有玩家得分
                bundle.put("score1", geti("score1"));
                bundle.put("score2", geti("score2"));
                bundle.put("score3", geti("score3"));
                bundle.put("score4", geti("score4"));

                // 保存其他信息
                bundle.put("time", geti("time"));

                bundle.put("pos", pos);
                
                data.putBundle(bundle);

                System.out.println(coins.size());
            }

            @Override
            public void onLoad(DataFile data) {
                var bundle = data.getBundle("gameData");

                // 从 bundle 中取出数据
                PosData pos = bundle.get("pos");
                
                for (var coinPos : pos.coins) {
                    getGameWorld().spawn("0", new Point2D(coinPos.getKey(), coinPos.getValue()));
                }
                
                for (var ePos : pos.enemies) {
                    getGameWorld().spawn("E", ePos.getKey(), ePos.getValue());
                }

                getGameWorld().spawn("P", SPAWN_P1);

                int time = bundle.get("time");

                System.out.println(pos.coins.size());
                
            }
        });
    }

    

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("coins", 0);
        vars.put("time", TIME_PER_LEVEL);
        // 四个玩家的得分
        vars.put("score1", 0);
        vars.put("score2", 0);
        vars.put("score3", 0);
        vars.put("score4", 0);
    }


    // 判断是否是重新加载游戏
    boolean isReload = false;
    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new PacFactory());

        if (!isReload) { // 重新生成游戏
            Level origin = getAssetLoader().loadLevel("origin.txt", new TextLevelLoader(40, 40, ' '));
            getGameWorld().setLevel(origin);

            getGameWorld().spawn("P", SPAWN_P1);
            getGameWorld().spawn("E", SPAWN_E1);
            getGameWorld().spawn("E", SPAWN_E2);
            getGameWorld().spawn("E", SPAWN_E3);
            getGameWorld().spawn("E", SPAWN_E4);
        } else { // 加载游戏
            Level reload = getAssetLoader().loadLevel("reload.txt", new TextLevelLoader(40, 40, ' '));
            getGameWorld().setLevel(reload);
        }
        isReload = true;


        AStarGrid grid = AStarGrid.fromWorld(getGameWorld(), MAP_SIZE, MAP_SIZE, BLOCK_SIZE, BLOCK_SIZE, (type) -> {
            if (type == BLOCK)
                return CellState.NOT_WALKABLE;
            return CellState.WALKABLE;
        });

        set("grid", grid);

        // coin设置为所有硬币总数
        set("coins", getGameWorld().getEntitiesByType(COIN).size());

        // 计时器
        run(() -> inc("time", -1), Duration.seconds(1));

        
        getWorldProperties().<Integer>addListener("time", (old, now) -> {
            if (now == 0) {
                onTimeOut();
            }
        });

    }

    // TODO: 初始化物理引擎
    @Override
    protected void initPhysics() {
        onCollision(PLAYER, ENEMY, (p, e) -> gameOver());

        onCollisionCollectible(PLAYER, COIN, c -> gameOver());
    }

    @Override
    protected void initUI() {
        UI ui = getAssetLoader().loadUI("pac_ui.fxml", new PacUIController());
        ui.getRoot().setTranslateX(MAP_SIZE * BLOCK_SIZE);

        getGameScene().addUI(ui);
    }

    boolean requestNewGame = false;

    @Override
    protected void onUpdate(double tpf) {
        // TODO: 添加网络同步逻辑
        if (requestNewGame) {
            requestNewGame = false;
            getGameController().startNewGame();
        }
    }

    // TODO: 时间为0时结束游戏并计算胜利玩家
    private void onTimeOut() {
        
    }

    // TODO: 显示游戏统计结果
    private void gameOver() {

    }

    public void onCoinPickup() {
        inc("coins", -1);
        inc("socre", +50);

        if (geti("coins") == 0) {
            gameOver();
        }
    }


    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        launch(args);
    }
}
