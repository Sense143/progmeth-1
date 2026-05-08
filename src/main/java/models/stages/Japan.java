package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Dog1;
import models.enemies.Snake;

import java.util.ArrayList;

public class Japan extends GameStage{
    public Japan() {
        super("Japan", 1500, "/stages/bg001.png");
    }

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        if(frameCount >= 600){
            if(frameCount % 600 == 0){
                Unit enemy = new Dog1();
                units.add(enemy);
                BattleManager.getInstance().addEnemyUnit(enemy);
            }
        }
        if(frameCount >= 1200){
            Unit enemy = new Snake();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }
}
