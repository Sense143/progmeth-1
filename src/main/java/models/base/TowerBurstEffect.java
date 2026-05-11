package models.base;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Objects;

/**
 * A five-frame burst visual effect played at the moment a tower is destroyed.
 *
 * <p>Unlike {@link ExplosionEffect}, the burst uses a shared static sprite
 * array so the images are loaded only once regardless of how many bursts fire.
 * Each instance advances independently using a game-loop tick counter.
 */
public class TowerBurstEffect {
    private double x, y;
    /**
     * Five-frame burst sprite sheet, shared across all instances to avoid
     * redundant image loading.
     */
    private static Image[] sharedFrames = new Image[5];
    private int currentFrame = 0;
    private int frameTick = 0;
    /** Game-loop ticks to display each frame (~66 ms at 60 fps). */
    private static final int FRAME_DELAY = 4;
    /** {@code true} once all five frames have been displayed. */
    private boolean finished = false;

    /**
     * Constructs a burst effect at the given world coordinates.
     * Loads the shared sprite sheet on the first instantiation.
     *
     * @param x world X of the burst centre
     * @param y world Y of the burst centre
     */
    public TowerBurstEffect(double x, double y) {
        this.x = x;
        this.y = y;
        if (sharedFrames[0] == null) {
            try {
                for (int i = 0; i < 5; i++) {
                    sharedFrames[i] = new Image(Objects.requireNonNull(
                        getClass().getResourceAsStream("/explosion/explode_" + (i + 1) + ".png")));
                }
            } catch (Exception e) {
                System.err.println("Failed to load tower burst frames: " + e.getMessage());
            }
        }
    }

    /**
     * Advances the animation by one tick and draws the current frame.
     * Does nothing once the effect has finished.
     *
     * @param gc      the graphics context to draw onto
     * @param cameraX the current camera X offset (world → screen conversion)
     */
    public void draw(GraphicsContext gc, double cameraX) {
        if (finished) return;

        frameTick++;
        if (frameTick >= FRAME_DELAY) {
            frameTick = 0;
            currentFrame++;
        }
        if (currentFrame >= 5) {
            finished = true;
            return;
        }

        if (sharedFrames[currentFrame] != null) {
            double size = 100;
            gc.drawImage(sharedFrames[currentFrame],
                    (x - size / 2) - cameraX,
                    y - size / 2,
                    size, size);
        }
    }

    /**
     * Returns {@code true} after all five frames have been displayed.
     *
     * @return {@code true} when the animation is done
     */
    public boolean isFinished() { return finished; }
}