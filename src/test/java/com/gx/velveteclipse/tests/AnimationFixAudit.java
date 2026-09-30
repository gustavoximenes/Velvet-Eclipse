package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.exploration.ExplorationRules;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.nio.file.*;
import java.util.function.BiFunction;
import com.google.gson.Gson;

public final class AnimationFixAudit {
 static int checks;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static double[] screen(Vec3 p,BattleFraming.View camera,double aspect){
  var back=camera.eye().m_82546_(camera.focus()).m_82541_();var right=new Vec3(back.f_82481_,0,-back.f_82479_).m_82541_();var up=back.m_82537_(right);var d=p.m_82546_(camera.eye());double z=-d.m_82526_(back),tan=Math.tan(Math.toRadians(18));
  return new double[]{(1+d.m_82526_(right)/(z*tan*aspect))/2,(1-d.m_82526_(up)/(z*tan))/2};
 }
 static List<double[]> vertices(ApprovedCombatAudit.Mesh m){var out=new ArrayList<double[]>();for(var q:m.quads)out.addAll((List<double[]>)q.get("points"));return out;}
 static double distance(double[] a,double[] b){double sum=0;for(int i=0;i<3;i++)sum+=(a[i]-b[i])*(a[i]-b[i]);return Math.sqrt(sum);}
 public static void main(String[] args)throws Exception{
  for(int width:new int[]{3,4})for(float inflate:new float[]{0,.25f})for(boolean leg:new boolean[]{false,true}){
   var limb=new RigidSkinLimb(40,16,-2,leg?0:-2,-2,width,12,4,inflate,leg?6:4);
   var base=new ApprovedCombatAudit.Mesh();limb.render(new PoseStack(),base,0,15728880);var initial=vertices(base);
   for(int degrees=-110;degrees<=120;degrees+=10){var bent=new ApprovedCombatAudit.Mesh();limb.render(new PoseStack(),bent,(float)Math.toRadians(degrees),15728880);var actual=vertices(bent);check(actual.size()==initial.size(),"fixed topology");
    for(int q=0;q<initial.size();q+=4)for(int a=0;a<4;a++)for(int b=a+1;b<4;b++)check(Math.abs(distance(initial.get(q+a),initial.get(q+b))-distance(actual.get(q+a),actual.get(q+b)))<1e-6,"joint must never stretch a skin segment");
   }
  }
  var enemies=new ArrayList<Vec3>();for(double x:new double[]{-1.8,0,1.8})for(double y:new double[]{.05,2.5})enemies.add(new Vec3(x,y,6));
  List<BiFunction<Vec3,Vec3,Vec3>> terrain=List.of((a,b)->b,(a,b)->b.f_82479_<-2&&a.f_82479_>=-2?a.m_165921_(b,(-2-a.f_82479_)/(b.f_82479_-a.f_82479_)):b,(a,b)->b.f_82481_<-3&&a.f_82481_>=-3?a.m_165921_(b,(-3-a.f_82481_)/(b.f_82481_-a.f_82481_)):b);
  var views=new ArrayList<Object>();
  for(double aspect:new double[]{4./3,16./9,21./9})for(int wall=0;wall<terrain.size();wall++){
   var view=BattleFraming.command(Vec3.f_82478_,enemies,36,aspect,terrain.get(wall));var waist=screen(new Vec3(0,.95,0),view,aspect);double unit=Math.min(aspect/640,1./360);
   check(Math.abs(waist[0]-125*unit/aspect)<.00001,"terrain preserves actor horizontal HUD anchor");
   check(Math.abs(waist[1]-(1-360*unit+230*unit))<.00001,"terrain preserves actor vertical HUD anchor");
   check(view.eye().m_82554_(new Vec3(0,1.1,0))>3,"no close-up camera inside actor");
   check(BattleFraming.commandContains(enemies,view.eye(),view.focus(),36,aspect),"enemies stay visible by side wall "+wall+" aspect "+aspect);
   views.add(Map.of("aspect",aspect,"wall",wall,"eye",List.of(view.eye().f_82479_,view.eye().f_82480_,view.eye().f_82481_),"focus",List.of(view.focus().f_82479_,view.focus().f_82480_,view.focus().f_82481_),"waist",waist));
  }
  check(ExplorationRules.ENTRY_READY<=10,"entry wipe ends within half a second of contact");
  Path output=Path.of("build/reports/animation-fix");Files.createDirectories(output);Files.writeString(output.resolve("camera.json"),new Gson().toJson(views));
  var frame=ApprovedWeaponPose.sample(com.gx.velveteclipse.equipment.EquipmentCatalog.Family.SWORD,-1,false);
  var pose=new PoseStack();pose.m_252781_(new org.joml.Quaternionf().rotationY((float)Math.toRadians(-80)));pose.m_85841_(ApprovedAttackRenderer.PLAYER_SCALE,ApprovedAttackRenderer.PLAYER_SCALE,ApprovedAttackRenderer.PLAYER_SCALE);pose.m_85837_(0,frame.feet(),0);pose.m_85841_(1,-1,-1);
  var hull=new CommandOutline.Hull();var mesh=new ApprovedCombatAudit.Mesh();ApprovedAttackRenderer.body(pose,hull,frame,false,Set.of(),15728880);
  mesh.material="outline";hull.emit(mesh);int count=mesh.quads.size();mesh.material="skin";ApprovedAttackRenderer.body(pose,mesh,frame,false,Set.of(),15728880);
  check(count*2==mesh.quads.size(),"outline shares actor topology");
  for(int face=0;face<count;face++){var a=(List<double[]>)mesh.quads.get(face).get("points");var b=(List<double[]>)mesh.quads.get(face+count).get("points");for(int i=0;i<4;i++)check(Math.abs(distance(a.get(3-i),b.get(i))-.025)<.00001,"outline reverses and offsets each vertex");}
  pose.m_85837_(frame.grip().x,frame.grip().y,frame.grip().z);pose.m_252781_(frame.weapon());pose.m_85841_(frame.scale(),frame.scale(),frame.scale());mesh.material="weapon_sword";ApprovedCombatAudit.model(com.gx.velveteclipse.equipment.EquipmentCatalog.Family.SWORD,false).m_104301_(pose,mesh,15728880,655360);
  Files.writeString(output.resolve("command.json"),new Gson().toJson(mesh.quads));
  System.out.println("PASS animation fix: "+checks+" checks. Rigid segment lengths, slim/clothing, HUD anchor under clipping, enemy visibility and entry duration. Offline only.");
 }
}
