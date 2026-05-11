package models.base;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Objects;

/**
 * A short five-frame explosion visual effect anchored to a world position.
 *
 * <p>Instances are created by {@link logic.EffectManager#spawnExplosion} and
 * drawn each frame via {@link #draw}. The effect marks itself finished after
 * all five frames have played; {@link logic.EffectManager} removes it from
 * the active list at that point.
 */
public class ExplosionEffect {
    private double x, y;
    /** Five-frame sprite sequence for the explosion. */
    private Image[] frames = new Image[5];
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    /** Milliseconds each frame is shown before advancing. */
    private int frameDelay = 50;
    /** {@code true} once all frames have been displayed. */
    private boolean finished = false;

    /**
     * Constructs an explosion effect centred at the given world coordinates
     * and immediately loads the sprite sheet.
     *
     * @param x world X of the explosion centre
     * @param y world Y of the explosion centre
     */
    public ExplosionEffect(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastFrameTime = System.currentTimeMillis();

        try {
            for(int i=0; i<5; i++) {
                frames[i] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_e" + (i+1) + ".png")));
            }
        } catch (Exception e) {
            System.err.println("โหลดรูป爆発ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /**
     * Advances the animation timer and draws the current frame onto the canvas.
     * Does nothing if the effect has already finished.
     *
     * @param gc      the graphics context to draw onto
     * @param cameraX the current camera X offset (world → screen conversion)
     */
    public void draw(GraphicsContext gc, double cameraX) {
        if(finished) return;

        long currentTime = System.currentTimeMillis();
        if(currentTime - lastFrameTime >= frameDelay) {
            currentFrame++;
            lastFrameTime = currentTime;
        }

        if(currentFrame >= 5) {
            finished = true;
            return;
        }

        if(frames[currentFrame] != null) {

            double sfxWidth = 80;
            double sfxHeight = 150;

            double groundY = 410;
            double drawY = groundY - sfxHeight;

            gc.drawImage(
                    frames[currentFrame],
                    (x - sfxWidth/2) - cameraX,
                    drawY,
                    sfxWidth,
                    sfxHeight
            );
        }
    }

    /**
     * Returns {@code true} after all five frames have been displayed.
     * {@link logic.EffectManager} uses this to remove completed effects.
     *
     * @return {@code true} when the animation is done
     */
    public boolean isFinished() {
        return finished;
    }
}