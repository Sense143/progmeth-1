package models.units;

import javafx.scene.image.Image;
import models.base.Tower;

import java.util.Objects;

public class CatTower extends Tower {

    private Image sprite;

    public CatTower() {
        super("Cat Tower", 2350, 2000);
        String imagePath = "/tower/Catbase.png";
        try {
            sprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
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
        return this.getX() - (this.getRenderWidth()/2);
    }
}
