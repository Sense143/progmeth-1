package models.base;

import interfaces.Attackable;
import interfaces.Moveable;
import logic.BattleManager;

import java.util.ArrayList;

public class AoeUnit extends Unit implements Attackable, Moveable {

    public AoeUnit(String name, double x, double hp, double attackDamage, double attackCooldown, double attackRange, double speed) {
        super(name, x, hp, attackDamage, attackCooldown, attackRange, speed);
        this.isAoe = true;
    }

    @Override
    public void takeDamage(double damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    @Override
    public void Attack(ArrayList<Unit> targets) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAttackTime >= attackCooldown){
            for(Unit target : targets){
                target.takeDamage(this.attackDamage); // แปลงเป็น int เผื่อไว้เหมือนเดิมครับ
            }
            lastAttackTime = currentTime;
        }
    }

    @Override
    public void Attack(Unit target) {
        return;
    }

    @Override
    public boolean isDead() {
        return this.completelyDead;
    }

    @Override
    public void move() {
        this.x += this.speed;
    }

    @Override
    public void update() {
        updateAnimation();

        if(this.hp <= 0 || currentState == State.DEAD_KNOCKBACK || currentState == State.DEAD_SOUL) {
            return;
        }

        ArrayList<Unit> targets = BattleManager.getInstance().findMultipleTargetsInRange(this);

        if(targets != null && !targets.isEmpty()) {
            isAttacking = true;
            long currentTime = System.currentTimeMillis();

            // ถ้ารอคูลดาวน์ครบแล้ว และไม่ได้ตีอยู่
            if (currentTime - lastAttackTime >= attackCooldown && currentState != State.ATTACK) {
                setState(State.ATTACK); // เริ่มง้างตี
                lastAttackTime = currentTime;
                hasDealtDamageThisAttack = false;

                // ส่งลิสต์เป้าหมายทั้งหมดไปให้ Unit.java ทำดาเมจหมู่ที่รูปที่ 3
                currentTargets.clear();
                currentTargets.addAll(targets);
            }
        } else {
            isAttacking = false;
            // ถ้าศัตรูตายหมดแล้ว และง้างตีเสร็จแล้ว ให้เดินต่อ
            if (currentState != State.ATTACK) {
                setState(State.WALK);
                this.move();
            }
        }
    }

    @Override
    public double getRenderWidth() {
        return 0;
    }

    @Override
    public double getRenderHeight() {
        return 0;
    }
}