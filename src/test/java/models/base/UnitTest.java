package models.base;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Unit base class logic — no JavaFX rendering involved.
 * ConcreteUnit's update() delegates to updateAnimation() so we can drive
 * the animation state machine through the same entry point Main uses.
 */
class UnitTest {

    static class ConcreteUnit extends Unit {
        ConcreteUnit(double x, double hp, double attackRange, double speed) {
            super("TestUnit", x, hp, 10, 500, attackRange, speed);
        }

        @Override public void update() { updateAnimation(); }
        @Override public void Attack(ArrayList<Unit> targets) {}
        @Override public void Attack(Unit target) {}
        @Override public boolean isDead() { return completelyDead; }
        @Override public double getRenderWidth() { return 50; }
        @Override public double getRenderHeight() { return 50; }
    }

    // ─── Position ─────────────────────────────────────────────────────────────

    @Test
    void getX_returnsInitialX() {
        ConcreteUnit unit = new ConcreteUnit(150, 100, 50, 1);
        assertEquals(150, unit.getX(), 0.001);
    }

    @Test
    void setX_updatesX() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.setX(250);
        assertEquals(250, unit.getX(), 0.001);
    }

    // ─── HP ───────────────────────────────────────────────────────────────────

    @Test
    void getHp_returnsInitialHp() {
        ConcreteUnit unit = new ConcreteUnit(0, 200, 50, 1);
        assertEquals(200, unit.getHp(), 0.001);
    }

    @Test
    void setHp_updatesHp() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.setHp(50);
        assertEquals(50, unit.getHp(), 0.001);
    }

    @Test
    void takeDamage_reducesHp() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.takeDamage(30);
        assertEquals(70, unit.getHp(), 0.001);
    }

    @Test
    void takeDamage_doesNotGoBelowZero() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.takeDamage(200);
        assertEquals(0, unit.getHp(), 0.001);
    }

    @Test
    void takeDamage_whenHpAlreadyZero_hpRemainsZero() {
        // Unit.takeDamage only acts when hp > 0
        ConcreteUnit unit = new ConcreteUnit(0, 0, 50, 1);
        unit.takeDamage(50);
        assertEquals(0, unit.getHp(), 0.001);
    }

    @Test
    void takeDamage_exactKill_leavesZeroHp() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.takeDamage(100);
        assertEquals(0, unit.getHp(), 0.001);
    }

    // ─── Speed / Range ────────────────────────────────────────────────────────

    @Test
    void getSpeed_returnsSpeed() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 3);
        assertEquals(3, unit.getSpeed(), 0.001);
    }

    @Test
    void getAttackRange_returnsRange() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 75, 1);
        assertEquals(75, unit.getAttackRange(), 0.001);
    }

    @Test
    void setAttackRange_updatesRange() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.setAttackRange(120);
        assertEquals(120, unit.getAttackRange(), 0.001);
    }

    @Test
    void getAttackRangeMin_defaultsToZero() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        assertEquals(0, unit.getAttackRangeMin(), 0.001);
    }

    // ─── getRimPosition ───────────────────────────────────────────────────────

    @Test
    void getRimPosition_speedPositive_returnsRightEdge() {
        // speed > 0 → rimPos = x + renderWidth/2 = 100 + 25 = 125
        ConcreteUnit unit = new ConcreteUnit(100, 100, 50, 3);
        assertEquals(125, unit.getRimPosition(), 0.001);
    }

    @Test
    void getRimPosition_speedNegative_returnsLeftEdge() {
        // speed < 0 → rimPos = x - renderWidth/2 = 100 - 25 = 75
        ConcreteUnit unit = new ConcreteUnit(100, 100, 50, -3);
        assertEquals(75, unit.getRimPosition(), 0.001);
    }

    @Test
    void getRimPosition_speedZero_returnsLeftEdge() {
        // speed == 0 → else branch → x - renderWidth/2
        ConcreteUnit unit = new ConcreteUnit(100, 100, 50, 0);
        assertEquals(75, unit.getRimPosition(), 0.001);
    }

    // ─── setState ─────────────────────────────────────────────────────────────

    @Test
    void initialState_isWalk() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        assertEquals(Unit.State.WALK, unit.currentState);
    }

    @Test
    void setState_changesState() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.setState(Unit.State.ATTACK);
        assertEquals(Unit.State.ATTACK, unit.currentState);
    }

    @Test
    void setState_resetsCurrentFrame() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.currentFrame = 2;
        unit.setState(Unit.State.ATTACK);
        assertEquals(0, unit.currentFrame);
    }

    @Test
    void setState_resetsAnimTick() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.animTick = 9;
        unit.setState(Unit.State.IDLE);
        assertEquals(0, unit.animTick);
    }

    @Test
    void setState_sameState_doesNotResetFrame() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.currentFrame = 2;
        unit.setState(Unit.State.WALK); // already WALK → no reset
        assertEquals(2, unit.currentFrame);
    }

    // ─── isDeadSoul ───────────────────────────────────────────────────────────

    @Test
    void isDeadSoul_walkState_returnsFalse() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        assertFalse(unit.isDeadSoul());
    }

    @Test
    void isDeadSoul_soulState_returnsTrue() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        unit.setState(Unit.State.DEAD_SOUL);
        assertTrue(unit.isDeadSoul());
    }

    // ─── updateAnimation — death state machine ────────────────────────────────

    @Test
    void updateAnimation_whenHpZeroInWalkState_transitionsToKnockback() {
        ConcreteUnit unit = new ConcreteUnit(0, 0, 50, 1);
        unit.update(); // hp=0 → DEAD_KNOCKBACK
        assertEquals(Unit.State.DEAD_KNOCKBACK, unit.currentState);
    }

    @Test
    void updateAnimation_after45KnockbackFrames_transitionsToSoul() {
        ConcreteUnit unit = new ConcreteUnit(0, 0, 50, 1);
        unit.update(); // triggers DEAD_KNOCKBACK entry

        // animTick starts at 0 after setState; need 45 increments to reach totalFrames
        for (int i = 0; i < 45; i++) unit.update();

        assertEquals(Unit.State.DEAD_SOUL, unit.currentState);
    }

    @Test
    void updateAnimation_soulState_yOffsetDecreasesEachFrame() {
        ConcreteUnit unit = new ConcreteUnit(0, 0, 50, 1);
        unit.setState(Unit.State.DEAD_SOUL);
        unit.yOffset = 0;
        unit.update();
        assertTrue(unit.yOffset < 0);
    }

    @Test
    void updateAnimation_soulState_after100Frames_completelyDead() {
        ConcreteUnit unit = new ConcreteUnit(0, 0, 50, 1);
        unit.setState(Unit.State.DEAD_SOUL);
        unit.yOffset = 0;
        // yOffset decreases by 4 each frame; completelyDead when yOffset <= -400
        for (int i = 0; i < 100; i++) unit.update();
        assertTrue(unit.completelyDead);
    }

    @Test
    void updateAnimation_walkState_cyclesFrames() {
        ConcreteUnit unit = new ConcreteUnit(0, 100, 50, 1);
        // Default animSpeed=10; advance past frame 2 to see wrap-around at frame 0
        unit.animTick = 9;
        unit.currentFrame = 2;
        unit.update(); // animTick → 10 ≥ 10 → frame++  → 3 → wraps to 0
        assertEquals(0, unit.currentFrame);
    }

    @Test
    void updateAnimation_knockback_xMovesInOppositeDirection() {
        // speed > 0 (moving right) → knockback moves left (x decreases)
        ConcreteUnit unit = new ConcreteUnit(500, 0, 50, 3);
        unit.update(); // transitions to DEAD_KNOCKBACK
        double xAfterEntry = unit.getX();
        unit.update();
        assertTrue(unit.getX() < xAfterEntry);
    }
}