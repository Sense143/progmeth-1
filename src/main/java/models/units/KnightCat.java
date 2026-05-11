package models.units;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

/**
 * A fast single-target cat unit with moderate HP and range.
 *
 * <p>Stats: 200 HP | 15 damage | 900 ms cooldown | 90 px range | −3.5 speed.
 */
public class KnightCat extends SingleTargetUnit {

    /**
     * Constructs a KnightCat at the default spawn position (X = 2300) and
     * loads its walk, attack, idle, and knockback sprites.
     */
    public KnightCat() {
        super("Knight Cat", 2300, 200, 15, 900, 90, -3.5);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป KnightCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 120 pixels */
    public double getRenderWidth() {
        return 120;
    }

    /** @return 100 pixels */
    public double getRenderHeight() {
        return 100;
    }
}