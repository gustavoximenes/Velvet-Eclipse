package com.gx.velveteclipse.forge;
import net.minecraft.server.level.ServerPlayer;
/** Invocation markers only. Skill impact is emitted from the confirmed BattleResolution. */
public final class CinematicEffects {
 public static void tick(Object actor,int tick,String persona){
  FeedbackServer.invocation(actor,tick,persona);
 }
 public static void melee(ServerPlayer player){}
}
