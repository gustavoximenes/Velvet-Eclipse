package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.client.exploration.*;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.vfx.*;
import com.gx.velveteclipse.network.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
import java.util.*;
import java.nio.file.*;
import com.google.gson.Gson;

/** Terrain, whole-party framing and shared Shift cues, without a running game. */
public final class ExplorationStageAudit {
 private static int checks;
 private static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  var origin=new TerrainFormation.Point(0,64,0);var layouts=new LinkedHashMap<String,TerrainFormation.Layout>();
  for(int count=1;count<=5;count++)for(int yaw=-180;yaw<=180;yaw+=30){
   var clear=TerrainFormation.solve(origin,yaw,count,1.95,(x,z,y)->new TerrainFormation.Point(x,y,z),(a,b)->true);
   check(clear!=null&&clear.enemies().size()==count,"complete clear formation");check(clear.yaw()==yaw,"preserves clear facing");validate(clear);
  }
  // Lava / pits and solid pillars reject footprints; the row must reflow around them.
  for(int scenario=0;scenario<4;scenario++){
   final int s=scenario;
   var layout=TerrainFormation.solve(origin,0,5,1.95,(x,z,y)->{
    if(s==0&&x>-.9&&x<1.2&&z>3&&z<7)return null;
    if(s==1&&x<0)return null;
    if(s==2&&z>2)return null;
    return new TerrainFormation.Point(x,s==3&&x>0?y+1:y,z);
   },(a,b)->s!=2||b.z()<0);
   check(layout!=null,"available alternate formation "+scenario);validate(layout);layouts.put("terrain-"+scenario,layout);
   for(var p:layout.enemies())check(s!=0||!(p.x()>-.9&&p.x()<1.2&&p.z()>3&&p.z()<7),"no footprint in blocked region");
  }
  check(TerrainFormation.solve(origin,0,5,1.95,(x,z,y)->null,(a,b)->true)==null,"void/enclosed room cannot create a floating formation");
  check(TerrainFormation.solve(origin,0,5,1.95,(x,z,y)->new TerrainFormation.Point(x,y+3,z),(a,b)->true)==null,"no unrelated floor three blocks away");
  check(TerrainFormation.solve(origin,0,5,1.95,(x,z,y)->new TerrainFormation.Point(x,y,z),(a,b)->false)==null,"no enemies behind an opaque wall");
  var points=new ArrayList<Vec3>();for(int i=0;i<5;i++)for(double x:new double[]{-.85,.85})for(double z:new double[]{-.85,.85})for(double h:new double[]{0,2.5})points.add(new Vec3((i-2)*1.95+x,h,5.25+z));for(int i=0;i<4;i++){points.add(new Vec3((i-1.5)*1.8,0,0));points.add(new Vec3((i-1.5)*1.8,1.9,0));}
  var views=new ArrayList<BattleFraming.View>();
  for(double aspect:new double[]{4./3,16./9,21./9,1})for(double yaw:new double[]{0,45,90,180}){
   double a=Math.toRadians(yaw);var view=BattleFraming.fit(points,new Vec3(6*Math.sin(a),3,-6*Math.cos(a)),62,aspect,(from,to)->to);views.add(view);
   var back=view.eye().m_82546_(view.focus()).m_82541_();var right=new Vec3(back.f_82481_,0,-back.f_82479_).m_82541_();var up=back.m_82537_(right).m_82541_();double tangent=Math.tan(Math.toRadians(31));
   for(var p:points){var v=p.m_82546_(view.eye());double depth=-v.m_82526_(back);check(depth>0,"actor in front of camera");check(Math.abs(v.m_82526_(right)/(depth*tangent*aspect))<.85,"full party fits horizontal viewport");check(Math.abs(v.m_82526_(up)/(depth*tangent))<.75,"heads and feet fit vertical viewport");}
  }
  var trapped=BattleFraming.fit(points,new Vec3(2,3,-5),62,16./9,(a,b)->a.m_165921_(b,.1));check(Double.isFinite(trapped.eye().m_82553_()),"fully enclosed camera still finite");
  check(Math.abs(ExplorationMotion.turn(179,-179,27)-181)<.01,"travel heading wraps without full rotation");
  check(ExplorationMotion.turn(0,180,27)==27,"bounded turning speed");
  var idle=CameraPath.exploration(new Vec3(0,64,0),0,0,false,0);var running=CameraPath.exploration(new Vec3(0,64,0),0,0,false,1);check(running.desired().m_82554_(running.pivot())>idle.desired().m_82554_(idle.pivot()),"running opens the camera gently");
  for(boolean recipient:new boolean[]{false,true})for(String part:List.of("body","head","leftArm","rightArm","leftLeg","rightLeg")){
   var previous=ShiftPresentation.rotation(part,0,recipient);
   for(double t=.1;t<=ShiftPresentation.END;t+=.1){var current=ShiftPresentation.rotation(part,t,recipient);check(Float.isFinite(current.getX()+current.getY()+current.getZ()),"finite shift pose");check(Math.abs(current.getX()-previous.getX())<.06,"no gesture snap");previous=current;}
   check(Math.abs(previous.getX())<.001,"gesture returns to idle");
  }
  var buffer=new FriendlyByteBuf(Unpooled.buffer());try{var cue=new BattleDirectionS2C(new UUID(4,2),3,8,"shift","","SWORD",0,64,0,2,64,0,ShiftPresentation.END);BattleDirectionS2C.encode(cue,buffer);check(cue.equals(BattleDirectionS2C.decode(buffer)),"all clients receive identical giver, receiver and clock");check(!buffer.isReadable(),"no extra packet bytes");}finally{buffer.release();}
  for(double t=0;t<44;t+=.5){var strokes=SummonVisual.geometry(t,41);check(strokes.size()<500,"bounded summon geometry");for(var stroke:strokes){check(stroke.from().isFinite()&&stroke.to().isFinite()&&stroke.width()>0&&stroke.alpha()>0&&stroke.alpha()<=1,"valid summon vertices");check(stroke.from().y>=0&&stroke.to().y<4.5,"effect rises above feet");}if(t>=SummonVisual.LIFE)check(strokes.isEmpty(),"summon expires");}
  var out=Path.of("build/reports/exploration-stage");Files.createDirectories(out);var report=new LinkedHashMap<String,Object>();report.put("formations",layouts);report.put("cameras",views);var effects=new LinkedHashMap<String,Object>();for(int t:new int[]{2,8,16,26,36})effects.put("tick-"+t,SummonVisual.geometry(t,41));report.put("summon",effects);Files.writeString(out.resolve("review.json"),new Gson().toJson(report));
  System.out.println("PASS exploration stage: "+checks+" checks; terrain exclusion, 1..5 enemies, four camera aspects, turn heading, Shift gestures/codec and finite summon geometry. Offline only.");
 }
 private static void validate(TerrainFormation.Layout layout){var p=layout.enemies();for(int i=0;i<p.size();i++)for(int j=0;j<i;j++)check(Math.hypot(p.get(i).x()-p.get(j).x(),p.get(i).z()-p.get(j).z())>=1.9,"actors never overlap");}
}
