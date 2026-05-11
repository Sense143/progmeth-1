package models.base;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import logic.BattleManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

/**
 * A sweeping cannon-wave projectile fired from the player's tower.
 *
 * <p>The wave travels left across the battlefield in discrete steps. At each
 * step a four-frame explosion animation plays, and any enemy inside the blast
 * radius takes {@code damage} once per step (tracked via {@link #hitUnits}).
 * The wave deactivates automatically when it leaves the left screen boundary.
 */
public class CannonWave {
    private double x;
    /** Fixed ground Y at which the wave travels. */
    private final double y = 410;
    /** Distance (pixels) the wave moves after completing one four-frame cycle. */
    private final double STEP_SIZE = 70;
    /** Half-width (pixels) of the damage zone centred on the current position. */
    private final double WAVE_WIDTH = 100;
    /** Damage dealt to each enemy caught in the blast. */
    private double damage = 500;

    private int currentFrame = 0;
    private int frameCounter = 0;
    /** Game-loop ticks per animation frame (lower = faster animation). */
    private final int ANIMATION_DELAY = 5;

    /** Four-frame blast animation. */
    private Image[] frames = new Image[4];
    /** Whether the wave is still active and should be updated/drawn. */
    private boolean active = true;
    /**
     * Tracks enemies hit at the current step so that each enemy takes damage
     * only once per step, not once per frame.
     */
    private HashSet<Unit> hitUnits = new HashSet<>();

    /**
     * Constructs a cannon wave starting at the given world X position.
     * Loads the four-frame blast sprite sheet from resources.
     *
     * @param startX world X where the wave originates (typically the tower X)
     */
    public CannonWave(double startX) {
        this.x = startX;
        try {
            for (int i = 0; i < 4; i++) {
                frames[i] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tower/tower_cannon_" + (i + 1) + ".png")));
            }
        } catch (Exception e) {
            System.err.println("โหลดรูป CannonWave ไม่สำเร็จ");
        }
    }

    /**
     * Advances the wave by one game-loop frame.
     *
     * <p>Each call cycles the blast animation. After four frames the wave
     * steps left by {@link #STEP_SIZE} and the hit-set is cleared for the new
     * position. Every frame, all enemies within the current blast radius that
     * have not already been hit this step receive {@link #damage}.
     * The wave becomes inactive when its X coordinate goes below −200.
     */
    public void update() {
        if (!active) return;

        int FAST_DELAY = 2;
        frameCounter++;

        if (frameCounter >= FAST_DELAY) {
            frameCounter = 0;
            currentFrame++;

            if (currentFrame >= 4) {
                currentFrame = 0;
                x -= STEP_SIZE;
                hitUnits.clear();
            }
        }

        double rangeStart = x - WAVE_WIDTH;
        double rangeEnd = x;

        ArrayList<Unit> enemies = BattleManager.getInstance().getEveryTargetOnBoard();
        for (Unit enemy : enemies) {
            if(enemy instanceof Tower) continue;
            if (enemy.getHp() <= 0) continue;

            double enemyX = enemy.getX();

            if (enemyX >= rangeStart && enemyX <= rangeEnd) {
                if (!hitUnits.contains(enemy)) {
                    enemy.takeDamage(damage);
                    hitUnits.add(enemy);

                    System.out.println("BOOM! Hit: " + enemy.getClass().getSimpleName() + " at X: " + x);
                }
            }
        }

        if (x < -200) {
            active = false;
            hitUnits.clear();
        }
    }

    /**
     * Draws the current blast frame at the wave's world position, adjusted by
     * the camera offset.
     *
     * @param gc      the graphics context to draw onto
     * @param cameraX the current camera X offset
     */
    public void draw(GraphicsContext gc, double cameraX) {
        if (!active || frames[currentFrame] == null) return;

        double drawX = x - cameraX;
        gc.drawImage(frames[currentFrame], drawX, y - 300, 180, 300);
    }

    /**
     * Returns whether the wave is still travelling and should be kept in the
     * active-wave list.
     *
     * @return {@code true} if the wave has not yet left the screen
     */
    public boolean isActive() { return active; }
}