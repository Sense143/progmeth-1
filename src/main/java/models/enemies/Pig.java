package models.enemies;

import javafx.scene.image.Image;
import models.base.AoeUnit;

import java.util.Objects;

public class Pig extends AoeUnit {

    public Pig() {
        super("Pig", 60, 5000, 200, 1500, 150, 1.5);
        try {
            // ท่าเดิน 3 รูป
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_3.png")));

            // ท่าโจมตี 3 รูป
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_a3.png")));

            // ท่ายืนรอคูลดาวน์ 1 รูป
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/enemy/pig/pig_idle.png")));

        } catch (Exception e) {
            System.out.println("โหลดรูป Pig ไม่สำเร็จ: " + e.getMessage());
        }
    }

    @Override
    public double getRenderWidth() {
        return 230;
    }

    @Override
    public double getRenderHeight() {
        return 200;
    }
}
