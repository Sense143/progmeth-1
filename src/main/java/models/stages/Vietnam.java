package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Stickman;

import java.util.ArrayList;

/**
 * Stage 4 — Vietnam.
 *
 * <p>Enemy composition: {@link Stickman} spam.
 * <ul>
 *   <li>A Stickman spawns every 0.5 s (30 frames) — relentless pressure.</li>
 * </ul>
 * Tower HP: 3000.
 */
public class Vietnam extends GameStage{

    /**
     * Constructs the Vietnam stage with a 3000 HP enemy tower and its
     * background image.
     */
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

    /** @return {@code "/tower/towertexture/ec014.png"} */
    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec014.png";
    }
}