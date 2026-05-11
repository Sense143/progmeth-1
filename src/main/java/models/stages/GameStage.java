package models.stages;

import javafx.scene.image.Image;
import models.base.Unit;
import logic.BattleManager;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Abstract base class for all game stages (levels).
 *
 * <p>A {@code GameStage} owns the enemy-spawn schedule, the enemy-tower HP,
 * and the stage's background image. The game loop calls
 * {@link #updateStage(ArrayList, double)} every frame; subclasses implement
 * {@link #spawnEnemyLogic(ArrayList)} to define the stage's unique wave
 * patterns using {@link #frameCount} as a timer (60 ticks ≈ 1 second).
 */
public abstract class GameStage {
    /** Human-readable stage name shown in the UI. */
    protected String stageName;
    /** Current HP of the enemy tower; synced from the live tower each frame. */
    protected double enemyTowerHp;
    /** Background panorama image drawn behind the battlefield. */
    protected Image backgroundImage;

    /** Game-loop frame counter; incremented once per {@link #updateStage} call. */
    protected int frameCount = 0;

    /**
     * Constructs a stage with the given name, enemy-tower HP, and background.
     *
     * @param name      display name of the stage
     * @param towerHp   starting HP of the enemy tower
     * @param imagePath classpath resource path to the background image
     */
    public GameStage(String name, double towerHp, String imagePath) {
        this.stageName = name;
        this.enemyTowerHp = towerHp;
        try {
            this.backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("หาภาพไม่เจอที่ Path: " + imagePath);
        }
    }

    /** @return the stage's display name */
    public String getStageName() { return stageName; }

    /** @return the current (live) HP of the enemy tower */
    public double getEnemyTowerHp() { return enemyTowerHp; }

    /** @return the background panorama image */
    public Image getBackgroundImage() { return backgroundImage; }

    /**
     * Called once per game-loop frame. Increments {@link #frameCount} and
     * delegates to {@link #spawnEnemyLogic(ArrayList)} for wave scheduling.
     * Also keeps {@link #enemyTowerHp} in sync with the live tower value.
     *
     * @param units     the active unit list (enemies are added here)
     * @param currentHp the enemy tower's current HP this frame
     */
    public void updateStage(ArrayList<Unit> units, double currentHp) {
        frameCount++;
        spawnEnemyLogic(units);
        this.enemyTowerHp = currentHp;
    }

    /**
     * Stage-specific enemy-spawn logic, driven by {@link #frameCount}.
     * Implementations create enemies, add them to {@code units}, and register
     * them with {@link BattleManager} so cat units can find them as targets.
     *
     * @param units the live unit list to which new enemies are added
     */
    protected abstract void spawnEnemyLogic(ArrayList<Unit> units);

    /**
     * Returns the classpath resource path to the enemy tower sprite for this
     * stage.
     *
     * @return sprite path, e.g. {@code "/tower/towertexture/ec018.png"}
     */
    public abstract String getEnemyTowerImagePath();
}