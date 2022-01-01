package com.ricky;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.IDComponent;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.text.TextLevelLoader;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.ui.UI;
import com.ricky.components.Direction;
import com.ricky.components.PlayerComponent;
import com.ricky.components.ScoreComponent;
import com.ricky.components.ai.DelayChaseComponent;
import com.ricky.network.NetMessage;
import com.ricky.ui.PacUIController;
import com.ricky.utils.PosData;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.dsl.components.RandomAStarMoveComponent;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Server;

import java.lang.invoke.VarHandle;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.events.EndElement;

import java.util.HashSet;
import java.math.*;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Pair;

import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.Config.*;
import static com.ricky.PacType.*;
import static com.ricky.network.ActionType.*;
import com.ricky.network.ActionData;
import java.util.Map;

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

    // private Map<Integer, Pair<Entity, Input>> ctrlMap = new HashMap<>();

    private PlayerComponent getPlayerComponent(Entity player) {
        if (player.hasComponent(PlayerComponent.class))
            return player.getComponent(PlayerComponent.class);
        return null;
    }

    private void moveTowards(Entity player, Direction dir) {
        PlayerComponent pc = getPlayerComponent(player);
        if (pc != null) {
            switch (dir) {
                case UP:
                    pc.up();
                    break;
                case DOWN:
                    pc.down();
                    break;
                case LEFT:
                    pc.left();
                    break;
                case RIGHT:
                    pc.right();
                    break;
            }
        }
    }

    @Override
    protected void initInput() {

        // 初始化本地输入处理
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                try {
                    player1.getComponent(PlayerComponent.class).up();
                } catch (Exception e) {}
            }
        }, KeyCode.W);
        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                try {
                    player1.getComponent(PlayerComponent.class).down();
                } catch (Exception e) {}
            }
        }, KeyCode.S);
        getInput().addAction(new UserAction("Left") {
            @Override
            protected void onAction() {
                try {
                    player1.getComponent(PlayerComponent.class).left();
                } catch (Exception e) {}
            }
        }, KeyCode.A);
        getInput().addAction(new UserAction("Right") {
            @Override
            protected void onAction() {
                try {
                    player1.getComponent(PlayerComponent.class).right();
                } catch (Exception e) {}
            }
        }, KeyCode.D);
        
        // 初始化客户端输入处理
        onKeyBuilder(cInput2, KeyCode.W).onAction(() -> { moveTowards(player2, Direction.UP); });
        onKeyBuilder(cInput2, KeyCode.S).onAction(() -> { moveTowards(player2, Direction.DOWN); });
        onKeyBuilder(cInput2, KeyCode.A).onAction(() -> { moveTowards(player2, Direction.LEFT); });
        onKeyBuilder(cInput2, KeyCode.D).onAction(() -> { moveTowards(player2, Direction.RIGHT); });
        
        onKeyBuilder(cInput3, KeyCode.W).onAction(() -> { moveTowards(player3, Direction.UP); });
        onKeyBuilder(cInput3, KeyCode.S).onAction(() -> { moveTowards(player3, Direction.DOWN); });
        onKeyBuilder(cInput3, KeyCode.A).onAction(() -> { moveTowards(player3, Direction.LEFT); });
        onKeyBuilder(cInput3, KeyCode.D).onAction(() -> { moveTowards(player3, Direction.RIGHT); });

        onKeyBuilder(cInput4, KeyCode.W).onAction(() -> { moveTowards(player4, Direction.UP); });
        onKeyBuilder(cInput4, KeyCode.S).onAction(() -> { moveTowards(player4, Direction.DOWN); });
        onKeyBuilder(cInput4, KeyCode.A).onAction(() -> { moveTowards(player4, Direction.LEFT); });
        onKeyBuilder(cInput4, KeyCode.D).onAction(() -> { moveTowards(player4, Direction.RIGHT); });
        
    }

    @Override
    protected void onPreInit() {
        // TODO: 加入对UUID的处理
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

        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new PacFactory());

        Level basic = getAssetLoader().loadLevel("small_r.txt", new TextLevelLoader(40, 40, ' '));
        getGameWorld().setLevel(basic);

        AStarGrid grid = AStarGrid.fromWorld(getGameWorld(), MAP_SIZE, MAP_SIZE, BLOCK_SIZE, BLOCK_SIZE, (type) -> {
            if (type == BLOCK)
                return CellState.NOT_WALKABLE;
            return CellState.WALKABLE;
        });

        set("grid", grid);

        if (IS_SERVER) {
            server = getNetService().newTCPServer(55555);

            // TODO: 服务器端生成所有可移动物体并加上UUID
            GameWorld toolWorld = new GameWorld();
            Level orign = getAssetLoader().loadLevel("small.txt", new TextLevelLoader(40, 40, ' '));
            toolWorld.setLevel(orign);

            // 生成硬币
            for (var coin : toolWorld.getEntitiesByType(COIN))
                getGameWorld().spawn("0", coin.getX(), coin.getY());
            // 生成玩家
            player1 = getGameWorld().spawn("P", SPAWN_PLAYERS[0]);
            player2 = getGameWorld().spawn("P", SPAWN_PLAYERS[1]);
            player3 = getGameWorld().spawn("P", SPAWN_PLAYERS[2]);
            player4 = getGameWorld().spawn("P", SPAWN_PLAYERS[3]);
            // 生成敌人
            for (int i = 0; i < 4; i++)
                getGameWorld().spawn("E", SPAWN_ENEMIES[i]);


            server.setOnConnected(conn -> {

                getExecutor().startAsyncFX(() -> {
                    // FIXME: 添加检查连接数是否满4逻辑
                    // 向客户端发送消息生成所有可移动物体
                    Bundle bundle = new Bundle("updateData");

                    NetMessage msg = new NetMessage();
                    for (var coin : getGameWorld().getEntitiesByType(COIN)) {
                        msg.actItems.put(getUUID(coin), new ActionData(ADD, COIN, coin.getX(), coin.getY()));
                    }

                    msg.actItems.put(getUUID(player1), new ActionData(ADD, PLAYER, player1.getX(), player1.getY()));
                    msg.actItems.put(getUUID(player2), new ActionData(ADD, PLAYER, player2.getX(), player2.getY()));
                    msg.actItems.put(getUUID(player3), new ActionData(ADD, PLAYER, player3.getX(), player3.getY()));
                    msg.actItems.put(getUUID(player4), new ActionData(ADD, PLAYER, player4.getX(), player4.getY()));

                    for (var enemy : getGameWorld().getEntitiesByType(ENEMY)) {
                        msg.actItems.put(getUUID(enemy), new ActionData(ADD, ENEMY, enemy.getX(), enemy.getY()));
                    }
                    
                    bundle.put("items", msg);

                    conn.send(bundle);

                    // 绑定客户端输入
                    switch (conn.getConnectionNum()) {
                        case 1:
                            getMPService().addInputReplicationReceiver(conn, cInput2);
                            break;
                        case 2:
                            getMPService().addInputReplicationReceiver(conn, cInput3);
                            break;
                        case 3:
                            getMPService().addInputReplicationReceiver(conn, cInput4);
                            break;
                    }

                    // 设置property同步
                    getMPService().addPropertyReplicationSender(conn, getWorldProperties());
                });
                
            });

            server.startAsync();

            run(() -> inc("time", -1), Duration.seconds(1));
        } else {

            var client = getNetService().newTCPClient("localhost", 55555);
            client.setOnConnected(conn -> {

                conn.addMessageHandlerFX((connection, message) -> {
                    NetMessage msg = message.get("items");
                    handleUpdateMessage(msg);
                });

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

    private void handleUpdateMessage(NetMessage msg) {
        if (msg == null)
            return;
        for (var action : msg.actItems.entrySet()) {
            switch (action.getValue().actionType) {
                case ADD:
                    switch (action.getValue().entityType) {
                        case COIN:
                            SpawnData cData = new SpawnData(action.getValue().x, action.getValue().y);
                            cData.put("uuid", action.getKey());
                            getGameWorld().spawn("0", cData);
                            break;
                        case PLAYER:
                            SpawnData pData = new SpawnData(action.getValue().x, action.getValue().y);
                            pData.put("uuid", action.getKey());
                            getGameWorld().spawn("PC", pData);
                            break;
                        case ENEMY:
                            // FIXME: 可能需要去掉移动组件
                            SpawnData eData = new SpawnData(action.getValue().x, action.getValue().y);
                            eData.put("uuid", action.getKey());
                            getGameWorld().spawn("EC", eData);
                            break;
                        default:
                            break;
                    }
                    break;
                case MOVE:
                    /* if (!getGameWorld().getEntityByID(action.getKey(), 0).isEmpty()) {
                        Entity e = getGameWorld().getEntityByID(action.getKey(), 0).get();
                        e.removeFromWorld();
                        switch (action.getValue().entityType) {
                            case COIN:
                                getGameWorld().spawn("0", action.getValue().x, action.getValue().y);
                                break;
                            case PLAYER:
                                getGameWorld().spawn("P", action.getValue().x, action.getValue().y);
                                break;
                            case ENEMY:
                                // FIXME: 可能需要去掉移动组件
                                var e1 = getGameWorld().spawn("E", action.getValue().x, action.getValue().y);
                                e1.removeComponent(AStarMoveComponent.class);
                                e1.removeComponent(DelayChaseComponent.class);
                                e1.removeComponent(RandomAStarMoveComponent.class);
                                break;
                            default:
                                break;
                        }
                    } */
                    // int cellX = (int)Math.round(action.getValue().x / BLOCK_SIZE), cellY = (int)Math.round(action.getValue().x / BLOCK_SIZE);
                    double x = action.getValue().x, y = action.getValue().y;
                    if (!getGameWorld().getEntityByID(action.getKey(), 0).isEmpty()) {
                        Entity e = getGameWorld().getEntityByID(action.getKey(), 0).get();
                        e.setPosition(x, y);
                    }
                    break;
                case DEL:
                    if (!getGameWorld().getEntityByID(action.getKey(), 0).isEmpty()){
                        Entity e = getGameWorld().getEntityByID(action.getKey(), 0).get();
                        e.removeFromWorld();
                    }
                    break;
            }
        }
    }

    private String getUUID(Entity e) {
        if (e == null) {
            System.out.println("entity is null");
            return "";
        }
        if (e.hasComponent(IDComponent.class))
            return e.getComponent(IDComponent.class).getName();
        return "";
    }
    
    private HashSet<String> toRemove = new HashSet<>();

    @Override
    protected void initPhysics() {
        // TODO: 维护需要remove的实体集合
        if (IS_SERVER) {
            // FIXME: 玩家和怪物碰撞
            onCollision(PLAYER, ENEMY, (p, e) -> {
                toRemove.add(getUUID(p));
                // p.removeFromWorld();
            });

            // 玩家捡起硬币
            onCollision(PLAYER, COIN, (p, c) -> {
                p.getComponent(ScoreComponent.class).inc(50);
                toRemove.add(getUUID(c));
            });

            // 玩家之间碰撞
            onCollision(PLAYER, PLAYER, (p1, p2) -> {
                int s1 = p1.getComponent(ScoreComponent.class).getScore(), s2 = p2.getComponent(ScoreComponent.class).getScore();
                if (s1 < s2) {
                    toRemove.add(getUUID(p1));
                }
                else if (s1 > s2) {
                    toRemove.add(getUUID(p2));
                }
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
            cInput2.update(tpf);
            cInput3.update(tpf);
            cInput4.update(tpf);

            // 判断是否游戏结束
            if (getGameWorld().getEntitiesByType(PLAYER).size() <= 1 ||
                getGameWorld().getEntitiesByType(COIN).size() == 0)
                gameOver();
        }

        // 更新显示信息
        if (IS_SERVER) {
            // 广播所有可移动实体的位置信息
            Bundle bundle = new Bundle("updateData");

            NetMessage msg = new NetMessage();
            msg.actItems.put(getUUID(player1), new ActionData(MOVE, PLAYER, player1.getX(), player1.getY()));
            msg.actItems.put(getUUID(player2), new ActionData(MOVE, PLAYER, player2.getX(), player2.getY()));
            msg.actItems.put(getUUID(player3), new ActionData(MOVE, PLAYER, player3.getX(), player3.getY()));
            msg.actItems.put(getUUID(player4), new ActionData(MOVE, PLAYER, player4.getX(), player4.getY()));

            for (var enemy : getGameWorld().getEntitiesByType(ENEMY)) {
                msg.actItems.put(getUUID(enemy), new ActionData(MOVE, ENEMY, enemy.getX(), enemy.getY()));
            }

            for (var uuid : toRemove) {
                Entity e = getGameWorld().getEntityByID(uuid, 0).get();
                msg.actItems.put(getUUID(e), new ActionData(DEL, BLOCK, e.getX(), e.getY()));
                e.removeFromWorld();
            }

            toRemove.clear();

            bundle.put("items", msg);
            server.broadcast(bundle);
        }
    }

    private void gameOver() {
        // TODO: gameover
        int winner = 0, maxScore = 0;
        for (int i = 1; i <= 4; i++) {
            int score = geti(String.format("score%d", i));
            if (score >= maxScore) {
                winner = i;
                maxScore = score;
            }
        }
        getDialogService().showMessageBox(String.format("Player %d wins! Press OK to exit.", winner), getGameController()::exit);
    }

    private MultiplayerService getMPService() {
        return getService(MultiplayerService.class);
    }

    public static void main(String args[]) {
        /* if (args.length == 1) {
            IS_SERVER = true;
        }
        else {
            IS_SERVER = false;
        } */
        launch(args);
    }

}
