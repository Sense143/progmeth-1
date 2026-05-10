package models.stages;

import javafx.scene.image.Image;
import models.base.Unit;
import logic.BattleManager;

import java.util.ArrayList;
import java.util.Objects;

public abstract class GameStage {
    protected String stageName;
    protected double enemyTowerHp;
    protected Image backgroundImage;

    // เพิ่มตัวนับเฟรม (60 เฟรม ≈ 1 วินาที)
    protected int frameCount = 0;

    public GameStage(String name, double towerHp, String imagePath) {
        this.stageName = name;
        this.enemyTowerHp = towerHp;
        // ป้องกัน Error กรณีลืมใส่รูป หรือพิมพ์ Path ผิด
        try {
            this.backgroundImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            System.err.println("หาภาพไม่เจอที่ Path: " + imagePath);
        }
    }

    public String getStageName() { return stageName; }
    public double getEnemyTowerHp() { return enemyTowerHp; }
    public Image getBackgroundImage() { return backgroundImage; }

    // Method นี้จะถูกเรียกจาก Main ทุกๆ รอบของ Game Loop
    public void updateStage(ArrayList<Unit> units, double currentHp) {
        frameCount++; // นับเวลาเพิ่มขึ้นเรื่อยๆ
        spawnEnemyLogic(units); // เรียกใช้ลอจิกของแต่ละด่าน
        this.enemyTowerHp = currentHp;
    }

    // abstract รับ parameter เป็น units เพื่อให้ด่านเสกตัวละครยัดใส่จอได้
    protected abstract void spawnEnemyLogic(ArrayList<Unit> units);
    public abstract String getEnemyTowerImagePath();
}