package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.network.BattleRosterS2C;
import net.minecraft.client.Minecraft;
import java.util.*;

/** Resource deltas come only from authoritative roster snapshots, never predicted damage. */
public final class BattleHitNumbers {
 private record Hit(UUID id,int amount,long tick){}
 private static final Map<UUID,Integer> previous=new HashMap<>();
 private static final List<Hit> hits=new ArrayList<>();
 public static void reset(){previous.clear();hits.clear();}
 public static void accept(List<BattleRosterS2C.Target> roster){
  var mc=Minecraft.m_91087_();var w=mc==null?null:mc.f_91073_;if(w==null||roster.isEmpty()){reset();return;}
  var present=new HashSet<UUID>();for(var t:roster){present.add(t.id());Integer old=previous.put(t.id(),t.hp());if(old!=null&&old!=t.hp()){if(hits.size()>=20)hits.remove(0);hits.add(new Hit(t.id(),t.hp()-old,w.m_46467_()));}}
  previous.keySet().retainAll(present);
 }
 public static void paint(HudDesign.Canvas c,int w,int h,float partial){
  var world=Minecraft.m_91087_().f_91073_;if(world==null)return;double now=world.m_46467_()+partial;hits.removeIf(hit->now-hit.tick>32);
  for(var hit:hits){var pos=BattleTargetHud.projected(hit.id);if(pos==null)continue;double t=now-hit.tick,u=ReloadBattleDesign.unit(w,h);float scale=(float)(u*(2.4+Math.max(0,1-t/5)*.9));int color=hit.amount>0?0xFF83FFC6:0xFFFFFFFF;int alpha=(int)(255*Math.min(1,(32-t)/8));color=(color&0xFFFFFF)|(Math.max(0,alpha)<<24);String label=hit.amount>0?"+"+hit.amount:Integer.toString(-hit.amount);int x=(int)(pos[0]-label.length()*6*scale/2),y=(int)(pos[1]-32*u-Math.min(12,t)*u);c.angled(label,x+2,y+2,scale,(alpha<<24)|0x06132D,-9);c.angled(label,x,y,scale,color,-9);}
 }
}
