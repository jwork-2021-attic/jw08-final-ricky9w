package com.ricky.components;

import com.almasb.fxgl.entity.component.Component;

public class ScoreComponent extends Component {
    
    private int score = 0;

    public int getScore() {
        return score;
    }

    public void inc(int x) {
        score += x;
    }
}
