package com.gx.velveteclipse.forge;
import com.gx.velveteclipse.network.*;
import net.minecraft.server.level.ServerPlayer;
/** Server distributes selected targets; Photon owns finite client presentation only. */
public final class CinematicEffects {
 public static void tick(Object actor,int tick,String skill,boolean magic,double[] base,float yaw){
  if(tick!=86||!com.gx.velveteclipse.core.persona.SpellVisuals.supported(skill))return;
  var targets=BattleService.effectTargets(actor,skill).stream().limit(5).map(BattleLightningS2C.Strike::at).toList();
  for(Object p:BattleService.members(actor))ModNetwork.sendPresentation(p,new BattleSpellS2C(skill,targets));
 }
 public static void melee(ServerPlayer player){/* The synchronized direction cue schedules the weapon impact. */}
}
