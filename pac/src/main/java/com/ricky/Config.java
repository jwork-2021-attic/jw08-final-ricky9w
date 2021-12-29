package com.ricky;

import javafx.geometry.Point2D;
import javafx.scene.effect.Light.Point;

public class Config {
    
    public static final int BLOCK_SIZE = 40;
    
    public static final int MAP_SIZE = 21;

    public static final int UI_SIZE = 80;

    public static final int TIME_PER_LEVEL = 100;

    // FIXME: 设定玩家ID
    public static final int PLAYER_ID = 1;

    public static final Point2D SPAWN_P1 = new Point2D(1 * BLOCK_SIZE, 1 * BLOCK_SIZE);
    public static final Point2D SPAWN_P2 = new Point2D(19 * BLOCK_SIZE, 1 * BLOCK_SIZE);
    public static final Point2D SPAWN_P3 = new Point2D(1 * BLOCK_SIZE, 19 * BLOCK_SIZE);
    public static final Point2D SPAWN_P4 = new Point2D(19 * BLOCK_SIZE, 19 * BLOCK_SIZE);

    public static final Point2D SPAWN_E1 = new Point2D(9 * BLOCK_SIZE, 8 * BLOCK_SIZE);
    public static final Point2D SPAWN_E2 = new Point2D(10 * BLOCK_SIZE, 8 * BLOCK_SIZE);
    public static final Point2D SPAWN_E3 = new Point2D(11 * BLOCK_SIZE, 8 * BLOCK_SIZE);
    public static final Point2D SPAWN_E4 = new Point2D(10 * BLOCK_SIZE, 9 * BLOCK_SIZE);
    
    
}
