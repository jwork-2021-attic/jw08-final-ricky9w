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
import com.ricky.components.PlayerComponent;
import com.ricky.ui.PacUIController;

import javafx.scene.effect.ColorInput;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import kotlin.ExtensionFunctionType;
import kotlin.contracts.Returns;

import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.Config.*;
import static com.ricky.PacType.*;

import java.util.Map;

import javax.sound.sampled.Port;

public class App extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
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

    @Override
    protected void initGame() {
        getGameScene().setBackgroundColor(Color.BLACK);

        getGameWorld().addEntityFactory(new PacFactory());

        Level level = getAssetLoader().loadLevel("pac_level0.txt", new TextLevelLoader(40, 40, ' '));
        getGameWorld().setLevel(level);

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
