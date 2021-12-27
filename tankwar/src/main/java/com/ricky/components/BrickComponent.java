package com.ricky.components;

import com.almasb.fxgl.entity.component.Component;

public class BrickComponent extends Component {
    
    private int hp = 10;

    public void onHit() {
        if (hp <= 0)
            return;
        
        hp--;

        //  砖块被攻击后颜色变淡, hp为0则从世界中移除
        if (hp >= 1) {
            entity.getViewComponent().setOpacity(0.5);
        } else {
            entity.removeFromWorld();
        }
    }
}
