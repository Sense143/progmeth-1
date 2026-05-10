package models.base;

import interfaces.Attackable;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Objects;

public abstract class Unit implements Attackable {
    protected String name;
    protected double x;
    protected double hp;
    protected double attackDamage;
    protected double attackCooldown;
    protected double attackRange;
    protected double speed;
    protected boolean isAttacking;
    protected boolean isAoe;
    protected long lastAttackTime = 0;
    protected double attackRangeMin = 0;
    protected boolean hasDealtDamageThisAttack = false;

    protected ArrayList<Unit> currentTargets = new ArrayList<>();
    public boolean completelyDead = false; // 🌟 สำคัญ: ตัวแปรบอก Main ว่าลบศพทิ้งได้

    public enum State { WALK, ATTACK, IDLE, DEAD_KNOCKBACK, DEAD_SOUL }
    protected State currentState = State.WALK;

    protected Image[] walkSprites = new Image[3];
    protected Image[] attackSprites = new Image[3];
    protected Image idleSprite;
    protected Image knockbackSprite;
    protected static Image[] sharedSoulSprites = new Image[4];

    protected int currentFrame = 0;
    protected int animTick = 0;
    protected int animSpeed = 10;

    protected int deadFrameCounter = 0;
    protected double yOffset = 0;
    protected double soulTimer = 0;

    public Unit(String name, double x, double hp, double attackDamage, double attackCooldown, double attackRange, double speed) {
        this.name = name;
        this.x = x;
        this.hp = hp;
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.speed = speed;
        this.attackRange = attackRange;
        this.isAttacking = false;
        loadSharedSoulSprites();
    }

    @Override
    public void takeDamage(double damage) {
        if (this.hp > 0) {
            this.hp -= damage;
            this.hp = Math.max(this.hp, 0);
        }
    }

    public double getX() { return x; }
    public double getYOffset() { return yOffset; }
    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = hp; }
    public void setX(double x) { this.x = x; }
    public double getAttackRange() { return attackRange; }
    public void setAttackRange(double attackRange) { this.attackRange = attackRange; }
    public double getAttackRangeMin() { return attackRangeMin; }
    public double getSpeed() { return speed; }

    public abstract void update();

    protected void updateAnimation() {
        // ถ้าเลือดหมด และยังไม่ได้อยู่ในสถานะตาย ให้เริ่ม "กระเด็น" (Knockback)
        if (this.hp <= 0 && currentState != State.DEAD_KNOCKBACK && currentState != State.DEAD_SOUL) {
            setState(State.DEAD_KNOCKBACK);
            this.yOffset = 0; // รีเซ็ตตำแหน่ง Y
            this.deadFrameCounter = 0;
        }

        if (currentState == State.DEAD_KNOCKBACK) {
            animTick++;

            // 🔧 ปรับแต่งเวลาและระยะทางรวมตรงนี้
            int totalFrames = 45; // เวลาทั้งหมดที่ใช้กระเด็น (เฟรม)
            double totalKnockbackX = 150.0; // ระยะถอยหลังรวมทั้งหมด

            // 🔧 ปรับความสูงของการเด้งแต่ละครั้ง
            double heightBounce1 = 25.0; // ความสูงเด้งครั้งแรก
            double heightBounce2 = 15.0; // ความสูงเด้งครั้งที่สอง (ให้เตี้ยลงเพื่อความสมจริง)

            double direction = (this.speed > 0) ? -1 : 1;

            // 1. แกน X: ค่อยๆ ถอยหลังไปตลอดแอนิเมชัน
            this.x += direction * (totalKnockbackX / totalFrames);

            // 2. แกน Y: แบ่งการทำงานเป็น 2 ช่วง (เด้ง 2 รอบ)
            int halfFrames = totalFrames / 2; // แบ่งครึ่งเวลาให้แต่ละรอบ (รอบละ 20 เฟรม)

            if (animTick <= halfFrames) {
                // --- เด้งครั้งที่ 1 (เฟรม 1 ถึง 20) ---
                if (animTick <= halfFrames / 2) {
                    this.yOffset -= (heightBounce1 / (halfFrames / 2.0)); // ขาขึ้น
                } else {
                    this.yOffset += (heightBounce1 / (halfFrames / 2.0)); // ขาลง
                }
            } else {
                // --- เด้งครั้งที่ 2 (เฟรม 21 ถึง 40) ---
                int bounce2Tick = animTick - halfFrames; // รีเซ็ตตัวนับสำหรับรอบสอง
                if (bounce2Tick <= halfFrames / 2) {
                    this.yOffset -= (heightBounce2 / (halfFrames / 2.0)); // ขาขึ้น
                } else {
                    this.yOffset += (heightBounce2 / (halfFrames / 2.0)); // ขาลง
                }
            }

            // 3. เมื่อจบแอนิเมชัน (ตกถึงพื้นรอบที่สอง)
            if (animTick >= totalFrames) {
                this.yOffset = 0; // จับวางให้แตะพื้นพอดี
                animTick = 0;     // รีเซ็ตเฟรม
                setState(State.DEAD_SOUL); // เปลี่ยนเป็นวิญญาณ
            }
            return;
        }

        if (currentState == State.DEAD_SOUL) {
            this.yOffset -= 4; // วิญญาณลอยขึ้นเรื่อยๆ

            // สลับเฟรมวิญญาณ (ถ้ามี 4 รูป)
            animTick++;
            if (animTick >= animSpeed) {
                animTick = 0;
                currentFrame = (currentFrame + 1) % 4;
            }

            // ถ้าลอยพ้นขอบบนของจอ ให้ยืนยันว่าตายสนิท (เพื่อไห้ Main ลบทิ้ง)
            if (this.yOffset <= -400) {
                this.completelyDead = true;
            }
            return;
        }

        // --- 3. แอนิเมชันปกติ (เดิน ตี รอ) ---
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            currentFrame++;

            if (currentState == State.WALK) {
                if (currentFrame > 2) currentFrame = 0;
            }
            else if (currentState == State.ATTACK) {
                // ดาเมจออกที่เฟรม 3 (index 2) และต้องยังไม่ได้ทำดาเมจในรอบนี้
                if (currentFrame == 2 && !hasDealtDamageThisAttack) {
                    processAttackDamage();
                }
                // ถ้าจบแอนิเมชันโจมตีแล้ว (พ้นเฟรม 3)
                if (currentFrame > 2) {
                    currentFrame = 0;
                    hasDealtDamageThisAttack = false;
                    setState(State.IDLE); // กลับไปยืนรอ
                }
            }
            else if (currentState == State.IDLE) {
                currentFrame = 0;
            }
        }
    }

    public Image getCurrentSprite() {
        if (currentState == State.DEAD_KNOCKBACK && knockbackSprite != null) return knockbackSprite;

        // 🌟 เปลี่ยนมาดึงจาก sharedSoulSprites แทน
        if (currentState == State.DEAD_SOUL && sharedSoulSprites[currentFrame] != null) return sharedSoulSprites[currentFrame];

        if (currentState == State.WALK && walkSprites[currentFrame] != null) return walkSprites[currentFrame];
        if (currentState == State.ATTACK && attackSprites[currentFrame] != null) return attackSprites[currentFrame];
        if (currentState == State.IDLE && idleSprite != null) return idleSprite;
        return null;
    }

    public void setState(State newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.currentFrame = 0;
            this.animTick = 0;
        }
    }

    public abstract double getRenderWidth();
    public abstract double getRenderHeight();

    public double getRimPosition(){
        if(this.speed > 0) return this.x + (this.getRenderWidth())/2;
        else return this.x - (this.getRenderWidth())/2;
    }

    protected void processAttackDamage() {
        if (currentTargets != null && !currentTargets.isEmpty()) {
            for (Unit target : currentTargets) {
                target.takeDamage(this.attackDamage);
            }
        }
        hasDealtDamageThisAttack = true; // ล็อคไม่ให้ดาเมจออกซ้ำจนกว่าจะง้างตีใหม่
    }

    protected void loadSharedSoulSprites() {
        // เช็คว่าถ้ายังไม่เคยโหลด (เป็น null) ค่อยโหลด
        if (sharedSoulSprites[0] == null) {
            try {
                sharedSoulSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_1.png")));
                sharedSoulSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_2.png")));
                sharedSoulSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_3.png")));
                sharedSoulSprites[3] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_4.png")));
            } catch (Exception e) {
                System.out.println("โหลดรูปวิญญาณไม่สำเร็จ: " + e.getMessage());
            }
        }
    }

    // 🌟 เพิ่มเมธอดนี้ลงไปใน Unit.java
    public boolean isDeadSoul() {
        return this.currentState == State.DEAD_SOUL;
    }
}