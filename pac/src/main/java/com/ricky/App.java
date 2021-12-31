package com.ricky;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.app.scene.FXGLDefaultMenu.MenuContent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.IDComponent;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.text.TextLevelLoader;
import com.almasb.fxgl.event.EventBus;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.net.BundleTCPMessageReader;
import com.almasb.fxgl.net.Client;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.ui.UI;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.StringIdGenerator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValue;
import com.fasterxml.jackson.databind.ser.std.InetAddressSerializer;
import com.fasterxml.jackson.databind.util.EnumValues;
import com.ricky.components.PlayerComponent;
import com.ricky.components.ScoreComponent;
import com.ricky.ui.PacUIController;
import com.ricky.utils.PosData;
import com.almasb.fxgl.core.collection.Array;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.KeyInputBuilder;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.multiplayer.ReplicationEvent;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.net.Server;
import java.util.EnumSet;
import java.util.HashMap;

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
import java.security.KeyStore.Entry;
import java.sql.ClientInfoStatus;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import javax.lang.model.util.ElementScanner6;
import javax.security.auth.login.FailedLoginException;
import javax.swing.plaf.metal.MetalBorders.PaletteBorder;
import javax.xml.datatype.DatatypeFactory;

public class App extends GameApplication{

    @Override
    protected void initUI() {
        UI ui = getAssetLoader().loadUI("pac_ui.fxml", new PacUIController());
        ui.getRoot().setTranslateX(MAP_SIZE * BLOCK_SIZE);

        getGameScene().addUI(ui);
    }
    
    @Override
    protected void initSettings(GameSettings settings) {
        // settings.setMainMenuEnabled(true);
        // settings.setEnabledMenuItems(EnumSet.allOf(MenuItem.class));

        settings.addEngineService(MultiplayerService.class);


        settings.setWidth(MAP_SIZE * BLOCK_SIZE + UI_SIZE * 2);
        settings.setHeight(MAP_SIZE * BLOCK_SIZE + 60);
        settings.setTitle("M-Pac-Man");
        settings.setVersion("1.0");
        settings.setManualResizeEnabled(false);
    }

    private Entity player1, player2, player3, player4;
    
    private Server<Bundle> server;
    
    private Input cInput2 = new Input(), cInput3 = new Input(), cInput4 = new Input();

    private Map<Integer, Pair<Entity, Input>> ctrlMap = new HashMap<>();

    @Override
    protected void initInput() {
        
        ctrlMap.put(1, new Pair<Entity, Input>(player2, cInput2));
        ctrlMap.put(2, new Pair<Entity, Input>(player3, cInput3));
        ctrlMap.put(3, new Pair<Entity, Input>(player4, cInput4));

        // 初始化本地输入处理
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                player1.getComponent(PlayerComponent.class).up();
            }
        }, KeyCode.W);
        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                player1.getComponent(PlayerComponent.class).down();
            }
        }, KeyCode.S);
        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                player1.getComponent(PlayerComponent.class).left();
            }
        }, KeyCode.A);
        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                player1.getComponent(PlayerComponent.class).right();
            }
        }, KeyCode.D);

        // 初始化客户端输入处理
        for (Map.Entry<Integer, Pair<Entity, Input>> entry : ctrlMap.entrySet()) {
            
            onKeyBuilder(entry.getValue().getValue(), KeyCode.W)
                    .onAction(() -> { 
                        try {
                            entry.getValue().getKey().getComponent(PlayerComponent.class).up();
                            System.out.println(entry.getValue().getKey().getComponent(PlayerComponent.class));
                        } catch (NullPointerException e) {}
                        // TODO: delete debug output
                        System.out.println("client key pressed");
                    });
            onKeyBuilder(entry.getValue().getValue(), KeyCode.S)
                    .onAction(() -> {
                        try {
                            entry.getValue().getKey().getComponent(PlayerComponent.class).down();
                        } catch (NullPointerException e) {}
                     });
            onKeyBuilder(entry.getValue().getValue(), KeyCode.A)
                    .onAction(() -> { 
                        try {
                            entry.getValue().getKey().getComponent(PlayerComponent.class).left();
                        } catch (NullPointerException e) {}
                     });

            onKeyBuilder(entry.getValue().getValue(), KeyCode.D)
                    .onAction(() -> { 
                        try {
                            entry.getValue().getKey().getComponent(PlayerComponent.class).right();
                        } catch (NullPointerException e) {}
                     });
        }
        
    }

    @Override
    protected void onPreInit() {
        getSaveLoadService().addHandler(new SaveLoadHandler() {
            @Override
            public void onSave(DataFile data) {

                var bundle = new Bundle("gameData");
                PosData pos = new PosData();

                var coins = getGameWorld().getEntitiesByType(COIN);
                for (var coin : coins)
                    pos.coins.add(new Pair(coin.getX(), coin.getY()));

                var enemies = getGameWorld().getEntitiesByType(ENEMY);
                for (var enemy : enemies)
                    pos.coins.add(new Pair(enemy.getX(), enemy.getY()));

                pos.player1 = new Pair(player1.getX(), player1.getY());
                pos.player2 = new Pair(player1.getX(), player2.getY());
                pos.player3 = new Pair(player1.getX(), player3.getY());
                pos.player4 = new Pair(player1.getX(), player4.getY());

                for (int i = 1; i <= 4; i++)
                    bundle.put(String.format("score%d", i), geti(String.format("score%d", i)));

                bundle.put("time", geti("time"));

                bundle.put("pos", pos);

                data.putBundle(bundle);
            }

            @Override
            public void onLoad(DataFile data) {
                if (IS_SERVER) {

                    var bundle = data.getBundle("gameData");
                    PosData pos = bundle.get("pos");
                    // 服务器端通过网络生成entity
                    for (var coinPos : pos.coins) {
                        var coin = spawn("0", new Point2D(coinPos.getKey(), coinPos.getValue()));
                        spawnOnAllConnections(coin, "0");
                    }
                    
                    for (var ePos : pos.enemies) {
                        var enemy = spawn("E", new Point2D(ePos.getKey(), ePos.getValue()));
                        spawnOnAllConnections(enemy, "E");
                    }

                    player1 = spawn("P", new Point2D(pos.player1.getKey(), pos.player1.getValue()));
                    player2 = spawn("P", new Point2D(pos.player2.getKey(), pos.player2.getValue()));
                    player3 = spawn("P", new Point2D(pos.player3.getKey(), pos.player3.getValue()));
                    player4 = spawn("P", new Point2D(pos.player4.getKey(), pos.player4.getValue()));

                    spawnOnAllConnections(player1, "P");
                    spawnOnAllConnections(player2, "P");
                    spawnOnAllConnections(player3, "P");
                    spawnOnAllConnections(player4, "P");

                    set("time", bundle.get("time"));
                    for (int i = 1; i < 4; i++) 
                        set(String.format("score%d", i), bundle.get(String.format("score%d", i)));
                }
            }
        });
    }

    private void spawnOnAllConnections(Entity e, String name) {
        for (var conn : server.getConnections())
            getMPService().spawn(conn, e, name);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("time", TIME_PER_LEVEL);
        for (int i = 1; i <= 4; i++)
            vars.put(String.format("score%d", i), 0);
    }

    boolean isReload = false;

    @Override
    protected void initGame() {

        AStarGrid grid = AStarGrid.fromWorld(getGameWorld(), MAP_SIZE, MAP_SIZE, BLOCK_SIZE, BLOCK_SIZE, (type) -> {
            if (type == BLOCK)
                return CellState.NOT_WALKABLE;
            return CellState.WALKABLE;
        });

        set("grid", grid);

        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new PacFactory());

        if (IS_SERVER) {
            server = getNetService().newTCPServer(55555);
            server.setOnConnected(conn -> {

                getExecutor().startAsyncFX(() -> {
                    // FIXME: 添加检查连接数是否满4逻辑
                    GameWorld toolWorld = new GameWorld();
                    Level orign = getAssetLoader().loadLevel("origin.txt", new TextLevelLoader(40, 40, ' '));
                    toolWorld.setLevel(orign);

                    // 生成砖块
                    for (var block : toolWorld.getEntitiesByType(BLOCK)) {
                        var sBlock = spawn("1", new Point2D(block.getX(), block.getY()));
                        getMPService().spawn(conn, sBlock, "1");
                    }
                    // 生成硬币
                    for (var coin : toolWorld.getEntitiesByType(COIN)) {
                        var sCoin = spawn("0", coin.getX(), coin.getY());
                        getMPService().spawn(conn, sCoin, "0");
                    }
                    // 生成玩家
                    player1 = spawn("P", SPAWN_PLAYERS[0]);
                    getMPService().spawn(conn, player1, "P");
                    player2 = spawn("P", SPAWN_PLAYERS[1]);
                    getMPService().spawn(conn, player2, "P");
                    player3 = spawn("P", SPAWN_PLAYERS[2]);
                    getMPService().spawn(conn, player3, "P");
                    player4 = spawn("P", SPAWN_PLAYERS[3]);
                    getMPService().spawn(conn, player4, "P");
                    // 生成怪物
                    for (int i = 0; i < 4; i++) {
                        var enemy = spawn("E", SPAWN_ENEMIES[i]);
                        getMPService().spawn(conn, enemy, "E");
                    }

                    // 绑定客户端输入
                    var connNum = conn.getConnectionNum();
                    getMPService().addInputReplicationReceiver(conn, ctrlMap.get(connNum).getValue());

                    // 设置property同步
                    getMPService().addPropertyReplicationSender(conn, getWorldProperties());
                });
                

                
            });

            server.startAsync();

            run(() -> inc("time", -1), Duration.seconds(1));
        } else {

            var client = getNetService().newTCPClient("localhost", 55555);
            client.setOnConnected(conn -> {

                getMPService().addEntityReplicationReceiver(conn, getGameWorld());

                getMPService().addInputReplicationSender(conn, getInput());

                getMPService().addPropertyReplicationReceiver(conn, getWorldProperties());
            });

            client.connectAsync();

            getInput().setProcessInput(false);
        }

        isReload = true;

        getWorldProperties().<Integer>addListener("time", (old, now) -> {
            if (now == 0) {
                gameOver();
            }
        });
    }

    @Override
    protected void initPhysics() {
        if (IS_SERVER) {
            // FIXME: 玩家和怪物碰撞
            /* onCollision(PLAYER, ENEMY, (p, e) -> {
                p.removeFromWorld();
            }); */

            // 玩家捡起硬币
            onCollision(PLAYER, COIN, (p, c) -> {
                p.getComponent(ScoreComponent.class).inc(50);
                c.removeFromWorld();
            });

            // 玩家之间碰撞
            onCollision(PLAYER, PLAYER, (p1, p2) -> {
                int s1 = p1.getComponent(ScoreComponent.class).getScore(), s2 = p2.getComponent(ScoreComponent.class).getScore();
                if (s1 < s2)
                    p1.removeFromWorld();
                else if (s1 > s2)
                    p2.removeFromWorld();
            });
        }
    }

    @Override
    protected void onUpdate(double tpf) {
        // 更新游戏全局存储的得分
        if (player1 != null) {
            try {
                set("score1", player1.getComponent(ScoreComponent.class).getScore());
                set("score2", player2.getComponent(ScoreComponent.class).getScore());
                set("score3", player3.getComponent(ScoreComponent.class).getScore());
                set("score4", player4.getComponent(ScoreComponent.class).getScore());
            } catch (Exception e) {
                // System.out.println(e);
            }
            // 同步客户端输入
            for (Map.Entry<Integer, Pair<Entity, Input>> entry : ctrlMap.entrySet())
                entry.getValue().getValue().update(tpf);

            // 判断是否游戏结束
            if (getGameWorld().getEntitiesByType(PLAYER).size() <= 1 ||
                getGameWorld().getEntitiesByType(COIN).size() == 0)
                gameOver();
        }
    }

    private void gameOver() {
        // TODO: gameover
        /* int winner = 0, maxScore = 0;
        for (int i = 1; i <= 4; i++) {
            int score = geti(String.format("score%d", i));
            if (score >= maxScore) {
                winner = i;
                maxScore = score;
            }
        }
        getDialogService().showMessageBox(String.format("Player %d wins! Press OK to exit.", winner), getGameController()::exit); */
    }

    private MultiplayerService getMPService() {
        return getService(MultiplayerService.class);
    }

    public static void main(String args[]) {
        if (args.length == 1) {
            IS_SERVER = true;
        }
        else {
            IS_SERVER = false;
        }
        launch(args);
    }

}
