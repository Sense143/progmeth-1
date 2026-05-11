package logic;

import models.base.ExplosionEffect;
import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;

/**
 * Singleton that manages all active visual effects on the battlefield.
 *
 * <p>Effects are spawned by units (e.g., UFOCat wave explosions) and
 * automatically removed once their animation finishes. The manager is
 * driven each frame by {@link #drawAll}.
 */
public class EffectManager {
    private static EffectManager instance;
    private ArrayList<ExplosionEffect> activeEffects = new ArrayList<>();

    private EffectManager() {}

    /**
     * Returns the shared singleton instance, creating it on first call.
     *
     * @return the global {@code EffectManager}
     */
    public static EffectManager getInstance() {
        if(instance == null) instance = new EffectManager();
        return instance;
    }

    /**
     * Spawns a new explosion effect centred at the given world coordinates.
     *
     * @param x world X position of the explosion centre
     * @param y world Y position of the explosion centre
     */
    public void spawnExplosion(double x, double y) {
        activeEffects.add(new ExplosionEffect(x, y));
    }

    /**
     * Updates and draws all active effects onto the canvas, then removes any
     * that have finished playing.
     *
     * @param gc      the graphics context to draw onto
     * @param cameraX the current camera X offset (world → screen conversion)
     */
    public void drawAll(GraphicsContext gc, double cameraX) {
        activeEffects.removeIf(ExplosionEffect::isFinished);
        for (ExplosionEffect sfx : activeEffects) {
            sfx.draw(gc, cameraX);
        }
    }
}