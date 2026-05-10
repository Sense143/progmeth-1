package models.units;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

public class FishCat extends SingleTargetUnit {

    public FishCat() {
        super("Fish Cat", 2300, 700, 120, 2000, 140, -2.5);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/fishCat/006_f_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป FishCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    public double getRenderWidth() {
        return 150;
    }

    public double getRenderHeight() {
        return 150;
    }
}
