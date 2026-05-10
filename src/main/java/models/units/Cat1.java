package models.units;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

public class Cat1 extends SingleTargetUnit{

    public Cat1() {
        super("Basic Cat", 2300, 100, 10, 1500, 50, -3);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/normCat/000_f_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Cat1 ไม่สำเร็จ: " + e.getMessage());
        }
    }

    public double getRenderWidth() {
        return 80;
    }

    public double getRenderHeight() {
        return 80;
    }

}
