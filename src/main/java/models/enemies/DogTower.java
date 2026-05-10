package models.enemies;

import javafx.scene.image.Image;
import models.base.Tower;

import java.util.Objects;

public class DogTower extends Tower {

    private Image sprite;

    public DogTower(double hp, String imagePath) {
        super("Dog Tower", 50, hp);
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                sprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            }
        } catch (Exception e) {
            System.err.println("หาภาพป้อมศัตรูไม่เจอ: " + imagePath);
        }
    }

    @Override
    public Image getCurrentSprite() {
        return sprite;
    }

    @Override
    public double getRenderWidth() {
        return 120;
    }

    @Override
    public double getRenderHeight() {
        return 250;
    }

    @Override
    public double getRimPosition() {
        return this.getX() + (this.getRenderWidth() / 2);
    }
}
