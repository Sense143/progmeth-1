package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Hippo;
import models.enemies.Snake;

import java.util.ArrayList;

public class China extends GameStage{

    public China() {
        super("China", 2500, "/stages/bg002.png");
    }

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        if(frameCount % 150 == 0){
            Unit enemy = new Snake();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
        if(frameCount % 1200 == 0){
            Unit enemy = new Hippo();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }
}
