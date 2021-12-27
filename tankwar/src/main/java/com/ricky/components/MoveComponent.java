package com.ricky.components;

import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.core.util.LazyValue;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityGroup;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.ricky.TankWarType;

import java.util.List;

import static com.ricky.TankWarType.*;

public class MoveComponent extends Component {
    
    private BoundingBoxComponent bbox;

    private Direction moveDir;


    // TODO: 物体移动逻辑
    private boolean movedThisFrame = false;

}
