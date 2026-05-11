package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Hippo;
import models.enemies.Snake;

import java.util.ArrayList;

/**
 * Stage 3 — China.
 *
 * <p>Enemy composition: {@link Snake} and {@link Hippo}.
 * <ul>
 *   <li>Snakes spawn every 2.5 s (150 frames).</li>
 *   <li>A Hippo boss spawns every 20 s (1200 frames).</li>
 * </ul>
 * Tower HP: 2500.
 */
public class China extends GameStage{

    /**
     * Constructs the China stage with a 2500 HP enemy tower and its
     * background image.
     */
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

    /** @return {@code "/tower/towertexture/ec046.png"} */
    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec046.png";
    }
}