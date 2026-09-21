package com.gx.velveteclipse.client;
import com.gx.velveteclipse.network.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class BattleTacticsClient {
 private static BattleTacticsS2C state=BattleTacticsS2C.empty();
 private static String queued="",toast="";
 private static int toastAge=-1,dismissed=-1;
 private static boolean awaitingAllOut;
 public static boolean awaitingAllOut(){return awaitingAllOut;}
 public static boolean exclusiveHud(){return prompt()||awaitingAllOut||AllOutPresentation.active();}
 public static void reset(){state=BattleTacticsS2C.empty();queued=toast="";toastAge=dismissed=-1;awaitingAllOut=false;}
 public static void accept(BattleTacticsS2C packet){
  awaitingAllOut=false;
  if(packet.actor()==null&&packet.event()==0){reset();return;}
  if(!packet.kind().isEmpty()&&state.event()!=packet.event()){queued=packet.kind();dismissed=-1;}
  state=packet;
 }
 public static boolean oneMore(){return state.oneMore();}
 public static List<UUID> passTargets(){return state.passTargets();}
 public static boolean canPass(){return state.oneMore()&&!state.passTargets().isEmpty();}
 public static boolean downed(int entity){return state.downed().stream().anyMatch(d->d.entityId()==entity);}
 public static List<BattleTacticsS2C.Down> downed(){return state.downed();}
 public static boolean prompt(){return state.allOut()&&dismissed!=state.event()&&queued.isEmpty()&&(toastAge<0||toastAge>=20)&&ClientBattleState.snapshot().playerTurn()&&!ClientBattleUiState.actionLocked();}
 public static void dismiss(){dismissed=state.event();}
 private static void allOut(){if(prompt()){dismiss();awaitingAllOut=true;ModNetwork.sendAction("all_out","");}}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;
  if(!ClientBattleState.snapshot().active()){reset();return;}
  if(!queued.isEmpty()&&!PersonaCinematics.skillSequenceActive()&&!PersonaCinematics.meleeActive()&&!AllOutPresentation.active()){toast=queued;queued="";toastAge=0;}
  if(toastAge>=0&&++toastAge>42)toastAge=-1;
 }
 public static boolean key(InputEvent.Key e){
  if(Minecraft.m_91087_().f_91080_!=null)return false;
  if(AllOutPresentation.active()||awaitingAllOut)return true;
  if(prompt()){if(e.getAction()==1){if(e.getKey()==257||e.getKey()==335)allOut();else if(e.getKey()==78)dismiss();}return true;}
  if(e.getAction()!=1)return false;
  if((e.getKey()==340||e.getKey()==344)&&ClientBattleState.snapshot().playerTurn()&&canPass()){ClientBattleUiState.baton();return true;}
  return false;
 }
 public static boolean click(double x,double y,int w,int h){
  if(AllOutPresentation.active()||awaitingAllOut)return true;
  if(prompt()){if(BattleTacticalDesign.go(w,h).contains(x,y))allOut();else if(BattleTacticalDesign.cancel(w,h).contains(x,y))dismiss();return true;}
  if(oneMore()&&BattleTacticalDesign.pass(w,h).contains(x,y)){ClientBattleUiState.baton();return true;}return false;
 }
 public static void paint(HudDesign.Canvas c,int w,int h){if(AllOutPresentation.active())return;
  if(prompt()||awaitingAllOut){BattleTacticalDesign.paint(c,w,h,false,false,true,"",-1);return;}
  boolean ready=ClientBattleState.snapshot().playerTurn()&&!PersonaCinematics.skillSequenceActive()&&!PersonaCinematics.meleeActive();
  boolean prompt=prompt();BattleTacticalDesign.paint(c,w,h,!prompt&&state.oneMore()&&ready,canPass(),prompt,toast,prompt?-1:toastAge);
  if(oneMore()&&ClientBattleUiState.selector())c.text("1 MORE: troca de Persona bloqueada",12,h-28,0xFF73F7FF);
 }
}
