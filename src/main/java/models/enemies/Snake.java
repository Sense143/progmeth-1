package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

public class Snake extends SingleTargetUnit {

    public Snake() {
        super("Snake", 30, 200, 10, 1500, 100, 4);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/snake/snake_idle.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Snake ไม่สำเร็จ: " + e.getMessage());
        }
    }

    @Override
    public double getRenderWidth() {
        return 80;
    }

    @Override
    public double getRenderHeight() {
        return 80;
    }
}
