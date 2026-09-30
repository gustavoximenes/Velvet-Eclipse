package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import java.lang.Math;
import java.util.*;
import java.nio.file.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.google.gson.Gson;

public final class PoseConventionsAudit {
 static int checks;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  int inside=0;double mostTwist=1;double maximumStep=0;String worstStep="";var collisions=new ArrayList<Object>();
  for(boolean slim:new boolean[]{false,true})for(var family:Family.values())for(int i=0;i<=116;i++){
   var frame=ApprovedWeaponPose.sample(family,i*.01,slim);
   for(var arm:List.of(frame.right(),frame.left())){check(arm.bend()<=.00001,"elbow must flex in normal-sword direction: "+family);check(arm.bend()>-Math.toRadians(125),"bounded elbow flexion");mostTwist=Math.min(mostTwist,arm.rotation().transform(new Vector3f(1,0,0)).x);}
   if(family!=Family.SWORD&&family!=Family.NONE&&family!=Family.KNIFE)for(boolean left:new boolean[]{false,true}){
    var arm=left?frame.left():frame.right();var elbow=arm.rotation().transform(new Vector3f((left?1:-1)*(slim?.03125f:.0625f),.25f,0)).add(left?.3125f:-.3125f,slim?.15625f:.125f,0);
    if(Math.abs(elbow.x)<.25&&elbow.y>0&&elbow.y<.75&&Math.abs(elbow.z)<.125){inside++;collisions.add(Map.of("family",family.name(),"time",i*.01,"slim",slim,"left",left,"elbow",List.of(elbow.x,elbow.y,elbow.z),"hand",List.of(arm.hand().x,arm.hand().y,arm.hand().z)));}
   }
  }
  Files.createDirectories(Path.of("build/reports/pose-conventions"));Files.writeString(Path.of("build/reports/pose-conventions/collisions.json"),new Gson().toJson(collisions));
  check(inside==0,"IK elbows enter torso: "+inside);
  for(boolean slim:new boolean[]{false,true})for(var family:Family.values()){
   var previous=ApprovedWeaponPose.sample(family,0,slim);
   for(int i=1;i<=232;i++){var current=ApprovedWeaponPose.sample(family,i*.005,slim);
    double turn=2*Math.acos(Math.min(1,Math.min(Math.abs(current.right().rotation().dot(previous.right().rotation())),Math.abs(current.left().rotation().dot(previous.left().rotation())))));
    if(turn>maximumStep){maximumStep=turn;worstStep=family+" t="+(i*.005)+" slim="+slim;
     Files.writeString(Path.of("build/reports/pose-conventions/step.json"),new Gson().toJson(Map.of("family",family,"time",i*.005,"slim",slim,"turn",Math.toDegrees(turn),"before",previous,"after",current)));
    }
    previous=current;
   }
  }
  check(maximumStep<Math.toRadians(20),"continuous elbow pole: "+Math.toDegrees(maximumStep)+" degrees/5ms "+worstStep);
  for(boolean slim:new boolean[]{false,true}){
   var sword=ApprovedWeaponPose.sample(Family.SWORD,.46,slim);var knife=ApprovedWeaponPose.sample(Family.KNIFE,.58,slim);
   check(Math.abs(sword.right().rotation().dot(knife.right().rotation()))>.99999&&sword.grip().distance(knife.grip())<.00001,"knife retains sword contact pose at knife server impact");
  }
  var animations=new ArrayList<Object>();
  for(boolean slim:new boolean[]{false,true})for(var family:Family.values())for(int i=0;i<32;i++){
   double phase=i*Math.PI/16;var frame=WeaponLocomotion.sample(family,slim,phase,.8);
   var look=WeaponLocomotion.look(frame,45*Math.sin(phase),25*Math.cos(phase));
   var worldHead=look.body().getNormalizedRotation(new Quaternionf()).mul(look.head());
   var expected=new Quaternionf().rotationY((float)Math.toRadians(45*Math.sin(phase))).rotateX((float)Math.toRadians(25*Math.cos(phase)));
   check(Math.abs(worldHead.dot(expected))>.99999,"head follows view despite torso rotation");
   check(look.body().equals(frame.body())&&look.grip().equals(frame.grip()),"look must not move weapon or torso");
   var mesh=new ApprovedCombatAudit.Mesh();ApprovedAttackRenderer.body(new PoseStack(),mesh,look,slim,Set.of(),15728880);
   double bottom=AnimationFixAudit.vertices(mesh).stream().mapToDouble(p->p[1]).max().orElseThrow();check(Math.abs(bottom-look.feet())<.003,"walk feet grounded");
   if(!slim&&family==Family.SWORD){var pose=new PoseStack();pose.m_85837_(look.grip().x,look.grip().y,look.grip().z);pose.m_252781_(look.weapon());pose.m_85841_(look.scale(),look.scale(),look.scale());mesh.material="weapon_sword";ApprovedCombatAudit.model(Family.SWORD,false).m_104301_(pose,mesh,15728880,655360);animations.add(Map.of("quads",mesh.quads,"feet",look.feet()));}
  }
  var enemies=List.of(new Vec3(-1.8,0,6),new Vec3(1.8,2.5,6));var state=new CommandCameraState();
  var first=state.update(Vec3.f_82478_,enemies,36,16./9,1,0,(a,b)->b);
  for(int i=1;i<240;i++){var p=new Vec3(Math.sin(i)*.03,Math.cos(i)*.002,Math.sin(i*.7)*.03);var view=state.update(p,enemies,36,16./9,1,i,(a,b)->b);check(view==first,"network micro-corrections must not reselect camera");}
  var updated=state.update(new Vec3(0,0,1),enemies,36,16./9,1,250,(a,b)->b);check(updated!=first,"actual reposition invalidates command camera");
  double spread=WeaponLocomotion.sample(Family.SWORD,false,Math.PI/2,1).left().rotation().dot(WeaponLocomotion.sample(Family.SWORD,false,Math.PI*1.5,1).left().rotation());check(Math.abs(spread)<.98,"walk arms counter-swing instead of freezing");
  check(CommandOutline.WIDTH<=.01,"subtle outline width");
  var path=Path.of("build/reports/pose-conventions");Files.createDirectories(path);Files.writeString(path.resolve("walk.json"),new Gson().toJson(animations));
  System.out.println("PASS pose conventions: "+checks+" checks; elbow direction/torso clearance, walking support, live head look, stable command camera. Maximum arm step="+Math.toDegrees(maximumStep)+" deg. Lowest hinge X="+mostTwist+". Offline only.");
 }
}
