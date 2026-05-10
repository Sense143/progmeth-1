package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Dog1;
import models.enemies.Snake;

import java.util.ArrayList;

public class Japan extends GameStage{
    public Japan() {
        super("Japan", 2000, "/stages/bg001.png");
    }

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        if (frameCount == 1) {
            Unit startSnake1 = new Snake();
            units.add(startSnake1);
            BattleManager.getInstance().addEnemyUnit(startSnake1);
        }

        if (frameCount == 60) {
            Unit startSnake2 = new Snake();
            units.add(startSnake2);
            BattleManager.getInstance().addEnemyUnit(startSnake2);
        }

        // 🌟 แยกเงื่อนไขการเสก Dog1 ให้ห่างกัน 6 เฟรม (0.1 วินาที)
        if (frameCount >= 600) {
            // หมาตัวแรกออกมาตอนครบรอบเป๊ะๆ
            if (frameCount % 600 == 0) {
                Unit dogA = new Dog1();
                units.add(dogA);
                BattleManager.getInstance().addEnemyUnit(dogA);
            }

            // หมาตัวที่สองออกมาทีหลัง 6 เฟรม (0.1 วิ)
            if (frameCount % 600 == 6) {
                Unit dogB = new Dog1();
                units.add(dogB);
                BattleManager.getInstance().addEnemyUnit(dogB);
            }
        }

        if (frameCount >= 1200 && frameCount % 600 == 0) {
            Unit enemy = new Snake();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }

        if (frameCount >= 1200 && frameCount % 700 == 0) {
            Unit enemy = new Snake();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }

    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec038.png";
    }
}
