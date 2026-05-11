package models.stages;

import logic.BattleManager;
import models.base.Unit;
import models.enemies.*;

import java.util.ArrayList;

/**
 * Stage 5 — Thailand (final stage).
 *
 * <p>Enemy composition: all unit types with a scripted mid-stage pig surge.
 * <ul>
 *   <li>Frame 1: opening {@link Pig}.</li>
 *   <li>When enemy tower HP drops below 5000: two additional Pigs spawn in
 *       quick succession (the pig army trigger).</li>
 *   <li>Dogs every 2 s (120 frames), Stickmen every 4 s (240 frames),
 *       Snakes every ~4.2 s (250 frames), Hippos every 30 s (1800 frames).</li>
 * </ul>
 * Tower HP: 10000. Uses stage-specific BGM (004.ogg) as set in
 * {@code Main.showGameScene()}.
 */
public class Thailand extends GameStage{

    /**
     * Constructs the Thailand stage with a 10000 HP enemy tower and its
     * background image.
     */
    public Thailand() {
        super("Thailand", 10000, "/stages/bg005.png");
    }

    /** Frame count at which the pig army began, used to schedule the second pig. */
    int newFrameCount = -100;
    /** Guards the pig army trigger so it fires only once per stage. */
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

    /** @return {@code "/tower/towertexture/ec030.png"} */
    @Override
    public String getEnemyTowerImagePath() {
        return "/tower/towertexture/ec030.png";
    }
}