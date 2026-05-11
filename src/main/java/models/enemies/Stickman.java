package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

/**
 * A fast single-target enemy with low HP but high damage — a glass-cannon threat.
 *
 * <p>Stats: 150 HP | 20 damage | 900 ms cooldown | 100 px range | +4 speed.
 *
 * <p>Distance to Stickman is measured from centre rather than rim when the
 * attacker is {@link models.units.UFOCat}, because Stickman's slim sprite
 * makes rim-based detection unreliable. This is handled in
 * {@link logic.BattleManager#findMultipleTargetsInRange}.
 */
public class Stickman extends SingleTargetUnit {

    /**
     * Constructs a Stickman at the default enemy spawn position (X = 60) and
     * loads its walk, attack, idle, and knockback sprites.
     */
    public Stickman() {
        super("Stickman", 60, 150, 20, 900, 100, 4);
        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_3.png")));

            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_a3.png")));

            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Stickman ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /** @return 85 pixels */
    @Override
    public double getRenderWidth() {
        return 85;
    }

    /** @return 60 pixels */
    @Override
    public double getRenderHeight() {
        return 60;
    }
}