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
import com.fasterxml.jackson.databind.deser.impl.PropertyValue;
import com.fasterxml.jackson.databind.util.EnumValues;
import com.ricky.components.PlayerComponent;
import com.ricky.ui.PacUIController;
import com.ricky.utils.PosData;
import com.almasb.fxgl.core.collection.Array;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.multiplayer.ReplicationEvent;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.net.Server;
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
import java.sql.ClientInfoStatus;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.datatype.DatatypeFactory;

public class newApp extends GameApplication{

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
    
    private Input cInput2, cInput3, cInput4;

    private Map<Entity, Input> ctrlMap;

    @Override
    protected void initInput() {
        ctrlMap.put(player2, cInput2);
        ctrlMap.put(player3, cInput3);
        ctrlMap.put(player4, cInput4);

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
        for (Map.Entry<Entity, Input> entry : ctrlMap.entrySet()) {
            onKeyBuilder(entry.getValue(), KeyCode.W)
                    .onAction(() -> { entry.getKey().getComponent(PlayerComponent.class).up(); 
                    // TODO: delete debug output
                    System.out.println("client key pressed");
                    });
            onKeyBuilder(entry.getValue(), KeyCode.S)
                    .onAction(() -> { entry.getKey().getComponent(PlayerComponent.class).down(); });
            onKeyBuilder(entry.getValue(), KeyCode.A)
                    .onAction(() -> { entry.getKey().getComponent(PlayerComponent.class).left(); });
            onKeyBuilder(entry.getValue(), KeyCode.D)
                    .onAction(() -> { entry.getKey().getComponent(PlayerComponent.class).right(); });
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

                
            }

            @Override
            public void onLoad(DataFile data) {

            }
        });
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {

    }

    @Override
    protected void initGame() {

    }

    @Override
    protected void initPhysics() {

    }

    @Override
    protected void onUpdate(double tpf) {

    }

    private void gameOver() {

    }

    private MultiplayerService getMPService() {
        return getService(MultiplayerService.class);
    }

    

}
