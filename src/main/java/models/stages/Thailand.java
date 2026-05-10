package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Pig;

import java.util.ArrayList;

public class Thailand extends GameStage{

    public Thailand() {
        super("Thailand", 10000, "/stages/bg005.png");
    }

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        if(frameCount == 1){
            Unit enemy = new Pig();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }
}
