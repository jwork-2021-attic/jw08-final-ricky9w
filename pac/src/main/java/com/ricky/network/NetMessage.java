package com.ricky.network;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

public class NetMessage implements Serializable {

    public Map<String, ActionData> actItems;

    public NetMessage() {
        actItems = new HashMap<>();
    }
}
