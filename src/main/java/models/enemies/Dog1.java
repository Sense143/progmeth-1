package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

/**
 * The basic enemy dog unit — low HP, light damage, single-target.
 *
 * <p>Stats: 150 HP | 4 damage | 2000 ms cooldown | 50 px range | +3 speed (moves right).
 */
public class Dog1 extends SingleTargetUnit {

    /**
     * Constructs a Dog1 at the default enemy spawn position (X = 60) and
     * loads its walk, attack, idle, and knockback sprites.
     */
    public Dog1(){
        super("Basic Dog", 60, 150, 4, 2000, 50, 3);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Dog1 ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 80 pixels */
    @Override
    public double getRenderWidth() {
        return 80;
    }

    /** @return 80 pixels */
    @Override
    public double getRenderHeight() {
        return 80;
    }
}