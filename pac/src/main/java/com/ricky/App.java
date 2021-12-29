package com.ricky;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.FXGLDefaultMenu.MenuContent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.IDComponent;
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
import com.almasb.fxgl.core.collection.Array;
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

import java.net.Socket;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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
        return getGameWorld().getEntityByID("player", CLIENT_ID).get();
    }

    private PlayerComponent getPlayerComponent() {
        var player = getGameWorld().getEntityByID("player", CLIENT_ID);
        
        if (player.isEmpty())
            return null;
            
        return player.get().getComponent(PlayerComponent.class);
    }

    @Override
    protected void initInput() {
        // 上下左右移动处理
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                if (getPlayerComponent() != null)
                    getPlayerComponent().up();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                if (getPlayerComponent() != null)
                    getPlayerComponent().down();
            }
        }, KeyCode.S);

        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                if (getPlayerComponent() != null)
                    getPlayerComponent().left();
            }
        }, KeyCode.A);

        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                if (getPlayerComponent() != null)
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

                // 保存所有玩家坐标
                for (int i = 1; i <= 4; i++) {
                    var player = getGameWorld().getEntityByID("player", i);

                    if (!player.isEmpty()) {
                        pos.players.put(i, new Pair(player.get().getX(), player.get().getY()));
                    }
                }

                // 保存所有玩家得分和状态
                bundle.put("scores", scores);
                bundle.put("alives", alives);

                // 保存其他信息
                bundle.put("time", geti("time"));

                bundle.put("pos", pos);
                
                data.putBundle(bundle);

            }

            @Override
            public void onLoad(DataFile data) {
                var bundle = data.getBundle("gameData");

                // 从 bundle 中取出数据
                PosData pos = bundle.get("pos");
                
                // 恢复所有entity
                for (var coinPos : pos.coins) {
                    getGameWorld().spawn("0", new Point2D(coinPos.getKey(), coinPos.getValue()));
                }
                
                for (var ePos : pos.enemies) {
                    getGameWorld().spawn("E", ePos.getKey(), ePos.getValue());
                }

                for (var pPos : pos.players.entrySet()) {
                    SpawnData pData = new SpawnData(new Point2D(pPos.getValue().getKey(), pPos.getValue().getValue()));
                    pData.put("name", "player");
                    pData.put("id", pPos.getKey());
                    getGameWorld().spawn("P", pData);
                }

                // 恢复其他变量
                set("time", bundle.get("time"));
                set("coins", pos.coins.size());
                
                scores = ((Integer[])bundle.get("scores")).clone();
                alives = ((Boolean[])bundle.get("alives")).clone();

            }
        });
    }

    private Integer[] scores = new Integer[4];
    private Boolean[] alives = new Boolean[4];


    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("coins", 0);
        // 初始化时间信息
        vars.put("time", TIME_PER_LEVEL);
        // 初始化玩家信息
        for (int i = 0; i < 4; i++) {
            scores[i] = 0;
            alives[i] = true;   
        }
    }


    // 判断是否是重新加载游戏
    boolean isReload = false;

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new PacFactory());

        if (!isReload) { // 重新生成游戏

            // 初始地图
            Level origin = getAssetLoader().loadLevel("origin.txt", new TextLevelLoader(40, 40, ' '));
            getGameWorld().setLevel(origin);

            // 生成四个玩家
            for (int i = 1; i <= 4; i++) {
                SpawnData pData = new SpawnData(SPAWN_PLAYERS[i - 1]);
                pData.put("name", "player");
                pData.put("id", i);
                getGameWorld().spawn("P", pData);
            }

            // 生成四个敌人
            for (int i = 1; i <= 4; i++) {
                SpawnData pData = new SpawnData(SPAWN_ENEMIES[i - 1]);
                getGameWorld().spawn("E", pData);
            }

        } else { // 加载游戏, 仅加载地图模块, 其他实体在 onLoad() 方法中加载
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
                gameOver();
            }
        });

    }

    // 初始化物理引擎
    @Override
    protected void initPhysics() {
        onCollision(PLAYER, ENEMY, (p, e) -> {
            var id = p.getComponent(IDComponent.class).getId();
            p.removeFromWorld();
            alives[id - 1] = false;
        });

        onCollision(PLAYER, COIN, (p, c) -> {
            var id = p.getComponent(IDComponent.class).getId();
            c.removeFromWorld();
            onCoinPickup(id);
        });

        // 玩家之间碰撞
        onCollision(PLAYER, PLAYER, (p1, p2) -> {
            var id1 = p1.getComponent(IDComponent.class).getId();
            var id2 = p2.getComponent(IDComponent.class).getId();
            var s1 = scores[id1 - 1];
            var s2 = scores[id2 - 1];
            if (s1 < s2) {
                p1.removeFromWorld();
                alives[id1 - 1] = false;
            } else if (s1 > s2) {
                p2.removeFromWorld();
                alives[id2 - 1] = false;
            }
        });

    }

    @Override
    protected void initUI() {
        UI ui = getAssetLoader().loadUI("pac_ui.fxml", new PacUIController());
        ui.getRoot().setTranslateX(MAP_SIZE * BLOCK_SIZE);

        getGameScene().addUI(ui);
    }

    boolean gameOver = false;

    @Override
    protected void onUpdate(double tpf) {
        // 检查当前存活的玩家数量
        var totalAlives = 0;
        for (int i = 0; i < 4; i++) {
            if (alives[i])
                totalAlives++;
        }
        // 玩家仅剩1位或全部死亡则游戏结束
        if (totalAlives <= 1)
            gameOver();
        
        // TODO: 添加网络同步逻辑
        
    }

    // 游戏结束的几种情况：
    // 1. 所有硬币都被吃完
    // 2. 只有一个玩家存活
    // 3. 时间耗尽
    private void gameOver() {
        // TODO: 积分完善积分方法
        // 检查存活玩家
        
        // 找出最大分数
        int winner = 0, maxScore = 0;
        for (int i = 0; i < 4; i++)
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                winner = i + 1;
            }
        getDialogService().showMessageBox(String.format("Player %d wins! Press OK to exit.", winner), getGameController()::exit);
    }

    public void onCoinPickup(int id) {
        inc("coins", -1);

        scores[id - 1] += 50;
        
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
