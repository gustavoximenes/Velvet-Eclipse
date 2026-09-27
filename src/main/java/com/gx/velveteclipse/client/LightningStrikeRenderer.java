package com.gx.velveteclipse.client;
import com.gx.velveteclipse.network.BattleLightningS2C;
import com.gx.velveteclipse.client.vfx.PhotonBattleEffects;
import com.gx.velveteclipse.core.persona.Element;
public final class LightningStrikeRenderer {
 public static void reset(){PhotonBattleEffects.reset();}
 public static void accept(BattleLightningS2C p){for(var t:p.strikes())PhotonBattleEffects.impact(Element.ELECTRIC,1,t.x(),t.y(),t.z(),t.seed());}
}
