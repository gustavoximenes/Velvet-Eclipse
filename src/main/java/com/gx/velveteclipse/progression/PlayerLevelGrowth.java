package com.gx.velveteclipse.progression;

/** Player resources grow independently of Persona levels and the optional roguelike mode. */
public final class PlayerLevelGrowth {
    public static final int BASE_HP = 120, BASE_SP = 45;
    public static final int HP_PER_LEVEL = 6, SP_PER_LEVEL = 3;
    public static final int MAX_LEVEL = 99;

    public static int hpGain(int before, int after) { return levels(before, after) * HP_PER_LEVEL; }
    public static int spGain(int before, int after) { return levels(before, after) * SP_PER_LEVEL; }
    private static int levels(int before, int after) { return Math.max(0, level(after) - level(before)); }
    private static int level(int value) { return Math.max(1, Math.min(MAX_LEVEL, value)); }

    public static int maxHp(int base, int level, int gear) {
        return capacity((long) base + hpGain(1, level) + gear, 1);
    }
    public static int maxSp(int base, int level, int gear) {
        return capacity((long) base + spGain(1, level) + gear, 0);
    }
    public static int addCapacity(int current, int gain, int maximum) {
        return (int) Math.max(0L, Math.min((long) maximum, (long) current + Math.max(0, gain)));
    }
    private static int capacity(long value, int minimum) {
        return (int) Math.max(minimum, Math.min(Integer.MAX_VALUE, value));
    }
    private PlayerLevelGrowth() {}
}
