package com.ricky;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.level.Level;
import com.almasb.fxgl.entity.level.text.TextLevelLoader;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.pathfinding.CellState;
import com.almasb.fxgl.pathfinding.astar.AStarGrid;
import com.almasb.fxgl.ui.UI;
import com.ricky.ui.TankWarUIController;

import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;
import static com.ricky.Config.*;


public class App extends GameApplication {


    // 初始化游戏窗口设置
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(MAP_SIZE * BLOCK_SIZE + UI_SIZE);
        settings.setHeight(MAP_SIZE * BLOCK_SIZE);
        settings.setTitle("Title");
        settings.setVersion("1.0");
        settings.setManualResizeEnabled(false);
    }

    // 初始化输入处理
    @Override
    protected void initInput() {

    }

    // 初始化游戏数据相关变量
    protected void initGameVars(Map<String, Object> vars) {
        
    }

    // 游戏开始
    @Override
    protected void initGame() {

    }

    // 初始化物理引擎设置
    @Override
    protected void initPhysics() {

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

    
    public static void main(String[] args) {
        System.out.println("Hello World");
        launch(args);
    }

}