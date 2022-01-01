package com.ricky.components;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.pathfinding.CellMoveComponent;
import com.almasb.fxgl.pathfinding.astar.AStarMoveComponent;

import static com.ricky.components.Direction.*;

@Required(AStarMoveComponent.class)
public class PlayerComponent extends Component {
    
    private CellMoveComponent moveComponent;

    private AStarMoveComponent astar;

    public Direction currMoveDir = RIGHT;
    public Direction nextMoveDir = RIGHT;

    public void up() {
        nextMoveDir = UP;
    }

    public void down() {
        nextMoveDir = DOWN;
    }

    public void left() {
        nextMoveDir = LEFT;
    }

    public void right() {
        nextMoveDir = RIGHT;
    }
    
    @Override
    public void onUpdate(double tpf) {
        var x = moveComponent.getCellX();
        var y = moveComponent.getCellY();

        if (x == 0 && currMoveDir == LEFT) {
            
        } else if (x == astar.getGrid().getWidth() - 1 && currMoveDir == RIGHT) {

        }

        if (astar.isMoving())
            return;

            switch (nextMoveDir) {
                case UP:
                    if (astar.getGrid().getUp(x, y).filter(c -> c.getState().isWalkable()).isPresent())
                        currMoveDir = nextMoveDir;
                    break;
                case RIGHT:
                    if (astar.getGrid().getRight(x, y).filter(c -> c.getState().isWalkable()).isPresent())
                        currMoveDir = nextMoveDir;
                    break;
                case DOWN:
                    if (astar.getGrid().getDown(x, y).filter(c -> c.getState().isWalkable()).isPresent())
                        currMoveDir = nextMoveDir;
                    break;
                case LEFT:
                    if (astar.getGrid().getLeft(x, y).filter(c -> c.getState().isWalkable()).isPresent())
                        currMoveDir = nextMoveDir;
                    break;
            }

        switch (currMoveDir) {
            case UP:
                astar.moveToUpCell();
                break;
            case RIGHT:
                astar.moveToRightCell();
                break;
            case DOWN:
                astar.moveToDownCell();
                break;
            case LEFT:
                astar.moveToLeftCell();
                break;
        }
    }
}
