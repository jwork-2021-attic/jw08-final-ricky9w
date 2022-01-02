package com.ricky;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.MenuItem;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.GameWorld;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.components.IDComponent;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.text.TextLevelLoader;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.ui.UI;
import com.ricky.components.Direction;
import com.ricky.components.PlayerComponent;
import com.ricky.components.ScoreComponent;
import com.ricky.network.NetMessage;
import com.ricky.ui.PacUIController;
import com.ricky.utils.PosData;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Server;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;

import java.util.Map;
import java.util.EnumSet;
import java.util.HashSet;

import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Pair;
import kotlin.TuplesKt;

import com.ricky.utils.Position2D;

import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.Config.*;
import static com.ricky.PacType.*;
import static com.ricky.network.ActionType.*;

import com.ricky.network.ActionData;

public class App extends GameApplication{

    @Override
    protected void initUI() {
        UI ui = getAssetLoader().loadUI("pac_ui.fxml", new PacUIController());
        ui.getRoot().setTranslateX(MAP_SIZE * BLOCK_SIZE);

        getGameScene().addUI(ui);
    }
    
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setMainMenuEnabled(true);
        settings.setEnabledMenuItems(EnumSet.allOf(MenuItem.class));

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
        if (player == null)
            throw(new NullPointerException());
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
                if (IS_SERVER) {
                    Bundle bundle = new Bundle("saveData");
                    
                    PosData pos = new PosData();

                    for (var coin : getGameWorld().getEntitiesByType(COIN)) {
                        pos.coins.put(getUUID(coin), new Position2D(coin.getX(), coin.getY()));
                    }

                    for (var enemy : getGameWorld().getEntitiesByType(ENEMY)) {
                        pos.enemies.put(getUUID(enemy), new Position2D(enemy.getX(), enemy.getY()));
                    }

                    pos.player1 = new Pair(getUUID(player1), new Position2D(player1.getX(), player1.getY()));
                    pos.player2 = new Pair(getUUID(player2), new Position2D(player2.getX(), player2.getY()));
                    pos.player3 = new Pair(getUUID(player3), new Position2D(player3.getX(), player3.getY()));
                    pos.player4 = new Pair(getUUID(player4), new Position2D(player4.getX(), player4.getY()));

                    for (int i = 1; i <= 4; i++)
                        bundle.put(String.format("score%d", i), geti(String.format("score%d", i)));
                    bundle.put("time", geti("time"));

                    bundle.put("pos", pos);

                    data.putBundle(bundle);
                }
            }

            @Override
            public void onLoad(DataFile data) {
                if (IS_SERVER) {

                    Bundle bundle = data.getBundle("saveData");
                    PosData pos = bundle.get("pos");
                    Bundle send = new Bundle("updateData");
                    NetMessage msg = new NetMessage();

                    for (var cPos : pos.coins.entrySet()) {
                        SpawnData cData = new SpawnData(cPos.getValue().x, cPos.getValue().y);
                        cData.put("uuid", cPos.getKey());
                        getGameWorld().spawn("0", cData);
                        msg.actItems.put(cPos.getKey(), new ActionData(ADD, COIN, cPos.getValue().x, cPos.getValue().y));
                    }

                    for (var ePos : pos.enemies.entrySet()) {
                        SpawnData eData = new SpawnData(ePos.getValue().x, ePos.getValue().y);
                        eData.put("uuid", ePos.getKey());
                        getGameWorld().spawn("E", eData);
                        msg.actItems.put(ePos.getKey(), new ActionData(ADD, ENEMY, ePos.getValue().x, ePos.getValue().y));
                    }

                    SpawnData pData1 = new SpawnData(pos.player1.getValue().x, pos.player1.getValue().y);
                    pData1.put("uuid", pos.player1.getKey());
                    player1 = getGameWorld().spawn("P", pData1);
                    msg.actItems.put(pos.player1.getKey(), new ActionData(ADD, PLAYER, pos.player1.getValue().x, pos.player2.getValue().y));

                    SpawnData pData2 = new SpawnData(pos.player2.getValue().x, pos.player2.getValue().y);
                    pData2.put("uuid", pos.player2.getKey());
                    player2 = getGameWorld().spawn("P", pData2);
                    msg.actItems.put(pos.player2.getKey(), new ActionData(ADD, PLAYER, pos.player2.getValue().x, pos.player2.getValue().y));

                    SpawnData pData3 = new SpawnData(pos.player3.getValue().x, pos.player3.getValue().y);
                    pData3.put("uuid", pos.player3.getKey());
                    player3 = getGameWorld().spawn("P", pData3);
                    msg.actItems.put(pos.player3.getKey(), new ActionData(ADD, PLAYER, pos.player3.getValue().x, pos.player3.getValue().y));

                    SpawnData pData4 = new SpawnData(pos.player4.getValue().x, pos.player4.getValue().y);
                    pData4.put("uuid", pos.player4.getKey());
                    player4 = getGameWorld().spawn("P", pData4);
                    msg.actItems.put(pos.player4.getKey(), new ActionData(ADD, PLAYER, pos.player4.getValue().x, pos.player4.getValue().y));

                    send.put("items", msg);

                    set("time", bundle.get("time"));
                    for (int i = 1; i <= 4; i++) {
                        set(String.format("score%d", i), bundle.get(String.format("score%d", i)));
                    }

                    server.broadcast(bundle);
                    
                }
            }
        });
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

            // 服务器端生成所有可移动物体并加上UUID
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

            getWorldProperties().<Integer>addListener("time", (old, now) -> {
                if (now == 0) {
                    gameOver();
                }
            });
        } else {

            var client = getNetService().newTCPClient("localhost", 55555);
            client.setOnConnected(conn -> {

                conn.addMessageHandlerFX((connection, message) -> {
                    if (message.exists("items")) {
                        handleUpdateMessage(message.get("items"));
                        if(message.exists("score1")){
                            set("score1", message.get("score1"));
                            set("score2", message.get("score2"));
                            set("score3", message.get("score3"));
                            set("score4", message.get("score4"));
                        }
                        
                    }
                    else if (message.exists("winner")) 
                        gameOverDialog(message.get("winner"));
                });

                getMPService().addEntityReplicationReceiver(conn, getGameWorld());

                getMPService().addInputReplicationSender(conn, getInput());

                getMPService().addPropertyReplicationReceiver(conn, getWorldProperties());

                
            });

            client.connectAsync();

            getInput().setProcessInput(false);
        }

        isReload = true;

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
                            SpawnData eData = new SpawnData(action.getValue().x, action.getValue().y);
                            eData.put("uuid", action.getKey());
                            getGameWorld().spawn("EC", eData);
                            break;
                        default:
                            break;
                    }
                    break;
                case MOVE:
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
        if (IS_SERVER) {
            // 玩家和怪物碰撞
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
            for (int i = 1; i <= 4; i++)
                bundle.put(String.format("score%d", i), geti(String.format("score%d", i)));
            server.broadcast(bundle);
        }
    }

    private void gameOver() {
        int winner = 0, maxScore = 0;
        for (int i = 1; i <= 4; i++) {
            int score = geti(String.format("score%d", i));
            if (score >= maxScore) {
                winner = i;
                maxScore = score;
            }
        }
        //通知其他客户端
        Bundle bundle = new Bundle("gameOver");
        bundle.put("winner", winner);
        server.broadcast(bundle);
        gameOverDialog(winner);
    }

    private void gameOverDialog(int winner) {
        getDialogService().showMessageBox(String.format("Player %d wins! Press OK to exit", winner), getGameController()::exit);
    }

    private MultiplayerService getMPService() {
        return getService(MultiplayerService.class);
    }

    public static void main(String args[]) {
        if (args.length == 1) {
            IS_SERVER = true;
        } else {
            IS_SERVER = false;
        }
        launch(args);
    }

}
