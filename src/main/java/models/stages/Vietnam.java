package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Stickman;

import java.util.ArrayList;

public class Vietnam extends GameStage{

    public Vietnam() {
        super("Vietnam", 3000, "/stages/bg004.png");
    }

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        if(frameCount % 30 == 0){
            Unit enemy = new Stickman();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }

    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec014.png";
    }
}
