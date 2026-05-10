package models.units;

import javafx.scene.image.Image;
import logic.BattleManager;
import models.base.AoeUnit;
import models.base.Unit;
import models.enemies.Stickman;

import java.util.ArrayList;
import java.util.Objects;

public class UFOCat extends AoeUnit {

    private boolean isWaveActive = false;
    private int currentWaveStep = 0;
    private long lastWaveTime = 0;
    private final double WAVE_WIDTH = 70; // ความกว้างแต่ละลูกคลื่น
    private final double MAX_WAVE_RANGE = 210; // ระยะรวมทั้งหมด

    public UFOCat() {
        super("UFO Cat", 2300, 300, 150, 3000, 210, -2.5);
        this.attackRangeMin = 0;

        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_3.png")));
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_a3.png")));
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_dead.png")));
        } catch (Exception e) {
            System.out.println("โหลดรูป UFOCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    @Override
    public void update() {
        updateAnimation();
        if(this.hp <= 0) return;

        long currentTime = System.currentTimeMillis();

        // --- โหมดกำลังปล่อยคลื่นระเบิด ---
        if (isWaveActive) {
            setState(State.ATTACK);

            if (currentTime - lastWaveTime >= 150) { // ดีเลย์ระหว่างระเบิดแต่ละลูก
                executeWaveStep(currentWaveStep);
                lastWaveTime = currentTime;
                currentWaveStep++;

                // ถ้าปล่อยครบ 3 ลูกแล้ว ให้จบเทิร์น
                if (currentWaveStep >= 3) {
                    isWaveActive = false;
                    currentWaveStep = 0;
                }
            }
            return;
        }

        // --- โหมดปกติ: เดินค้นหาศัตรู ---
        this.attackRange = MAX_WAVE_RANGE;
        this.attackRangeMin = 0;

        ArrayList<Unit> targets = BattleManager.getInstance().findMultipleTargetsInRange(this);

        if(targets != null && !targets.isEmpty()){
            isAttacking = true;
            long timeSinceLastAttack = currentTime - lastAttackTime;

            if (timeSinceLastAttack < 500) {
                setState(State.ATTACK);
            } else if (timeSinceLastAttack >= attackCooldown) {
                // ถึงเวลาโจมตี -> เริ่มเข้าโหมดปล่อย Wave!
                isWaveActive = true;
                currentWaveStep = 0;
                lastWaveTime = currentTime;
                lastAttackTime = currentTime;

                executeWaveStep(currentWaveStep); // ยิงลูกแรกทันที!
                currentWaveStep++;
            } else {
                setState(State.IDLE);
            }
        } else {
            isAttacking = false;
            setState(State.WALK);
            this.move();
        }
    }

    private void executeWaveStep(int cnt) {
        // 1. ดึงเป้าหมายทั้งหมดในระยะ 210 มาเลย (เพื่อให้ BattleManager มองเห็นศัตรูแน่นอน)
        this.attackRangeMin = 0;
        this.attackRange = MAX_WAVE_RANGE;
        ArrayList<Unit> allTargets = BattleManager.getInstance().findMultipleTargetsInRange(this);

        ArrayList<Unit> targetsToHitNow = new ArrayList<>();
        double currentMaxRange = (currentWaveStep + 1) * WAVE_WIDTH; // 70, 140, 210

        // 2. คัดกรองศัตรูด้วยตัวเอง
        if (allTargets != null) {
            for (Unit t : allTargets) {
                // คำนวณระยะห่าง
                double distance = Math.abs(this.getX() - t.getRimPosition());
                if(t instanceof Stickman){
                    distance = Math.abs(this.getX() - t.getX());
                }

                // ถ้าศัตรูอยู่ในวงของคลื่นลูกนี้ และ "ยังไม่เคยโดนระเบิดลูกก่อนหน้า"
                if (distance <= currentMaxRange && distance >= currentWaveStep * WAVE_WIDTH) {
                    targetsToHitNow.add(t);
                }
            }
        }

        // 🌟 3. โซนทำดาเมจ! (แก้ไขตรงนี้ให้ตีเข้าชัวร์ๆ)
        if (!targetsToHitNow.isEmpty()) {

            // ลองเรียกของเดิมดูก่อน
            // this.Attack(targetsToHitNow);

            // ⚠️ ถ้าระบบเดิมด้านบนไม่ทำงาน ให้ใช้การ "ลดเลือดตรงๆ" แบบนี้แทนครับ:
            for (Unit t : targetsToHitNow) {
                // สมมติว่าพลังโจมตีคือ 300 คุณสามารถใช้คำสั่งลด HP ของเกมคุณได้เลย
                // เช่น t.takeDamage(300);
                // หรือใช้ Getter/Setter แบบนี้:
                t.setHp(t.getHp() - this.attackDamage);
            }
        }

        // 4. วาด Effect ระเบิด
        double direction = (this.getSpeed() < 0) ? -1 : 1;
        double waveCenterDistance = (currentWaveStep * WAVE_WIDTH) + (WAVE_WIDTH / 2);

        double ufoCenterX = this.getX() + (this.getRenderWidth() / 2);
        double spawnX = ufoCenterX + (direction * waveCenterDistance);

        logic.EffectManager.getInstance().spawnExplosion(spawnX, 410);
    }

    public double getRenderWidth() { return 100; }
    public double getRenderHeight() { return 120; }
}