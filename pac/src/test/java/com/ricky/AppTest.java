package com.ricky;

import static org.junit.Assert.assertTrue;

import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.entity.Entity;
import com.ricky.components.Direction;
import com.ricky.network.ActionData;
import com.ricky.network.ActionType;
import com.ricky.network.NetMessage;

import java.lang.reflect.Method;
import java.util.UUID;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void settingInitTest() {
        new App().initSettings(new GameSettings());
        System.out.println("settings initialized");
    }

    @Test
    public void serverTest() throws Exception {
        var app = new App();
        Class[] mArgs = { Entity.class };
        try {
            Method m = app.getClass().getDeclaredMethod("getPlayerComponent", mArgs);
            m.setAccessible(true);
            m.invoke(app, new Entity());
        } catch (NoSuchMethodException e) {
            System.out.println("no such method");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    public void moveTest() throws Exception {
        var app = new App();
        Class[] mArgs = { Entity.class, Direction.class };
        try {
            Method m = app.getClass().getDeclaredMethod("moveTowards", mArgs);
            m.setAccessible(true);
            m.invoke(app, new Entity(), Direction.UP);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    public void testMessageHandler() {
        var msg = new NetMessage();
        msg.actItems.put(UUID.randomUUID().toString(), new ActionData(ActionType.ADD, PacType.COIN, 100.0, 100.0));
        var app = new App();
        Class[] mArgs = { NetMessage.class };
        try {
            Method m = app.getClass().getDeclaredMethod("handleUpdateMessage", mArgs);
            m.setAccessible(true);
            m.invoke(app, msg);
        } catch (Exception e) {
            System.out.println(e);
        }
       
    }
}
