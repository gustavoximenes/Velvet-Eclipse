package com.gx.velveteclipse.core.battle;

/** Distances in blocks, relative to the leader's original position and facing. */
public final class BattleFormation {
    public static final int MAX_ENEMIES=5;
    public static int validateCount(int count){if(count<1||count>MAX_ENEMIES)throw new IllegalArgumentException("Use entre 1 e 5 inimigos.");return count;}
    public static double enemyX(int index,int count){validateCount(count);return (index-(count-1)/2.0)*1.8;}
    public static double allyX(int slot){return slot==0?0:((slot+1)/2)*1.8*(slot%2==1?1:-1);}
    private BattleFormation(){}
}
