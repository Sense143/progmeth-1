package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.*;

import java.util.ArrayList;

public class Thailand extends GameStage{

    public Thailand() {
        super("Thailand", 10000, "/stages/bg005.png");
    }

    int newFrameCount = -100;
    private boolean isPigArmySpawned = false;

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        if(frameCount == 1){
            Unit enemy = new Pig();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
        if(this.getEnemyTowerHp() <= 5000 && !isPigArmySpawned){
            Unit enemy = new Pig();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
            newFrameCount = frameCount;
            isPigArmySpawned = true;
        }
        if(frameCount - newFrameCount == 10){
            Unit enemy = new Pig();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
        if(frameCount % 240 == 0){
            Unit enemy = new Stickman();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
        if(frameCount % 250 == 0){
            Unit enemy = new Snake();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
        if(frameCount % 120 == 0){
            Unit enemy = new Dog1();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
        if(frameCount % 1800 == 0){
            Unit enemy = new Hippo();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }

    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec030.png";
    }
}
