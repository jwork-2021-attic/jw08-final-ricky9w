package com.ricky.utils;

import java.io.Serializable;

import javafx.geometry.Point2D;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SaveData implements java.io.Serializable {
    private static final long serialVersionID = 1L;

    public Set<Point2D> coins;
    
    public Point2D player1, player2, player3, player4;

    public Set<Point2D> enemies;

    public int score1, score2, score3, score4;

    public SaveData() {
        coins = new HashSet<Point2D>();
        enemies = new HashSet<Point2D>();
    }

}
