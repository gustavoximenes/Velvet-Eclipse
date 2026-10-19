package com.gx.velveteclipse.core.battle;

import java.util.Locale;

/** Four-player Normal is the reference; only actual combat participants affect scaling. */
public enum BattleDifficulty {
    RELAXED("Tranquilo",.70,.50,"Combates mais curtos e dano recebido bem menor."),
    EASY("Fácil",.85,.75,"Inimigos menos resistentes e golpes mais leves."),
    NORMAL("Normal",1,1,"Desafio padrão, ajustado ao tamanho da equipe."),
    HARD("Difícil",1.20,1.30,"Inimigos mais resistentes e golpes mais fortes.");

    public final String label,description;
    public final double health,damage;
    BattleDifficulty(String label,double health,double damage,String description){this.label=label;this.health=health;this.damage=damage;this.description=description;}
    public String id(){return name().toLowerCase(Locale.ROOT);}
    public BattleDifficulty step(int delta){return values()[Math.floorMod(ordinal()+delta,values().length)];}
    public static boolean valid(String id){for(var d:values())if(d.id().equals(id))return true;return false;}
    public static BattleDifficulty parse(String id){for(var d:values())if(d.id().equals(id))return d;return NORMAL;}
    public static int participants(int count){return Math.max(1,Math.min(64,count));}
    public double enemyHealth(int count){int n=participants(count);return health*(n<=4?.2+.2*n:1+.25*(n-4));}
    public double enemyDamage(int count){int n=participants(count);return damage*(n==1?.60:n==2?.75:n==3?.90:1);}
    public long raidDuration(int count){long base=this==RELAXED?450_000:this==EASY?360_000:300_000;return Math.round(base*Math.max(1,participants(count)/4.0));}
    public static boolean mayChoose(boolean leader,boolean available,boolean groupBusy,String id){return leader&&available&&!groupBusy&&valid(id);}
}
