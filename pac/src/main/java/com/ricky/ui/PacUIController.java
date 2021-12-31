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

        labelScore1.textProperty().bind(getip("score1").asString("Score1: %d"));
        labelScore2.textProperty().bind(getip("score2").asString("Score2: %d"));
        labelScore3.textProperty().bind(getip("score3").asString("Score3: %d"));
        labelScore4.textProperty().bind(getip("score4").asString("Score4: %d"));
        
        root.getChildren().addAll(timeBar);
    }
}
