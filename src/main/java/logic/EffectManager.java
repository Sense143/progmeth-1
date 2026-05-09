// สร้างไฟล์ใหม่: EffectManager.java ใน package logic
package logic;

import models.base.ExplosionEffect;
import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;

public class EffectManager {
    private static EffectManager instance;
    private ArrayList<ExplosionEffect> activeEffects = new ArrayList<>();

    private EffectManager() {} // Singleton

    public static EffectManager getInstance() {
        if(instance == null) instance = new EffectManager();
        return instance;
    }

    // 🌟 3. สั่งเพิ่มระเบิดใหม่ ณ พิกัด x, y
    public void spawnExplosion(double x, double y) {
        activeEffects.add(new ExplosionEffect(x, y));
    }

    // 🌟 4. สั่งวาดทุกเอฟเฟกต์ที่กำลังทำงานลง Canvas
    public void drawAll(GraphicsContext gc, double cameraX) {
        // วาดและลบเอฟเฟกต์ที่เล่นจบแล้ว
        activeEffects.removeIf(ExplosionEffect::isFinished);
        for (ExplosionEffect sfx : activeEffects) {
            sfx.draw(gc, cameraX);
        }
    }

    // อย่าลืม clear ตอนเริ่มด่านใหม่ด้วยนะครับ
    public void clearAll() {
        activeEffects.clear();
    }
}