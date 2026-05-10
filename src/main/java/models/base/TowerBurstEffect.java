package models.base;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Objects;

public class TowerBurstEffect {
    private double x, y;
    private static Image[] sharedFrames = new Image[5];
    private int currentFrame = 0;
    private int frameTick = 0;
    private static final int FRAME_DELAY = 4; // ~66ms per frame at 60fps
    private boolean finished = false;

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

    public boolean isFinished() { return finished; }
}