package models.base;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import logic.BattleManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class CannonWave {
    private double x;
    private final double y = 410;
    private final double STEP_SIZE = 70; // 🌟 ระยะที่ขยับในแต่ละก้าว (2300 -> 2230 คือ 70)
    private final double WAVE_WIDTH = 100; // 🌟 รัศมีการเช็คดาเมจรอบจุดระเบิด
    private double damage = 500;

    private int currentFrame = 0;
    private int frameCounter = 0;
    private final int ANIMATION_DELAY = 5; // 🌟 ความเร็วการเปลี่ยนรูป (ยิ่งน้อยยิ่งเร็ว)

    private Image[] frames = new Image[4];
    private boolean active = true;
    private HashSet<Unit> hitUnits = new HashSet<>();

    public CannonWave(double startX) {
        this.x = startX; // เริ่มที่ 2300
        try {
            for (int i = 0; i < 4; i++) {
                frames[i] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tower/tower_cannon_" + (i + 1) + ".png")));
            }
        } catch (Exception e) {
            System.err.println("โหลดรูป CannonWave ไม่สำเร็จ");
        }
    }

    public void update() {
        if (!active) return;

        // 1. จัดการจังหวะแอนิเมชัน
        int FAST_DELAY = 2; // ความไว (1 = ไวสุด, 2 = กำลังดี)
        frameCounter++;

        if (frameCounter >= FAST_DELAY) {
            frameCounter = 0;
            currentFrame++;

            // เมื่อเล่นครบ 4 รูปในจุดนี้แล้วค่อยขยับตำแหน่ง
            if (currentFrame >= 4) {
                currentFrame = 0;
                x -= STEP_SIZE;   // ขยับไปก้าวถัดไป
                hitUnits.clear(); // เคลียร์เพื่อให้การระเบิดก้าวใหม่เริ่มนับหนึ่งการทำดาเมจใหม่
            }
        }

        // 2. 🌟 เช็คดาเมจ "ทุกเฟรม" (ไม่มี if currentFrame แล้ว)
        // การระเบิดจะทำงานเหมือน "ม่านพลัง" ที่กางแช่ไว้จนกว่าจะเล่นครบ 4 รูป
        double rangeStart = x - WAVE_WIDTH;
        double rangeEnd = x;

        ArrayList<Unit> enemies = BattleManager.getInstance().getEveryTargetOnBoard();
        for (Unit enemy : enemies) {
            if(enemy instanceof Tower) continue;
            // กรองเฉพาะตัวที่ยังมีชีวิตและไม่ใช่สถานะวิญญาณ
            if (enemy.getHp() <= 0) continue;

            double enemyX = enemy.getX();

            // เช็คว่าศัตรูอยู่ในรัศมีระเบิด ณ ก้าวนั้นๆ หรือไม่
            if (enemyX >= rangeStart && enemyX <= rangeEnd) {
                // hitUnits จะช่วยให้ศัตรูโดนดาเมจแค่ "ครั้งเดียวต่อหนึ่งก้าวระเบิด"
                // ไม่เช่นนั้นมันจะโดนดาเมจทุกๆ 16ms ซึ่งแรงเกินไป
                if (!hitUnits.contains(enemy)) {
                    enemy.takeDamage(damage);
                    hitUnits.add(enemy);

                    // ใส่ System.out เพื่อเช็คใน Console ว่าโดนจริงไหม
                    System.out.println("BOOM! Hit: " + enemy.getClass().getSimpleName() + " at X: " + x);
                }
            }
        }

        // 3. เงื่อนไขสิ้นสุด
        if (x < -200) {
            active = false;
            hitUnits.clear();
        }
    }

    public void draw(GraphicsContext gc, double cameraX) {
        if (!active || frames[currentFrame] == null) return;

        double drawX = x - cameraX;
        // วาดรูปแอนิเมชันระเบิด ณ ตำแหน่ง x ปัจจุบัน
        gc.drawImage(frames[currentFrame], drawX, y - 300, 180, 300);
    }

    public boolean isActive() { return active; }
}