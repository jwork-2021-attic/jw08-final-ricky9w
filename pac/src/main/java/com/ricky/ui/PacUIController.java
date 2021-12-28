package com.ricky.ui;

import com.almasb.fxgl.ui.ProgressBar;
import com.almasb.fxgl.ui.UIController;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.Config.*;

public class PacUIController implements UIController {
    
    @FXML
    private Pane root;

    private ProgressBar timeBar;

    

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

        // TODO: 显示其他信息
        
        
        root.getChildren().addAll(timeBar);
    }
}
