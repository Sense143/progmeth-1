package models.units;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

/**
 * The basic player cat unit — cheap, fast cooldown, and single-target.
 *
 * <p>Stats: 100 HP | 10 damage | 1500 ms cooldown | 50 px range | −3 speed.
 */
public class Cat1 extends SingleTargetUnit{

    /**
     * Constructs a Cat1 at the default spawn position (X = 2300) and loads
     * its walk, attack, idle, and knockback sprites.
     */
    public Cat1() {
        super("Basic Cat", 2300, 100, 10, 1500, 50, -3);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Cat1 ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 80 pixels */
    public double getRenderWidth() {
        return 80;
    }

    /** @return 80 pixels */
    public double getRenderHeight() {
        return 80;
    }

}