package com.ricky.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.ricky.components.Direction;
import com.ricky.network.ActionData;
import com.ricky.network.ActionType;
import com.ricky.network.NetMessage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import com.ricky.components.PlayerComponent;

import org.junit.Test;

public class PlayerComponentTest {
    
    @Test
    public void testDirs() {
        PlayerComponent component = new PlayerComponent();
        component.up();
        assertEquals(Direction.UP, component.nextMoveDir);
        component.down();
        assertEquals(Direction.DOWN, component.nextMoveDir);
        component.left();
        assertEquals(Direction.LEFT, component.nextMoveDir);
        component.right();
        assertEquals(Direction.RIGHT, component.nextMoveDir);
    }

    @Test
    public void testUpdate() {
        PlayerComponent component = new PlayerComponent();
        component.currMoveDir = Direction.RIGHT;
        assertEquals(Direction.RIGHT, component.currMoveDir);
        component.up();
    }

}
