package com.ricky.ui;

import com.almasb.fxgl.ui.ProgressBar;
import com.almasb.fxgl.ui.UIController;
import com.ricky.Config;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.almasb.fxgl.dsl.FXGL.*;

public class TankWarUIController implements UIController {
    
    @FXML
    private Pane root;

    private ProgressBar timeBar;

    @FXML
    private Label labelBar;
    

    @Override
    public void init() {
        timeBar = new ProgressBar(true);
        timeBar.setHeight(50);
        timeBar.setTranslateX(-60);
        timeBar.setTranslateY(100);
        timeBar.setRotate(-90);
        // TODO: timebar颜色
        timeBar.setFill(Color.RED);
        timeBar.setLabelVisible(false);
        timeBar.setMaxValue(Config.VICTORY_TIME);
        timeBar.setMinValue(0);
        // TODO: timebar取值和绑定
        timeBar.setCurrentValue(0);
        
        root.getChildren().addAll(timeBar);

        labelBar.setFont(getUIFactoryService().newFont(18));
        labelBar.textProperty().set("Time");
        

    }
}
