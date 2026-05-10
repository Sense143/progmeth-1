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
    protected long timeOfDeath = 0;
    protected double attackRangeMin = 0;
    protected boolean hasDealtDamageThisAttack = false;
    protected Unit currentTarget;

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

    public double getAttackRangeMin() {
        return attackRangeMin;
    }

    public double getSpeed() {
        return speed;
    }

    public abstract void update();

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            currentFrame++;

            if (currentState == State.WALK) {
                // ถ้าเดินอยู่ ให้วนลูปภาพ 0, 1, 2
                if (currentFrame > 2) {
                    currentFrame = 0;
                }
            }
            else if (currentState == State.ATTACK) {
                // ถ้าโจมตีอยู่ และถึงเฟรมที่ 2 (รูปที่ 3 จังหวะฟันพอดี)
                if (currentFrame == 2 && currentTarget != null) {
                    processAttackDamage(currentTarget);
                }

                // พอเล่นแอนิเมชันโจมตีเสร็จ (currentFrame ทะลุ 2)
                if (currentFrame > 2) {
                    currentFrame = 0;
                    hasDealtDamageThisAttack = false; // ปลดล็อคดาเมจสำหรับรอบหน้า
                    setState(State.IDLE); // ตีเสร็จให้กลับไปยืนรอคูลดาวน์ก่อน
                }
            }
            else if (currentState == State.IDLE) {
                // ยืนเฉยๆ ไม่ต้องขยับเฟรม
                currentFrame = 0;
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


    public abstract double getRenderWidth();
    public abstract double getRenderHeight();

    public double getRimPosition(){
        if(this.speed > 0) return this.x + (this.getRenderWidth())/2;
        else return this.x - (this.getRenderWidth())/2;
    }

    protected void processAttackDamage(Unit target) {
        // currentFrame == 2 คือรูปที่ 3 ของ Array ครับ
        if (this.currentFrame == 2 && !hasDealtDamageThisAttack) {
            target.takeDamage(this.attackDamage);
            hasDealtDamageThisAttack = true;
            System.out.println(this.name + " ฟันโดนที่เฟรมภาพที่ 3!");
        }
    }
}