package com.ricky.utils;

import java.io.Serializable;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javafx.beans.binding.DoubleExpression;
import javafx.geometry.Point2D;
import javafx.util.Pair;

public class PosData implements Serializable {
    
    public Set<Pair<Double, Double>> coins;

    public Set<Pair<Double, Double>> enemies;

    public Map<Integer, Pair<Double, Double>> players;

    public Pair<Double, Double> player1, player2, player3, player4;


    public PosData() {
        coins = new HashSet<Pair<Double, Double>>();

        enemies = new HashSet<Pair<Double, Double>>();

        players = new HashMap<Integer, Pair<Double, Double>>();
    }
}
