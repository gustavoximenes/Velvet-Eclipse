package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.exploration.*;
import com.gx.velveteclipse.exploration.BowAim;
import com.gx.velveteclipse.shadow.ShadowAwareness;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import net.minecraft.world.phys.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.Gson;
public final class CombatPolishAudit {
 private static int checks;
 private static void check(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  for(boolean visible:new boolean[]{true,false}){var brain=new ShadowAwareness();brain.update(visible,true,true);check(brain.questioning(),"question appears");for(int i=0;i<30;i++){check(!brain.canTurn(),"no turning before 1.5 seconds "+i);brain.update(visible,true,true);}check(brain.canTurn(),"turn unlocks at 30 ticks");}
  var brain=new ShadowAwareness();brain.update(false,true,true);for(int i=0;i<10;i++)brain.update(false,true,true);for(int i=0;i<20;i++)brain.update(true,true,true);check(brain.canTurn(),"notice/search transitions keep the same warning clock");brain.reset();check(brain.alertTicks==0,"fresh warning after reset");
  var frames=new ArrayList<Object>();
  for(double aspect:new double[]{4./3,16./9,21./9,1})for(int count=1;count<=5;count++)for(int yaw=0;yaw<360;yaw+=45){
   var enemies=new ArrayList<Vec3>();double angle=Math.toRadians(yaw);var forward=new Vec3(-Math.sin(angle),0,Math.cos(angle));var side=new Vec3(forward.f_82481_,0,-forward.f_82479_);
   for(int i=0;i<count;i++)for(double x:new double[]{-.85,.85})for(double y:new double[]{.05,2.5})for(double z:new double[]{-.85,.85})enemies.add(forward.m_82490_(5.25+z).m_82549_(side.m_82490_((i-(count-1)/2.)*1.95+x)).m_82520_(0,y,0));
   var view=BattleFraming.shoulder(Vec3.f_82478_,enemies,62,aspect,(a,b)->b);check(BattleFraming.contains(enemies,view.eye(),view.focus(),62,aspect),"all enemy bounds fit");check(Math.abs(view.eye().f_82480_-1.85)<.01,"shoulder height");if(aspect>=1.3)check(view.eye().m_82554_(new Vec3(0,1.85,0))<4,"close shoulder camera on open ground");if(yaw==0&&aspect==16./9)frames.add(view);
  }
  for(int yaw=-180;yaw<=180;yaw+=15)for(int pitch=-80;pitch<=80;pitch+=10){var d=BowAim.direction(yaw,pitch);check(Math.abs(d.m_82553_()-1)<1e-6,"aim direction normalized");check(pitch<=0||d.f_82480_<0,"pitch aims below when looking down");}
  var start=new Vec3(0,1.44,0);var end=start.m_82549_(BowAim.direction(0,0).m_82490_(10));check(new AABB(-.5,0,4,.5,2,5).m_82371_(start,end).isPresent(),"arrow intersects front target");check(new AABB(2,0,4,3,2,5).m_82371_(start,end).isEmpty(),"no side auto-targeting");check(new AABB(-.5,0,4,.5,2,5).m_82371_(start,new Vec3(0,1.44,3)).isEmpty(),"block-limited ray excludes target behind wall");
  for(var family:Family.values())for(String part:List.of("body","head","rightArm","leftArm","rightLeg","leftLeg"))for(boolean bend:new boolean[]{false,true}){
   var previous=CombatAttackPose.sample(family,part,0,bend);for(double t=.02;t<44;t+=.02){var v=CombatAttackPose.sample(family,part,t,bend);check(Float.isFinite(v.getX()+v.getY()+v.getZ()),"finite attack pose");check(Math.abs(v.getX()-previous.getX())+Math.abs(v.getY()-previous.getY())+Math.abs(v.getZ()-previous.getZ())<.14,"no attack pose discontinuity "+family+" "+part+" @ "+t);previous=v;}
  }
  check(CombatAttackPose.speed(Family.BOW,6)==0&&CombatAttackPose.speed(Family.GUN,35)==0,"ranged combatants stay planted");check(CombatAttackPose.weight(0)==0&&CombatAttackPose.weight(44)==0,"blend into and out of real pose");
  var out=Path.of("build/reports/combat-polish");Files.createDirectories(out);Files.writeString(out.resolve("shoulder-cameras.json"),new Gson().toJson(frames));
  System.out.println("PASS combat polish: "+checks+" checks; shoulder enemy framing, 30-tick alert, aim rays and weapon continuity. Offline only.");
 }
}
