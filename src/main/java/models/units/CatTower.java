package models.units;

import javafx.scene.image.Image;
import models.base.Tower;

import java.util.Objects;

/**
 * The player's home base tower, positioned at the right side of the battlefield.
 *
 * <p>Stats: 2000 HP | X = 2350.
 * The tower's rim (the left edge that enemies attack) is its centre minus half
 * its render width.
 */
public class CatTower extends Tower {

    private Image sprite;

    /**
     * Constructs the player tower and loads its sprite from resources.
     */
    public CatTower() {
        super("Cat Tower", 2350, 2000);
        String imagePath = "/tower/Catbase.png";
        try {
            sprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("หาภาพป้อมศัตรูไม่เจอ: " + imagePath);
        }
    }

    /** @return the tower sprite */
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
     * Returns the left edge of the tower — the point enemies advance toward.
     * Overrides the default rim calculation because the cat tower faces left.
     *
     * @return world X of the tower's left edge
     */
    @Override
    public double getRimPosition() {
        return this.getX() - (this.getRenderWidth()/2);
    }
}