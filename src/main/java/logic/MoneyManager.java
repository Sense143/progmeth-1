package logic;

public class MoneyManager {
    private static MoneyManager instance;

    private double currentMoney = 0;
    private int maxMoney;
    private int moneyLevel = 1;
    private final int MAX_LEVEL = 8;
    private int upgradeCost;
    private double generationRate;

    private MoneyManager() {
        reset();
    }

    public static MoneyManager getInstance() {
        if (instance == null) instance = new MoneyManager();
        return instance;
    }

    // รีเซ็ตตอนเริ่มด่านใหม่
    public void reset() {
        this.currentMoney = 0;
        this.moneyLevel = 1;
        updateStats();
    }

    // อัปเดตสเตตัสกระเป๋าตังค์ตามเลเวลปัจจุบัน
    private void updateStats() {
        this.maxMoney = 100 + (moneyLevel * 100);
        this.upgradeCost = (int) (0.6 * this.maxMoney);
        this.generationRate = 0.3 + (moneyLevel * 0.15); // ความเร็วเงินเด้ง (คำนวณทุกเฟรม)
    }

    // เรียกใช้ทุกๆ เฟรม (60 ครั้ง/วินาที)
    public void update() {
        if (currentMoney < maxMoney) {
            currentMoney += generationRate;
            if (currentMoney > maxMoney) {
                currentMoney = maxMoney; // ล็อกไม่ให้เกินหลอด
            }
        }
    }

    // เมธอดสำหรับใช้เงินซื้อแมว
    public boolean spend(int amount) {
        if (currentMoney >= amount) {
            currentMoney -= amount;
            return true; // จ่ายเงินสำเร็จ
        }
        return false; // เงินไม่พอ
    }

    // เมธอดสำหรับกด Level Up
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

    public int getCurrentMoney() { return (int) currentMoney; }
    public int getMaxMoney() { return maxMoney; }
    public int getMoneyLevel() { return moneyLevel; }
    public int getUpgradeCost() { return upgradeCost; }
    public boolean isMaxLevel() { return moneyLevel >= MAX_LEVEL; }
}