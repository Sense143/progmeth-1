package models.base;

import interfaces.Attackable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

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

    @Override
    public void takeDamage(double damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    public double getX() {
        return x;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

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

    public abstract void update();

    public void render(GraphicsContext gc) {
        // ถ้าคุณมีไฟล์รูปภาพ
        // gc.drawImage(this.image, this.x, 350, 50, 50);

        // หรือถ้าจะวาดเป็นสี่เหลี่ยมตามที่คุณต้องการ
        if (this.isAoe) {
            gc.setFill(Color.ORANGE);
        } else {
            gc.setFill(Color.BLUE);
        }
        gc.fillRect(this.x, 350, 50, 50);
    }

}
