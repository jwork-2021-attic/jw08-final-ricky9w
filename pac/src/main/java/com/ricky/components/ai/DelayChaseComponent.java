package com.ricky.components.ai;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;
import static com.almasb.fxgl.dsl.FXGL.*;

import static com.ricky.PacType.*;

import java.util.Random;


@Required(AStarMoveComponent.class)
public class DelayChaseComponent extends Component {
    
    private AStarMoveComponent astar;

    private boolean isDelayed = false;

    @Override
    public void onUpdate(double tpf) {
        if (!isDelayed) {
            move();
        } else {
            if (astar.isAtDestination()) {
                move();
            }
        }
    }

    private void move() {
        Random rand = new Random();

        var players = getGameWorld().getEntitiesByType(PLAYER);
        var player = players.get(rand.nextInt(players.size()));
        
        int x = player.call("getCellX");
        int y = player.call("getCellY");

        astar.moveToCell(x, y);
    }

    public DelayChaseComponent withDelay() {
        isDelayed = true;
        return this;
    }
}
