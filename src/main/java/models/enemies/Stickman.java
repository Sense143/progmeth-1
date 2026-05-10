package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

public class Stickman extends SingleTargetUnit {

    public Stickman() {
        super("Stickman", 60, 150, 20, 900, 100, 4);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/stickman/stick_idle.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Stickman ไม่สำเร็จ: " + e.getMessage());
        }
    }

    @Override
    public double getRenderWidth() {
        return 85;
    }

    @Override
    public double getRenderHeight() {
        return 60;
    }
}
