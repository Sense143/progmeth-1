package models.enemies;

import javafx.scene.image.Image;
import models.base.SingleTargetUnit;

import java.util.Objects;

public class Dog1 extends SingleTargetUnit {

    public Dog1(){
        super("Basic Dog", 60, 150, 4, 2000, 50, 3);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/dog/dog_idle.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Dog1 ไม่สำเร็จ: " + e.getMessage());
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
