package com.ricky.components;

import javafx.geometry.Point2D;


// 使用 Point2D(x, y)中的二维信息表示方向
public enum Direction {
    UP(new Point2D(0, -1)), RIGHT(new Point2D(1, 0)), DOWN(new Point2D(0, 1)), LEFT(new Point2D(-1, 0));

    public Point2D vector;
    
    Direction(Point2D vector) {
        this.vector = vector;
    }
}
