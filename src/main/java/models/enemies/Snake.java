package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

/**
 * A fast single-target enemy with medium HP.
 *
 * <p>Stats: 200 HP | 10 damage | 1500 ms cooldown | 70 px range | +4 speed.
 */
public class Snake extends SingleTargetUnit {

    /**
     * Constructs a Snake at the default enemy spawn position (X = 60) and
     * loads its walk, attack, idle, and knockback sprites.
     */
    public Snake() {
        super("Snake", 60, 200, 10, 1500, 70, 4);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Snake ไม่สำเร็จ: " + e.getMessage());
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