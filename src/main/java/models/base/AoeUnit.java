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
    public void takeDamage(int damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    @Override
    public void Attack(ArrayList<Unit> targets) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAttackTime >= attackCooldown){
            for(Unit target : targets){
                target.takeDamage(this.attackDamage);
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
        return this.hp <= 0;
    }

    @Override
    public void move() {
        this.x += this.speed;
    }

    @Override
    public void update() {
        if(isDead()) return;
        ArrayList<Unit> targets = BattleManager.getInstance().findMultipleTargetsInRange(this);
        if(targets != null && !targets.isEmpty()){
            isAttacking = true;
            this.Attack(targets);
        }
        else{
            isAttacking = false;
            this.move();
        }
    }
}
