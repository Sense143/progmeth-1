package interfaces;

import models.base.Unit;

import java.util.ArrayList;

public interface Attackable {
    void takeDamage(int damage);

    void takeDamage(double damage);

    void Attack(ArrayList<Unit> targets);
    void Attack(Unit target);
    boolean isDead();
}
