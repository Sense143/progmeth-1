package models.base;

import interfaces.Attackable;

import java.util.ArrayList;

public class Tower extends Unit{

    private double maxHp;

    public double getMaxHp() {
        return maxHp;
    }

    public Tower(String name, double x, double hp) {
        super(name, x, hp, 0, 0, 0, 0);
        this.maxHp = hp;
    }

    @Override
    public void update() {

    }

    @Override
    public double getRenderWidth() {
        return 0;
    }

    @Override
    public double getRenderHeight() {
        return 0;
    }

    @Override
    public void takeDamage(double damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    @Override
    public void Attack(ArrayList<Unit> targets) {

    }

    @Override
    public void Attack(Unit target) {

    }

    @Override
    public boolean isDead() {
        return this.hp <= 0;
    }
}
