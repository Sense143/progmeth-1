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
        return this.hp <= 0;
    }

    @Override
    public void move() {
        this.x += this.speed;
    }

    @Override
    public void update() {
        if(this.hp <= 0){
            return;
        }

        // 🌟 1. สั่งให้ระบบรูปภาพอัปเดตทุกรอบที่ลูปทำงาน
        updateAnimation();

        Unit target = BattleManager.getInstance().findSingleTargetInRange(this);
        if(target != null){
            isAttacking = true;
            long currentTime = System.currentTimeMillis();

            // 🌟 2. Logic จัดการรูปโจมตี กับ รูปคูลดาวน์
            // สมมติให้รูปโจมตี (ง้างมือ) แสดงผลเป็นเวลา 500ms หลังทำดาเมจ
            long timeSinceLastAttack = currentTime - lastAttackTime;

            if (timeSinceLastAttack < 500) {
                // พึ่งโจมตีไปไม่นาน ให้แสดงท่า ATTACK วนไป
                setState(State.ATTACK);
            } else if (timeSinceLastAttack >= attackCooldown) {
                // คูลดาวน์เสร็จแล้ว! โจมตีเลย (lastAttackTime จะถูกรีเซ็ตในนี้)
                setState(State.ATTACK);
                this.Attack(target);
            } else {
                // ตีเสร็จแล้ว แต่คูลดาวน์ยังไม่เสร็จ ให้ยืนรอ (IDLE)
                setState(State.IDLE);
            }
        }
        else{
            isAttacking = false;
            // 🌟 3. ไม่มีศัตรูในระยะ ให้เปลี่ยนท่าเป็นเดิน (WALK)
            setState(State.WALK);
            this.move();
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