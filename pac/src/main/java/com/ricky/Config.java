package com.ricky;

import javafx.geometry.Point2D;

public class Config {
    
    public static final int BLOCK_SIZE = 40;
    
    public static final int MAP_SIZE = 15;

    public static final int UI_SIZE = 100;

    public static final int TIME_PER_LEVEL = 100;

    public static final Point2D[] SPAWN_PLAYERS = {
        new Point2D(1 * BLOCK_SIZE, 1 * BLOCK_SIZE),
        new Point2D((MAP_SIZE - 2) * BLOCK_SIZE, 1 * BLOCK_SIZE),
        new Point2D(1 * BLOCK_SIZE, (MAP_SIZE - 2) * BLOCK_SIZE),
        new Point2D((MAP_SIZE - 2) * BLOCK_SIZE, (MAP_SIZE - 2) * BLOCK_SIZE)
    };

    public static final Point2D[] SPAWN_ENEMIES = {
        new Point2D(3 * BLOCK_SIZE, 5 * BLOCK_SIZE),
        new Point2D(4 * BLOCK_SIZE, 5 * BLOCK_SIZE),
        new Point2D(5 * BLOCK_SIZE, 5 * BLOCK_SIZE),
        new Point2D(6 * BLOCK_SIZE, 5 * BLOCK_SIZE)
    };
    
    public static boolean IS_SERVER = true;
    
}
