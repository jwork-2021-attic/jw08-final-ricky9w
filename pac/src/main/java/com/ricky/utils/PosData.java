package com.ricky.utils;

import java.io.Serializable;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javafx.util.Pair;

public class PosData implements java.io.Serializable {
    
    public Set<Pair<Double, Double>> coins;

    public Set<Pair<Double, Double>> enemies;

    public Map<Integer, Pair<Double, Double>> players;


    public PosData() {
        coins = new HashSet<Pair<Double, Double>>();

        enemies = new HashSet<Pair<Double, Double>>();

        players = new HashMap<Integer, Pair<Double, Double>>();
    }
}
