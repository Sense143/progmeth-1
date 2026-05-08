package models.base;

import interfaces.Attackable;

import java.util.ArrayList;

public class Tower extends Unit{

    public Tower(String name, double x, double hp) {
        super(name, x, hp, 0, 0, 0, 0);
    }

    @Override
    public void update() {

    }

    @Override
    public void takeDamage(int damage) {

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
