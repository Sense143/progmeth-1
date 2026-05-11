package models.enemies;

import javafx.scene.image.Image;
import models.base.AoeUnit;

import java.util.Objects;

/**
 * A powerful AoE enemy unit — very high HP, high damage, but slow.
 *
 * <p>Stats: 5000 HP | 200 damage | 1500 ms cooldown | 150 px range | +0.5 speed.
 * Used as a boss / mini-boss in later stages.
 */
public class Pig extends AoeUnit {

    /**
     * Constructs a Pig at the default enemy spawn position (X = 60) and
     * loads its walk, attack, idle, and knockback sprites.
     */
    public Pig() {
        super("Pig", 60, 5000, 200, 1500, 150, 0.5);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Pig ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 230 pixels */
    @Override
    public double getRenderWidth() {
        return 230;
    }

    /** @return 200 pixels */
    @Override
    public double getRenderHeight() {
        return 200;
    }
}