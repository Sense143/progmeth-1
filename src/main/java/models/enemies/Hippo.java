package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

/**
 * A heavy single-target enemy — massive HP, high damage, slow attack and
 * movement. Acts as a stage mini-boss.
 *
 * <p>Stats: 1200 HP | 50 damage | 4000 ms cooldown | 120 px range | +2 speed.
 */
public class Hippo extends SingleTargetUnit {

    /**
     * Constructs a Hippo at the default enemy spawn position (X = 60) and
     * loads its walk, attack, idle, and knockback sprites.
     */
    public Hippo() {
        super("Hippo", 60, 1200, 50, 4000, 120, 2);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Hippo ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 144 pixels */
    @Override
    public double getRenderWidth() {
        return 144;
    }

    /** @return 120 pixels */
    @Override
    public double getRenderHeight() {
        return 120;
    }
}