package com.gx.velveteclipse.forge;
import net.minecraft.server.level.ServerPlayer;
/** Invocation markers only. Skill impact is emitted from the confirmed BattleResolution. */
public final class CinematicEffects {
 public static void tick(Object actor,int tick,String skill,boolean magic,double[] base,float yaw){
  if(tick==38)FeedbackServer.cue(actor,"summon_evoker");
  if(tick==46)FeedbackServer.cue(actor,"summon_summon");
  if(tick==54)FeedbackServer.cue(actor,"summon_persona_aura");
 }
 public static void melee(ServerPlayer player){}
}
