package com.ricky;

import javafx.util.Duration;

public class Config {
    
    public static final int BLOCK_SIZE = 40;

    // 地图大小
    public static final int MAP_SIZE = 21;

    public static final int UI_SIZE = 80;

    // 子弹飞行速度
    public static final double BULLET_SPEED = 10 * 60;

    // 两次射击时间间隔
    public static final Duration SHOOT_DELAY = Duration.seconds(1);

    // 取得胜利所需的占领时间
    public static final int VICTORY_TIME = 10;

}
