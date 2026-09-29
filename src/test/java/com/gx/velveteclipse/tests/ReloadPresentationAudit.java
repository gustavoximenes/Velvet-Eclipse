package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.vfx.PhotonEffectPlan;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.util.*;

/** Protocol, effect budgets, layout separation and camera trajectories without launching Minecraft. */
public final class ReloadPresentationAudit {
 private static int checks;
 private static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static void main(String[] args){
  for(Element element:Element.values())if(element!=Element.NONE)for(float scale:new float[]{.5f,1,1.65f,100}){
   var p=PhotonEffectPlan.create(element,scale,41);check(p.beams().size()<=48&&p.sparks()<=32,"effect bounded for five targets");check(p.equals(PhotonEffectPlan.create(element,scale,41)),"deterministic choreography");
   for(var b:p.beams()){check(b.width()>0&&b.life()>0&&b.life()+b.delay()<60,"finite emitter lifetime");for(var v:List.of(b.from(),b.to()))check(Float.isFinite(v.x()+v.y()+v.z())&&Math.abs(v.x())<4&&v.y()>=-2&&v.y()<7,"bounded target-local geometry");}
  }
  for(String kind:List.of("skill","attack","enemy","command"))for(double tick=0;tick<120;tick+=.25){var s=BattleShot.sample(kind,tick);check(Double.isFinite(s.side()+s.height()+s.depth()+s.focus()+s.focusHeight()),"camera has no NaN");check(s.fov()>=36&&s.fov()<=70&&Math.abs(s.roll())<=2,"controlled FOV/roll");check(s.height()>=1.3&&s.focus()>=0&&s.focus()<=1,"camera tracks actor/target above ground");}
  for(int[] size:List.of(new int[]{320,180},new int[]{427,240},new int[]{640,360},new int[]{640,480},new int[]{1280,540},new int[]{1920,1080})){
   int w=size[0],h=size[1];var commands=BattleMenuLayout.main(w,h);
   for(int i=0;i<commands.length;i++){var a=commands[i];check(a.x()>=0&&a.y()>=0&&a.x()+a.w()<=w&&a.y()+a.h()<=h,"command in viewport");for(int j=i+1;j<commands.length;j++)check(!intersects(a,commands[j]),"unique command hitboxes");}
   for(int count=1;count<=5;count++)for(int i=0;i<count;i++){var a=ReloadBattleDesign.party(w,h,count,i);check(a.x()>=0&&a.y()-a.h()*.4>=0&&a.x()+a.w()<=w&&a.y()+a.h()<=h,"party column in viewport");for(var b:commands)check(!intersects(a,b),"party never covers command controls");}
  }
  var roster=new BattleRosterS2C(List.of(new BattleRosterS2C.Target(UUID.randomUUID(),1,"Player",true,321,450,"orpheus","",true,37,110),new BattleRosterS2C.Target(UUID.randomUUID(),9,"Shadow",false,0,140,"maya","FIRE:WEAK",false,0,0)));
  var buf=new FriendlyByteBuf(Unpooled.buffer());try{BattleRosterS2C.encode(roster,buf);check(roster.equals(BattleRosterS2C.decode(buf)),"HP/SP/team roundtrip");}finally{buf.release();}
  var cue=new BattleDirectionS2C(UUID.randomUUID(),2,5,"skill","agi","BOW",-32000,64,-1200,32000,70,800,108);buf=new FriendlyByteBuf(Unpooled.buffer());try{BattleDirectionS2C.encode(cue,buf);check(cue.equals(BattleDirectionS2C.decode(buf)),"remote actor/target roundtrip");}finally{buf.release();}
  boolean rejected=false;try{new BattleDirectionS2C(UUID.randomUUID(),1,2,"skill","agi","SWORD",Double.NaN,0,0,0,0,0,108);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid coordinates rejected");
  for(String id:List.of("agi","zio","eiga","garu","bufu","kouha","frei","psi","megido","dia"))check(SpellVisuals.supported(id),"Photon family for "+id);
  System.out.println("PASS Reload presentation: "+checks+" checks; network actor/target and HP/SP, finite VFX budgets, camera framing, responsive hitboxes. Offline only.");
 }
 private static boolean intersects(BattleMenuLayout.Rect a,BattleMenuLayout.Rect b){return a.x()<b.x()+b.w()&&a.x()+a.w()>b.x()&&a.y()<b.y()+b.h()&&a.y()+a.h()>b.y();}
}
