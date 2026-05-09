// สร้างไฟล์ใหม่: ExplosionEffect.java ใน package models.base หรือ effects ก็ได้
package models.base;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.util.Objects;

public class ExplosionEffect {
    private double x, y;
    private Image[] frames = new Image[5]; // เก็บรุป 5 ภาพ
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private int frameDelay = 50; // ดีเลย์ระหว่างภาพ (50ms = 0.05วิ)
    private boolean finished = false; // บอกว่าเอฟเฟกต์จบหรือยัง

    public ExplosionEffect(double x, double y) {
        this.x = x;
        this.y = y;
        this.lastFrameTime = System.currentTimeMillis();

        // 🌟 1. โหลดรูป 5 ภาพของคุณเตรียมไว้
        try {
            for(int i=0; i<5; i++) {
                // สมมติชื่อไฟล์: bomb_1.png, bomb_2.png, ...
                frames[i] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_e" + (i+1) + ".png")));
            }
        } catch (Exception e) {
            System.err.println("โหลดรูป爆発ไม่สำเร็จ: " + e.getMessage());
        }
    }

    public void draw(GraphicsContext gc, double cameraX) {
        if(finished) return;

        // เช็คเวลา (เหมือนเดิม)
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastFrameTime >= frameDelay) {
            currentFrame++;
            lastFrameTime = currentTime;
        }

        if(currentFrame >= 5) {
            finished = true;
            return;
        }

        // วาดรูปลงบนหน้าจอ
        if(frames[currentFrame] != null) {

            double sfxWidth = 80;  // ความกว้าง
            double sfxHeight = 150; // ความสูง

            // 🌟 1. กำหนดระดับพื้นให้ตรงกับ Main.java
            double groundY = 410;

            // 🌟 2. คำนวณแกน Y ใหม่: เอาพื้นตั้ง ลบด้วยความสูงของรูป (เพื่อให้ขอบล่างติดพื้นพอดี)
            double drawY = groundY - sfxHeight;

            gc.drawImage(
                    frames[currentFrame],
                    (x - sfxWidth/2) - cameraX, // แกน X (ซ้าย-ขวา) จัดกึ่งกลางตัวศัตรูเหมือนเดิม
                    drawY,                      // แกน Y (บน-ล่าง) 🌟 ใช้ค่าที่คำนวณใหม่ให้ติดพื้น
                    sfxWidth,
                    sfxHeight
            );
        }
    }

    public boolean isFinished() {
        return finished;
    }
}