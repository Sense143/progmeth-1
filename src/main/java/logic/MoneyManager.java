package logic;

/**
 * Singleton that manages the player's money throughout a stage.
 *
 * <p>Money accrues passively each frame via {@link #update()} at a rate
 * proportional to the current wallet level. The player can spend money to
 * deploy units ({@link #spend}) or upgrade the wallet capacity
 * ({@link #upgradeWallet}). All state is reset between stages via
 * {@link #reset()}.
 */
public class MoneyManager {
    private static MoneyManager instance;

    private double currentMoney = 0;
    private int maxMoney;
    private int moneyLevel = 1;
    private final int MAX_LEVEL = 8;
    private int upgradeCost;
    private double generationRate;

    /** Money cap of the previous level, used to compute the next level's cap. */
    int previousMoney = 150;

    private MoneyManager() {
        reset();
    }

    /**
     * Returns the shared singleton instance, creating it on first call.
     *
     * @return the global {@code MoneyManager}
     */
    public static MoneyManager getInstance() {
        if (instance == null) instance = new MoneyManager();
        return instance;
    }

    /**
     * Resets all state to initial values for a new stage.
     * Money level returns to 1 and current money is cleared.
     */
    public void reset() {
        this.currentMoney = 0;
        this.moneyLevel = 1;
        previousMoney = 150;
        updateStats();
    }

    /**
     * Recalculates {@code maxMoney}, {@code upgradeCost}, and
     * {@code generationRate} based on the current wallet level.
     * Each level multiplies the cap by 1.5×.
     */
    private void updateStats() {
        this.maxMoney = (int) (previousMoney * 1.5);
        previousMoney = this.maxMoney;
        this.upgradeCost = (int) (0.6 * this.maxMoney);
        this.generationRate = (double) this.maxMoney / 600;
    }

    /**
     * Advances passive money generation by one frame (called ~60 times per
     * second). Money is capped at {@code maxMoney}.
     */
    public void update() {
        if (currentMoney < maxMoney) {
            currentMoney += generationRate;
            if (currentMoney > maxMoney) {
                currentMoney = maxMoney;
            }
        }
    }

    /**
     * Deducts {@code amount} from the current money if funds are sufficient.
     *
     * @param amount the cost to deduct
     * @return {@code true} if the transaction succeeded, {@code false} if
     *         insufficient funds
     */
    public boolean spend(int amount) {
        if (currentMoney >= amount) {
            currentMoney -= amount;
            return true;
        }
        return false;
    }

    /**
     * Upgrades the wallet to the next level if the player has enough money
     * and the wallet is not already at {@link #MAX_LEVEL}.
     *
     * @return {@code true} if the upgrade succeeded
     */
    public boolean upgradeWallet() {
        if (moneyLevel < MAX_LEVEL && currentMoney >= upgradeCost) {
            currentMoney -= upgradeCost;
            moneyLevel++;
            updateStats();
            System.out.println("อัปเกรดกระเป๋าเป็น Level " + moneyLevel + " แล้ว!");
            return true;
        }
        return false;
    }

    /**
     * Returns the player's current money, truncated to an integer.
     *
     * @return current money as {@code int}
     */
    public int getCurrentMoney() { return (int) currentMoney; }

    /**
     * Returns the maximum money the wallet can hold at the current level.
     *
     * @return max money capacity
     */
    public int getMaxMoney() { return maxMoney; }

    /**
     * Returns the current wallet upgrade level (1–{@link #MAX_LEVEL}).
     *
     * @return wallet level
     */
    public int getMoneyLevel() { return moneyLevel; }

    /**
     * Returns the cost to upgrade the wallet to the next level.
     *
     * @return upgrade cost
     */
    public int getUpgradeCost() { return upgradeCost; }

    /**
     * Returns {@code true} if the wallet is already at the maximum level and
     * cannot be upgraded further.
     *
     * @return {@code true} when at max level
     */
    public boolean isMaxLevel() { return moneyLevel >= MAX_LEVEL; }
}