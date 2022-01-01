package com.ricky.utils;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;

public class PosData implements Serializable {
    
    public Map<String, Position2D> coins;
    
    public Map<String, Position2D> enemies;

    public Pair<String, Position2D> player1, player2, player3, player4;

    public PosData() {
        coins = new HashMap<>();
        enemies = new HashMap<>();
    }

}
