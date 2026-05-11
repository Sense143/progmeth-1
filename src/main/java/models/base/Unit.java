package models.base;

import interfaces.Attackable;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Abstract base class for every entity on the battlefield — cats, dogs, and
 * towers alike.
 *
 * <p>A {@code Unit} holds core combat statistics, a sprite state machine
 * ({@link State}), and the shared death animation logic (knockback bounce →
 * soul float). Concrete subclasses supply their own sprites, stats, and
 * {@link #update()} AI loop.
 */
public abstract class Unit implements Attackable {
    /** Display name shown in debug output. */
    protected String name;
    /** World X position (pixels, left = 0). */
    protected double x;
    /** Current hit points. Clamped to [0, initial HP] by {@link #takeDamage}. */
    protected double hp;
    /** Damage dealt per attack cycle. */
    protected double attackDamage;
    /** Milliseconds between successive attacks. */
    protected double attackCooldown;
    /** Maximum distance (pixels) at which this unit can hit targets. */
    protected double attackRange;
    /** Pixels moved per frame (negative = moving left / cat direction). */
    protected double speed;
    /** Whether the unit is currently engaged with a target. */
    protected boolean isAttacking;
    /** {@code true} for AoE units, {@code false} for single-target units. */
    protected boolean isAoe;
    /** Timestamp (ms) of the last attack, used for cooldown gating. */
    protected long lastAttackTime = 0;
    /** Minimum distance at which this unit can hit targets (inner range). */
    protected double attackRangeMin = 0;
    /** Guards against dealing damage more than once per attack animation. */
    protected boolean hasDealtDamageThisAttack = false;

    /** Targets locked in at attack-start; damage is applied on animation frame 3. */
    protected ArrayList<Unit> currentTargets = new ArrayList<>();

    /**
     * Set to {@code true} after the soul-float animation has scrolled off
     * screen. The game loop uses this flag to remove the unit from all lists.
     */
    public boolean completelyDead = false;

    /**
     * Animation state machine used to select the correct sprite sheet row and
     * drive movement/death behaviour.
     */
    public enum State { WALK, ATTACK, IDLE, DEAD_KNOCKBACK, DEAD_SOUL }
    /** Current animation state. */
    protected State currentState = State.WALK;

    /** Three-frame walk animation sprites. */
    protected Image[] walkSprites = new Image[3];
    /** Three-frame attack animation sprites. */
    protected Image[] attackSprites = new Image[3];
    /** Single idle (post-attack rest) sprite. */
    protected Image idleSprite;
    /** Sprite shown during the knockback death phase. */
    protected Image knockbackSprite;
    /**
     * Four-frame soul animation shared across all unit instances to avoid
     * loading the same images repeatedly.
     */
    protected static Image[] sharedSoulSprites = new Image[4];

    /** Index into the current animation's sprite array. */
    protected int currentFrame = 0;
    /** Frame tick counter; resets every {@link #animSpeed} ticks. */
    protected int animTick = 0;
    /** Ticks per animation frame (lower = faster). */
    protected int animSpeed = 10;

    /** Frame counter used during the knockback bounce sequence. */
    protected int deadFrameCounter = 0;
    /** Vertical offset (pixels, upward = negative) for death/soul animations. */
    protected double yOffset = 0;
    /** Unused; reserved for future soul-float timing. */
    protected double soulTimer = 0;

    /**
     * Optional override sprite set externally (e.g., cannon-fire flash).
     * When non-null, {@link #getCurrentSprite()} returns this image instead
     * of the state-machine selection.
     */
    protected Image customSprite = null;

    /**
     * Temporarily overrides the displayed sprite with a custom image.
     *
     * @param sprite the image to display, or {@code null} to revert to normal
     */
    public void setCurrentSprite(Image sprite) {
        this.customSprite = sprite;
    }

    /**
     * Constructs a unit with the given base statistics. Sprite arrays are
     * null until the subclass constructor populates them.
     *
     * @param name          display name
     * @param x             initial world X position
     * @param hp            maximum (and initial) hit points
     * @param attackDamage  damage per attack
     * @param attackCooldown milliseconds between attacks
     * @param attackRange   maximum attack distance in pixels
     * @param speed         pixels moved per frame (negative moves left)
     */
    public Unit(String name, double x, double hp, double attackDamage, double attackCooldown, double attackRange, double speed) {
        this.name = name;
        this.x = x;
        this.hp = hp;
        this.attackDamage = attackDamage;
        this.attackCooldown = attackCooldown;
        this.speed = speed;
        this.attackRange = attackRange;
        this.isAttacking = false;
        loadSharedSoulSprites();
    }

    /**
     * Reduces HP by {@code damage}, clamping the result to 0.
     *
     * @param damage the raw damage amount (non-negative)
     */
    @Override
    public void takeDamage(double damage) {
        if (this.hp > 0) {
            this.hp -= damage;
            this.hp = Math.max(this.hp, 0);
        }
    }

    /** @return world X position */
    public double getX() { return x; }
    /** @return current vertical render offset (used by death animations) */
    public double getYOffset() { return yOffset; }
    /** @return current HP */
    public double getHp() { return hp; }
    /**
     * Directly sets HP. Prefer {@link #takeDamage} so floor clamping applies.
     * @param hp new HP value
     */
    public void setHp(double hp) { this.hp = hp; }
    /**
     * Teleports the unit to an absolute world X coordinate.
     * @param x new X position
     */
    public void setX(double x) { this.x = x; }
    /** @return maximum attack distance in pixels */
    public double getAttackRange() { return attackRange; }
    /**
     * Overrides the attack range at runtime.
     * @param attackRange new range in pixels
     */
    public void setAttackRange(double attackRange) { this.attackRange = attackRange; }
    /** @return minimum attack distance (inner dead zone) in pixels */
    public double getAttackRangeMin() { return attackRangeMin; }
    /** @return movement speed in pixels per frame */
    public double getSpeed() { return speed; }

    /**
     * Per-frame logic hook. Subclasses implement AI: movement, target
     * acquisition, attack timing, and animation state transitions.
     */
    public abstract void update();

    /**
     * Drives the sprite state machine every frame.
     *
     * <p>Handles three phases in priority order:
     * <ol>
     *   <li><b>DEAD_KNOCKBACK</b> — two-bounce arc over 45 frames, then
     *       transitions to DEAD_SOUL.</li>
     *   <li><b>DEAD_SOUL</b> — soul floats upward; sets {@link #completelyDead}
     *       when it leaves the screen.</li>
     *   <li><b>Normal</b> — cycles WALK / ATTACK / IDLE frames, triggering
     *       {@link #processAttackDamage()} on animation frame&nbsp;3.</li>
     * </ol>
     */
    protected void updateAnimation() {
        if (this.hp <= 0 && currentState != State.DEAD_KNOCKBACK && currentState != State.DEAD_SOUL) {
            setState(State.DEAD_KNOCKBACK);
            this.yOffset = 0;
            this.deadFrameCounter = 0;
        }

        if (currentState == State.DEAD_KNOCKBACK) {
            animTick++;

            int totalFrames = 45;
            double totalKnockbackX = 150.0;

            double heightBounce1 = 25.0;
            double heightBounce2 = 15.0;

            double direction = (this.speed > 0) ? -1 : 1;

            this.x += direction * (totalKnockbackX / totalFrames);

            int halfFrames = totalFrames / 2;

            if (animTick <= halfFrames) {
                if (animTick <= halfFrames / 2) {
                    this.yOffset -= (heightBounce1 / (halfFrames / 2.0));
                } else {
                    this.yOffset += (heightBounce1 / (halfFrames / 2.0));
                }
            } else {
                int bounce2Tick = animTick - halfFrames;
                if (bounce2Tick <= halfFrames / 2) {
                    this.yOffset -= (heightBounce2 / (halfFrames / 2.0));
                } else {
                    this.yOffset += (heightBounce2 / (halfFrames / 2.0));
                }
            }

            if (animTick >= totalFrames) {
                this.yOffset = 0;
                animTick = 0;
                setState(State.DEAD_SOUL);
            }
            return;
        }

        if (currentState == State.DEAD_SOUL) {
            this.yOffset -= 4;

            animTick++;
            if (animTick >= animSpeed) {
                animTick = 0;
                currentFrame = (currentFrame + 1) % 4;
            }

            if (this.yOffset <= -400) {
                this.completelyDead = true;
            }
            return;
        }

        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            currentFrame++;

            if (currentState == State.WALK) {
                if (currentFrame > 2) currentFrame = 0;
            }
            else if (currentState == State.ATTACK) {
                if (currentFrame == 2 && !hasDealtDamageThisAttack) {
                    processAttackDamage();
                }
                if (currentFrame > 2) {
                    currentFrame = 0;
                    hasDealtDamageThisAttack = false;
                    setState(State.IDLE);
                }
            }
            else if (currentState == State.IDLE) {
                currentFrame = 0;
            }
        }
    }

    /**
     * Returns the image to display this frame, honouring the following
     * priority: custom override → knockback → soul → walk → attack → idle.
     *
     * @return the current sprite, or {@code null} if none is available
     */
    public Image getCurrentSprite() {
        if (customSprite != null) return customSprite;

        if (currentState == State.DEAD_KNOCKBACK && knockbackSprite != null) return knockbackSprite;
        if (currentState == State.DEAD_SOUL && sharedSoulSprites[currentFrame] != null) return sharedSoulSprites[currentFrame];
        if (currentState == State.WALK && walkSprites[currentFrame] != null) return walkSprites[currentFrame];
        if (currentState == State.ATTACK && attackSprites[currentFrame] != null) return attackSprites[currentFrame];
        if (currentState == State.IDLE && idleSprite != null) return idleSprite;
        return null;
    }

    /**
     * Transitions to a new animation state, resetting frame counters.
     * No-ops if the unit is already in {@code newState}.
     *
     * @param newState the state to transition to
     */
    public void setState(State newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.currentFrame = 0;
            this.animTick = 0;
        }
    }

    /**
     * Returns the rendered width of this unit's sprite in pixels.
     * Used for collision rim calculations.
     *
     * @return render width
     */
    public abstract double getRenderWidth();

    /**
     * Returns the rendered height of this unit's sprite in pixels.
     *
     * @return render height
     */
    public abstract double getRenderHeight();

    /**
     * Returns the world X of the unit's leading edge — the point enemies
     * measure range from. For right-moving units this is the right edge;
     * for left-moving units it is the left edge.
     *
     * @return rim X position
     */
    public double getRimPosition(){
        if(this.speed > 0) return this.x + (this.getRenderWidth())/2;
        else return this.x - (this.getRenderWidth())/2;
    }

    /**
     * Applies {@link #attackDamage} to all locked-in {@link #currentTargets}.
     * Called automatically by {@link #updateAnimation()} on attack frame 3.
     * The {@link #hasDealtDamageThisAttack} flag prevents repeat damage
     * within the same swing.
     */
    protected void processAttackDamage() {
        if (currentTargets != null && !currentTargets.isEmpty()) {
            for (Unit target : currentTargets) {
                target.takeDamage(this.attackDamage);
            }
        }
        hasDealtDamageThisAttack = true;
    }

    /**
     * Lazily loads the four soul-animation frames into {@link #sharedSoulSprites}
     * if they have not been loaded yet. Safe to call from every unit constructor.
     */
    protected void loadSharedSoulSprites() {
        if (sharedSoulSprites[0] == null) {
            try {
                sharedSoulSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_1.png")));
                sharedSoulSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_2.png")));
                sharedSoulSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_3.png")));
                sharedSoulSprites[3] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/soul/soul_4.png")));
            } catch (Exception e) {
                System.out.println("โหลดรูปวิญญาณไม่สำเร็จ: " + e.getMessage());
            }
        }
    }

    /**
     * Returns {@code true} if this unit is currently in the
     * {@link State#DEAD_SOUL} floating phase.
     *
     * @return {@code true} when displaying soul animation
     */
    public boolean isDeadSoul() {
        return this.currentState == State.DEAD_SOUL;
    }
}