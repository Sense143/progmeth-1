package models.base;

import interfaces.Attackable;
import interfaces.Moveable;
import logic.BattleManager;

import java.util.ArrayList;

public class SingleTargetUnit extends Unit implements Attackable, Moveable {

    public SingleTargetUnit(String name, double x, double hp, double attackDamage, double attackCooldown, double attackRange, double speed) {
        super(name, x, hp, attackDamage, attackCooldown, attackRange, speed);
        this.isAoe = false;
    }

    @Override
    public void takeDamage(double damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    @Override
    public void Attack(ArrayList<Unit> targets) {
        return;
    }

    @Override
    public void Attack(Unit target) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAttackTime >= attackCooldown){
            target.takeDamage(this.attackDamage);
            lastAttackTime = currentTime;
        }
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
        // 1. อัปเดตแอนิเมชัน
        updateAnimation();

        if(this.hp <= 0 || currentState == State.DEAD_KNOCKBACK || currentState == State.DEAD_SOUL) {
            return;
        }

        Unit target = BattleManager.getInstance().findSingleTargetInRange(this);
        if(target != null) {
            isAttacking = true;
            long currentTime = System.currentTimeMillis();

            // 2. ถ้ารอคูลดาวน์ครบแล้ว และ "ไม่ได้กำลังง้างตีอยู่" ให้เริ่มตี!
            if (currentTime - lastAttackTime >= attackCooldown && currentState != State.ATTACK) {
                setState(State.ATTACK); // เริ่มแอนิเมชันง้างตี
                lastAttackTime = currentTime;
                hasDealtDamageThisAttack = false;

                // 3. ล็อคเป้าหมายไว้ ให้ Unit.java เอาไปลดเลือดตอนรูปที่ 3
                currentTargets.clear();
                currentTargets.add(target);
            }
        } else {
            isAttacking = false;
            // 4. ถ้าไม่มีเป้าหมาย และ "ไม่ได้ง้างตีค้างอยู่" ค่อยเดินหน้าต่อ
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