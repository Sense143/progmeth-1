package models.units;

import javafx.scene.image.Image;
import models.base.AoeUnit;

import java.util.Objects;

public class TofuCat extends AoeUnit {

    public TofuCat() {
        super("Tofu Cat", 2300, 400, 2, 2000, 40, -2);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/tofuCat/001_c_dead.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป TofuCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    public double getRenderWidth() {
        return 150;
    }

    public double getRenderHeight() {
        return 120;
    }
}
