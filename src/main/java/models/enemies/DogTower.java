package models.enemies;

import javafx.scene.image.Image;
import models.base.Tower;

import java.util.Objects;

/**
 * The enemy home base tower, positioned at the left side of the battlefield.
 *
 * <p>HP and sprite are supplied per-stage so each stage can configure its
 * own tower appearance and difficulty. The tower's rim (right edge that cat
 * units attack toward) is its centre plus half its render width.
 */
public class DogTower extends Tower {

    private Image sprite;

    /**
     * Constructs a DogTower at the default enemy-base position (X = 50).
     *
     * @param hp        starting (and maximum) hit points for this stage
     * @param imagePath classpath resource path to the tower sprite, or an
     *                  empty string to leave the sprite blank
     */
    public DogTower(double hp, String imagePath) {
        super("Dog Tower", 50, hp);
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                sprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            }
        } catch (Exception e) {
            System.err.println("หาภาพป้อมศัตรูไม่เจอ: " + imagePath);
        }
    }

    /** @return the tower sprite (may be {@code null} if no path was given) */
    @Override
    public Image getCurrentSprite() {
        return sprite;
    }

    /** @return 120 pixels */
    @Override
    public double getRenderWidth() {
        return 120;
    }

    /** @return 250 pixels */
    @Override
    public double getRenderHeight() {
        return 250;
    }

    /**
     * Returns the right edge of the tower — the point cat units advance toward.
     * Overrides the default rim calculation because the dog tower faces right.
     *
     * @return world X of the tower's right edge
     */
    @Override
    public double getRimPosition() {
        return this.getX() + (this.getRenderWidth() / 2);
    }
}