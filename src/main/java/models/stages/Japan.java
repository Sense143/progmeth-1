package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.Dog1;
import models.enemies.Snake;

import java.util.ArrayList;

/**
 * Stage 2 — Japan.
 *
 * <p>Enemy composition: {@link Snake} and {@link Dog1}.
 * <ul>
 *   <li>Two snakes spawn at frames 1 and 60 (opening rush).</li>
 *   <li>From frame 600 onward: two dogs spawn every 10 s,
 *       staggered 6 frames apart to avoid exact overlap.</li>
 *   <li>From frame 1200 onward: extra snakes every 10 s and every ~11.7 s.</li>
 * </ul>
 * Tower HP: 2000.
 */
public class Japan extends GameStage{

    /**
     * Constructs the Japan stage with a 2000 HP enemy tower and its
     * background image.
     */
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

        if (frameCount >= 600) {
            if (frameCount % 600 == 0) {
                Unit dogA = new Dog1();
                units.add(dogA);
                BattleManager.getInstance().addEnemyUnit(dogA);
            }

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

    /** @return {@code "/tower/towertexture/ec038.png"} */
    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec038.png";
    }
}