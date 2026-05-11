package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Dog1;

import java.util.ArrayList;

/**
 * Stage 1 — Korea.
 *
 * <p>Enemy composition: {@link Dog1} only.
 * <ul>
 *   <li>First dog spawns at frame 180 (3 s).</li>
 *   <li>Additional dogs spawn every 10 s (600 frames) thereafter.</li>
 * </ul>
 * Tower HP: 1500.
 */
public class Korea extends GameStage{

    /**
     * Constructs the Korea stage with a 1500 HP enemy tower and its
     * background image.
     */
    public Korea() {
        super("Korea", 1500, "/stages/bg000.png");
    }

    @Override
    protected void spawnEnemyLogic(ArrayList<Unit> units) {
        if(frameCount == 180){
            Unit enemy = new Dog1();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }

        if (frameCount % 600 == 0 && frameCount != 0) {
            Unit enemy = new Dog1();
            units.add(enemy);
            BattleManager.getInstance().addEnemyUnit(enemy);
        }
    }

    /** @return {@code "/tower/towertexture/ec018.png"} */
    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec018.png";
    }
}