package com.ricky.ui;

import com.almasb.fxgl.ui.ProgressBar;
import com.almasb.fxgl.ui.UIController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.Config.*;

public class PacUIController implements UIController {
    
    @FXML
    private Pane root;

    private ProgressBar timeBar;

    @FXML
    private Label labelScore1;

    @FXML
    private Label labelScore2;
    
    @FXML
    private Label labelScore3;

    @FXML
    private Label labelScore4;

    @Override
    public void init() {
        timeBar = new ProgressBar(false);
        timeBar.setHeight(50);
        timeBar.setTranslateX(-60);
        timeBar.setTranslateY(100);
        timeBar.setRotate(-90);
        timeBar.setFill(Color.GREEN);
        timeBar.setLabelVisible(false);
        timeBar.setMaxValue(TIME_PER_LEVEL);
        timeBar.setMinValue(0);
        timeBar.setCurrentValue(TIME_PER_LEVEL);
        timeBar.currentValueProperty().bind(getip("time"));

        // FIXME
        // 1. 将分数转变为使用property存储
        // 2. 添加信息显示
        
        
        root.getChildren().addAll(timeBar);
    }
}
