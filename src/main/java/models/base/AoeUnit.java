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
        return; // AoeUnit ไม่โจมตีเป้าหมายเดี่ยว
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
        if(isDead()){
            BattleManager.getInstance().removeUnit(this);
            return;
        }

        // 🌟 1. สั่งให้ระบบรูปภาพอัปเดตทุกรอบ (คลาสแม่ Unit จะจัดการ animationTimer ให้เอง)
        updateAnimation();

        // ค้นหาศัตรูแบบหมู่
        ArrayList<Unit> targets = BattleManager.getInstance().findMultipleTargetsInRange(this);

        if(targets != null && !targets.isEmpty()){
            isAttacking = true;
            long currentTime = System.currentTimeMillis();

            // 🌟 2. Logic จัดการรูปโจมตี กับ รูปคูลดาวน์
            // ให้รูปโจมตี (ง้างมือ) แสดงผลเป็นเวลา 500ms หลังทำดาเมจ
            long timeSinceLastAttack = currentTime - lastAttackTime;

            if (timeSinceLastAttack < 500) {
                // พึ่งโจมตีไปไม่นาน ให้แสดงท่า ATTACK วนไป
                setState(State.ATTACK);
            } else if (timeSinceLastAttack >= attackCooldown) {
                // คูลดาวน์เสร็จแล้ว! โจมตีเลย แล้วเปลี่ยนเป็นท่า ATTACK
                setState(State.ATTACK);
                this.Attack(targets);
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