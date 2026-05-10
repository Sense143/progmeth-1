package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

public class Hippo extends SingleTargetUnit {

    public Hippo() {
        super("Hippo", 60, 1200, 50, 4000, 120, 2);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/hippo/hippo_idle.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Hippo ไม่สำเร็จ: " + e.getMessage());
        }
    }

    @Override
    public double getRenderWidth() {
        return 144;
    }

    @Override
    public double getRenderHeight() {
        return 120;
    }
}
