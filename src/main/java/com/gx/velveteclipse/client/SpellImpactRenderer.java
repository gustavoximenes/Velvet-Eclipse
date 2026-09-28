package com.gx.velveteclipse.client;
import com.gx.velveteclipse.network.BattleSpellS2C;
import com.gx.velveteclipse.client.vfx.PhotonBattleEffects;
public final class SpellImpactRenderer {
 public static void reset(){PhotonBattleEffects.reset();}
 public static void accept(BattleSpellS2C p){for(var t:p.targets())PhotonBattleEffects.impact(p.skill(),t);}
}
