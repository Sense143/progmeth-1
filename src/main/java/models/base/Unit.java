package models.base;

import interfaces.Attackable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

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

    // 🌟 สร้างสถานะของตัวละคร (เดิน, โจมตี, รอคูลดาวน์)
    public enum State { WALK, ATTACK, IDLE }
    protected State currentState = State.WALK;

    // 🌟 ตัวแปรเก็บรูปภาพ (3 รูปเดิน, 3 รูปตี, 1 รูปรอคูลดาวน์)
    protected Image[] walkSprites = new Image[3];
    protected Image[] attackSprites = new Image[3];
    protected Image idleSprite;

    // 🌟 ตัวแปรสำหรับควบคุมความสมูทของแอนิเมชัน
    protected int currentFrame = 0;
    protected int animTick = 0;
    protected int animSpeed = 10; // เปลี่ยนรูปทุกๆ 10 รอบลูป (60FPS)

    public Unit(String name, double x, double hp, double attackDamage, double attackCooldown, double attackRange, double speed) {
        this.name = name;
        this.x = x;
        this.hp = hp;
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.speed = speed;
        this.attackRange = attackRange;
        this.isAttacking = false;
    }

    @Override
    public void takeDamage(double damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    public double getX() { return x; }
    public double getHp() { return hp; }
    public void setHp(double hp) { this.hp = hp; }
    public void setX(double x) { this.x = x; }
    public double getAttackRange() { return attackRange; }
    public void setAttackRange(double attackRange) { this.attackRange = attackRange; }

    public abstract void update();

    // 🌟 อัปเดตเฟรมภาพ (สลับรูป)
    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            currentFrame++;

            // ถ้าเป็นสถานะเดิน หรือ โจมตี ให้วนลูป 3 รูป (index 0, 1, 2)
            if (currentState == State.WALK || currentState == State.ATTACK) {
                if (currentFrame >= 3) {
                    currentFrame = 0;
                }
            }
        }
    }

    // 🌟 ดึงภาพสถานะปัจจุบันไปวาด
    public Image getCurrentSprite() {
        if (currentState == State.WALK && walkSprites[currentFrame] != null) {
            return walkSprites[currentFrame];
        } else if (currentState == State.ATTACK && attackSprites[currentFrame] != null) {
            return attackSprites[currentFrame];
        } else if (currentState == State.IDLE && idleSprite != null) {
            return idleSprite;
        }
        return null;
    }

    // 🌟 เปลี่ยนสถานะการกระทำ
    public void setState(State newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.currentFrame = 0; // เปลี่ยนท่าปุ๊บ ให้เริ่มรูปที่ 1 ใหม่
            this.animTick = 0;
        }
    }

    // (เมธอด render สี่เหลี่ยมแบบเดิม เก็บไว้เผื่อฉุกเฉิน)
    public void render(GraphicsContext gc) {
        if (this.isAoe) gc.setFill(Color.ORANGE);
        else gc.setFill(Color.BLUE);
        gc.fillRect(this.x, 350, 50, 50);
    }
}