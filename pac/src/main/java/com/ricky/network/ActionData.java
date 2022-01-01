package com.ricky.network;

import com.ricky.PacType;

import java.io.Serializable;

public class ActionData implements Serializable {
    
    public ActionType actionType;

    public PacType entityType;

    public Double x, y;

    public ActionData(ActionType actionType, PacType entityType, Double x, Double y) {
        this.actionType = actionType;
        this.entityType = entityType;
        this.x = x;
        this.y = y;
    }
    
}
