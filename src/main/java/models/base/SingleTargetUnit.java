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
    public void takeDamage(int damage) {
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
        if(isDead()){
            BattleManager.getInstance().removeUnit(this);
            return;
        }

        Unit target = BattleManager.getInstance().findSingleTargetInRange(this);
        if(target != null){
            isAttacking = true;
            this.Attack(target);
        }
        else{
            isAttacking = false;
            this.move();
        }
    }
}
