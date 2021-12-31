package com.ricky;

import javafx.geometry.Point2D;

public class Config {
    
    public static final int BLOCK_SIZE = 40;
    
    public static final int MAP_SIZE = 21;

    public static final int UI_SIZE = 100;

    public static final int TIME_PER_LEVEL = 100;

    public static final Point2D[] SPAWN_PLAYERS = {
        new Point2D(1 * BLOCK_SIZE, 1 * BLOCK_SIZE),
        new Point2D(19 * BLOCK_SIZE, 1 * BLOCK_SIZE),
        new Point2D(1 * BLOCK_SIZE, 19 * BLOCK_SIZE),
        new Point2D(19 * BLOCK_SIZE, 19 * BLOCK_SIZE)
    };

    public static final Point2D[] SPAWN_ENEMIES = {
        new Point2D(9 * BLOCK_SIZE, 8 * BLOCK_SIZE),
        new Point2D(10 * BLOCK_SIZE, 8 * BLOCK_SIZE),
        new Point2D(11 * BLOCK_SIZE, 8 * BLOCK_SIZE),
        new Point2D(10 * BLOCK_SIZE, 9 * BLOCK_SIZE)
    };

    // FIXME: 设定玩家ID和服务器配置
/*     public static final int CLIENT_ID = 1;
    
    public static final boolean IS_SERVER = true; */

    public static final int CLIENT_ID = 2;

    public static final boolean IS_SERVER = false;
    
}
