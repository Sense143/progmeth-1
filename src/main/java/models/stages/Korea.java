package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Dog1;

import java.util.ArrayList;

public class Korea extends GameStage{

    public Korea() {
        super("Korea", 1500, "/stages/bg000.png");
    }

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        // frameCount วิ่ง 60 รอบ = 1 วินาที
        // สมมติอยากให้เสกหมาทุกๆ 3 วินาที (3 * 60 = 180 เฟรม)

        if(frameCount == 180){
            Unit enemy = new Dog1();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }

        if (frameCount % 600 == 0 && frameCount != 0) {
            // 1. สร้างหมาที่พิกัด X = 0 (ฝั่งซ้าย)
            Unit enemy = new Dog1();

            // 2. แอดหมาเข้าหน้าจอ
            units.add(enemy);

            // 3. แอดหมาเข้า BattleManager ให้แมวมองเห็นเป้าหมาย
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }
}
