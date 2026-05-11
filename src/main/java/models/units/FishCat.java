package models.units;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

/**
 * A tanky single-target cat unit with long range and high damage.
 *
 * <p>Stats: 700 HP | 120 damage | 2000 ms cooldown | 140 px range | −2.5 speed.
 */
public class FishCat extends SingleTargetUnit {

    /**
     * Constructs a FishCat at the default spawn position (X = 2300) and loads
     * its walk, attack, idle, and knockback sprites.
     */
    public FishCat() {
        super("Fish Cat", 2300, 700, 120, 2000, 140, -2.5);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป FishCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 150 pixels */
    public double getRenderWidth() {
        return 150;
    }

    /** @return 150 pixels */
    public double getRenderHeight() {
        return 150;
    }
}