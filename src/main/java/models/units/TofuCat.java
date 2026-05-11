package models.units;

import javafx.scene.image.Image;
import models.base.AoeUnit;

import java.util.Objects;

/**
 * A wide AoE cat unit shaped like a tofu block — slow but hits everything
 * directly in front of it.
 *
 * <p>Stats: 400 HP | 2 damage | 2000 ms cooldown | 40 px range | −2 speed.
 * Distance to TofuCat is measured from centre (not rim) so it acts as a
 * forward wall. This is handled specially in
 * {@link logic.BattleManager#findMultipleTargetsInRange}.
 */
public class TofuCat extends AoeUnit {

    /**
     * Constructs a TofuCat at the default spawn position (X = 2300) and loads
     * its walk, attack, idle, and knockback sprites.
     */
    public TofuCat() {
        super("Tofu Cat", 2300, 400, 2, 2000, 40, -2);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป TofuCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 150 pixels */
    public double getRenderWidth() {
        return 150;
    }

    /** @return 120 pixels */
    public double getRenderHeight() {
        return 120;
    }
}