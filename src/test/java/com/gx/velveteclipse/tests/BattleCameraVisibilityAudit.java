package com.gx.velveteclipse.tests;
import java.util.*;
import java.nio.file.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.tartarus.CutawayQuads;
import com.gx.velveteclipse.client.exploration.CameraPath;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.raid.*;

public final class BattleCameraVisibilityAudit {
 static int checks;static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {visibility();cutaway();cache();train();System.out.println("PASS battle camera/visibility: "+checks+" checks. Local cutaway restoration, observer isolation, late rosters and actual train voxels; offline only.");}
 static void visibility(){
  for(int count:new int[]{1,4,12}){
   var roster=new ArrayList<BattleRosterS2C.Target>();for(int i=0;i<count;i++)roster.add(new BattleRosterS2C.Target(UUID.randomUUID(),100+i,"Player",true,100,100,"pixie","",i==0,50,50,"SWORD"));
   for(int i=0;i<5;i++)roster.add(new BattleRosterS2C.Target(UUID.randomUUID(),200+i,"Shadow",false,100,100,"cowardly_maya","",false,50,50,"SWORD"));
   var stale=new BattleSceneS2C.Scene(UUID.randomUUID(),"Player",0,65,0,3,true,List.of(200,201,202,203,204));var other=new BattleSceneS2C.Scene(UUID.randomUUID(),"Other",40,65,0,3,true,List.of(300));
   for(int i=200;i<205;i++){check(!EncounterVisibility.hidden(i,true,roster,List.of(stale,other)),"own enemy never hidden by stale observer scene");check(EncounterVisibility.hidden(i,false,List.of(),List.of(stale)),"outside player continues seeing encounter smoke");}
   check(EncounterVisibility.hidden(300,true,roster,List.of(stale,other)),"another battle still hidden");check(!EncounterVisibility.participant(-1,true,roster),"unspawned IDs are not actors");
   check(EncounterVisibility.ownScene(stale,true,roster)&&!EncounterVisibility.ownScene(other,true,roster),"smoke excluded only for own scene");
  }
 }
 static BakedQuad quad(float[][] points){int[] data=new int[32];for(int i=0;i<4;i++){int o=i*8;for(int a=0;a<3;a++)data[o+a]=Float.floatToRawIntBits(points[i][a]);data[o+3]=0xffeeffbb;data[o+4]=Float.floatToRawIntBits((i==1||i==2)?.8f:.2f);data[o+5]=Float.floatToRawIntBits(i>=2?.9f:.1f);data[o+6]=240;data[o+7]=127;}return new BakedQuad(data,-1,Direction.NORTH,null,false,false);}
 static void cutaway(){
  var origin=new BlockPos(256,64,-16);var volume=new CutawayVolume(new Vec3(256,67,-20),List.of(new Vec3(256,65.4,-12),new Vec3(256,67,-12)),65.12);
  var wall=quad(new float[][]{{-8,1,0},{8,1,0},{8,10,0},{-8,10,0}});var floor=quad(new float[][]{{-8,1,-8},{8,1,-8},{8,1,8},{-8,1,8}});var back=quad(new float[][]{{-8,1,9},{8,1,9},{8,10,9},{-8,10,9}});
  var source=List.of(wall,floor,back);var original=wall.m_111303_().clone();var local=CutawayQuads.filter(source,origin,List.of(volume));
  check(!local.contains(wall)&&local.contains(floor)&&local.contains(back),"only blocking wall clipped; floor/back wall preserved");
  check(local.size()>3&&local.size()<2000,"partial wall survives as bounded native triangles");
  check(Arrays.equals(original,wall.m_111303_()),"source mesh not mutated for other clients or cached render passes");
  check(CutawayQuads.filter(source,origin,List.of())==source,"player outside battle retains exact complete mesh");
  check(CutawayQuads.filter(source,null,List.of(volume))==source,"inventory and other dimensions never clipped");
  check(CutawayQuads.filter(source,origin,List.of())==source,"restoration uses the original mesh");
  for(var q:local)for(int v=0;v<4;v++){var data=q.m_111303_();for(int c:new int[]{0,1,2,4,5})check(Float.isFinite(Float.intBitsToFloat(data[v*8+c])),"finite interpolated geometry/UV");check(data[v*8+6]==240,"emission/light preserved in split geometry");}
 }
 static void cache(){
  var state=new CommandCameraState();var p=new Vec3(0,65,0);var late=List.of(new Vec3(-1,65,7),new Vec3(1,68,7));
  var first=state.update(p,List.of(),36,16./9,7,0,(a,b)->b);var filled=state.update(p,late,36,16./9,7,1,(a,b)->b);check(first!=filled,"late tracked enemy invalidates frame immediately");
  for(int tick=2;tick<100;tick++)check(state.update(p,late.stream().map(v->v.m_82520_(.04,0,-.03)).toList(),36,16./9,7,tick,(a,b)->b)==filled,"network jitter keeps same command framing");
  var replacement=state.update(p,late,36,16./9,8,100,(a,b)->b);check(replacement!=filled,"replacement actor ID invalidates owner key");state.reset();check(state.view()==null,"new encounter starts without old frame");
 }
 static final Map<String,TrainLayout.Cell> cells=new HashMap<>();
 static double hit(Vec3 a,Vec3 b){
  double length=a.m_82554_(b),nearest=1;
  for(int x=(int)Math.floor(Math.min(a.f_82479_,b.f_82479_));x<=Math.floor(Math.max(a.f_82479_,b.f_82479_));x++)for(int y=(int)Math.floor(Math.min(a.f_82480_,b.f_82480_));y<=Math.floor(Math.max(a.f_82480_,b.f_82480_));y++)for(int z=(int)Math.floor(Math.min(a.f_82481_,b.f_82481_));z<=Math.floor(Math.max(a.f_82481_,b.f_82481_));z++){
   var cell=cells.get(x+","+y+","+z);if(cell==null)continue;double inset=cell.material().equals("rail")?.375:0;
   double[] lo={x+inset,y,z+inset},hi={x+1-inset,y+1,z+1-inset},start={a.f_82479_,a.f_82480_,a.f_82481_},end={b.f_82479_,b.f_82480_,b.f_82481_};double enter=0,exit=1;
   for(int i=0;i<3&&enter<=exit;i++){double d=end[i]-start[i];if(Math.abs(d)<1e-9){if(start[i]<lo[i]||start[i]>hi[i]){enter=2;break;}}else{double t=(lo[i]-start[i])/d,u=(hi[i]-start[i])/d;enter=Math.max(enter,Math.min(t,u));exit=Math.min(exit,Math.max(t,u));}}
   if(enter<=exit)nearest=Math.min(nearest,enter);
  }
  return nearest==1?Double.POSITIVE_INFINITY:nearest*length;
 }
 static Vec3 clip(Vec3 a,Vec3 b){return CameraPath.clip(a,b,BattleCameraVisibilityAudit::hit);}
 static void train()throws Exception{
  var report=new ArrayList<Object>();for(var c:TrainLayout.build())if(!c.material().equals("air"))cells.put(c.x()+","+(64+c.y())+","+c.z(),c);
  for(int stage=0;stage<3;stage++){
   // Gates of previously completed cars are open in the real raid.
   for(int old=0;old<stage;old++)for(int x=-5;x<=5;x++)for(int y=65;y<71;y++)cells.remove(x+","+y+","+TrainLayout.gateZ(old));
   double z=TrainLayout.encounterZ(stage),radius=stage==2?3.4:.85,depth=stage==2?1.8:.85,height=stage==2?5.6:4;
   var enemies=new ArrayList<Vec3>();for(double x:new double[]{-radius,radius})for(double dz:new double[]{-depth,depth})for(double y:new double[]{.1,height})enemies.add(new Vec3(.5+x,65.05+y,z+.5+dz));
   for(int player=0;player<PriestessRules.MAX_CAPACITY;player++)for(double aspect:new double[]{4./3,16./9,21./9}){
    var feet=new Vec3(.5+(player%3-1)*2.1,65.05,z-7-(player/3)*2.1);var view=TrainBattleCamera.frame(feet,enemies,36,aspect,BattleCameraVisibilityAudit::clip);
    double width=TrainLayout.width((int)view.eye().f_82481_);check(view.eye().f_82479_>-width+1.16&&view.eye().f_82479_<width-.16&&view.eye().f_82480_>65.3&&view.eye().f_82480_<70.8,"camera stays inside train stage "+stage+" player "+player);
    check(clip(feet.m_82520_(0,1.4,0),view.eye()).m_82554_(view.eye())<.16,"no wall/seat/pole between player and camera");
    var subjects=new ArrayList<>(enemies);subjects.add(feet.m_82520_(0,.05,0));subjects.add(feet.m_82520_(0,1.95,0));
    check(BattleFraming.commandContains(subjects,view.eye(),view.focus(),view.fov(),aspect),"full player/enemy frame stage="+stage+" player="+player+" aspect="+aspect+" view="+view);
    for(var point:subjects)check(clip(point.f_82480_<feet.f_82480_+.3?point.m_82520_(0,.25,0):point,view.eye()).m_82554_(view.eye())<.23,"clear subject in train stage="+stage+" player="+player+" point="+point+" view="+view);
    report.add(Map.of("stage",stage,"player",player,"aspect",aspect,"eye",view.eye(),"focus",view.focus(),"fov",view.fov()));
   }
  }
  Path out=Path.of("build/reports/model-combat");Files.createDirectories(out);Files.writeString(out.resolve("train-cameras.json"),new com.google.gson.Gson().toJson(report));
 }
}
