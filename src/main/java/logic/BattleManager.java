package logic;

import models.base.Tower;
import models.base.Unit;
import models.enemies.Stickman;
import models.units.TofuCat;
import models.units.UFOCat;

import java.util.ArrayList;

public class BattleManager {
    private static BattleManager instance;
    private ArrayList<Unit> playerUnits = new ArrayList<>();
    private ArrayList<Unit> enemyUnits = new ArrayList<>();

    public static BattleManager getInstance() {
        if (instance == null) instance = new BattleManager();
        return instance;
    }

    public ArrayList<Unit> findMultipleTargetsInRange(Unit attacker) {
        ArrayList<Unit> potentialTargets = (isPlayerUnit(attacker)) ? enemyUnits : playerUnits;
        ArrayList<Unit> targets = new ArrayList<>();
        for (Unit target : potentialTargets) {
            if(target.getHp() <= 0 && !(target instanceof models.base.Tower)) continue;
            double distance = Math.abs(attacker.getX() - target.getRimPosition());
            if(target instanceof TofuCat){
                distance = Math.abs(attacker.getX() - target.getX());
            }
            if(target instanceof Stickman && attacker instanceof UFOCat){
                distance = Math.abs(attacker.getX() - target.getX());
            }
            if (distance <= attacker.getAttackRange() && distance >= attacker.getAttackRangeMin()) {
                targets.add(target);
            }
        }
        return targets;
    }

    public ArrayList<Unit> getEveryTargetOnBoard(){
        return enemyUnits;
    }

    public Unit findSingleTargetInRange(Unit attacker) {
        ArrayList<Unit> Targets = (isPlayerUnit(attacker)) ? enemyUnits : playerUnits;
        for (Unit target : Targets) {
            if(target.getHp() <= 0 && !(target instanceof models.base.Tower)) continue;
            double distance = Math.abs(attacker.getX() - target.getRimPosition());
            if(target instanceof TofuCat){
                distance = Math.abs(attacker.getX() - target.getX());
            }
            if (distance <= attacker.getAttackRange()) {
                return target;
            }
        }
        return null;
    }

    private boolean isPlayerUnit(Unit unit) {
        return playerUnits.contains(unit);
    }

    public void addPlayerUnit(Unit unit) {
        playerUnits.add(unit);
    }

    public void addEnemyUnit(Unit unit) {
        enemyUnits.add(unit);
    }

    public void removeUnit(Unit unit) {
        playerUnits.remove(unit);
        enemyUnits.remove(unit);
    }

    public ArrayList<Unit> getPlayerUnits() { return playerUnits; }
    public ArrayList<Unit> getEnemyUnits()  { return enemyUnits; }

    public void clearAll() {
        playerUnits.clear();
        enemyUnits.clear();
    }
}
