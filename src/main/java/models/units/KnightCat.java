package models.units;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

public class KnightCat extends SingleTargetUnit {

    public KnightCat() {
        super("Knight Cat", 2300, 200, 15, 900, 90, -3.5);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/knightCat/002_c_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป KnightCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    public double getRenderWidth() {
        return 120;
    }

    public double getRenderHeight() {
        return 100;
    }
}
