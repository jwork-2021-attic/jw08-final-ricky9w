package com.ricky.network;

import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.event.EventBus;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.multiplayer.NetworkComponent;
import com.almasb.fxgl.multiplayer.ReplicationEvent;
import com.almasb.fxgl.net.Connection;
import com.almasb.fxgl.net.Server;

public class MultiPlayerHandler {
    
    public Server<Bundle> server;
    public Connection<Bundle> connection;

    public Input[] clientInputs = new Input[4];
    public EventBus[] clientBuses = new EventBus[4];

    public Input getClienInput(int id) {
        return clientInputs[id - 1];
    }

    public EventBus getClientEventBus(int id) {
        return clientBuses[id - 1];
    }

    
}
