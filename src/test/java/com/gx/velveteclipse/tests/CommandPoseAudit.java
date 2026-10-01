package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import java.lang.Math;
import java.util.*;
import java.nio.file.*;
import com.google.gson.Gson;

public final class CommandPoseAudit {
 static int checks;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  var output=new ArrayList<Object>();
  for(boolean slim:new boolean[]{false,true})for(var family:Family.values()){
   var f=BattleCommandPose.sample(family,slim);var approved=ApprovedWeaponPose.sample(Family.SWORD,-1,slim);
   check(f.body().equals(approved.body())&&f.head().equals(approved.head()),"approved command torso/head: "+family);
   check(f.rightLeg().equals(approved.rightLeg())&&f.leftLeg().equals(approved.leftLeg()),"approved grounded stance");
   check(f.height()==0&&f.rightKnee()==0&&f.leftKnee()==0,"command does not prepare a jump");
   check(f.right().hand().x<-.25&&f.left().hand().x>.25,"relaxed hands stay on their own side: "+family);
   check(f.right().hand().y>.55&&f.left().hand().y>.50,"hands stay below chest: "+family);
   check(Math.abs(f.right().bend())<Math.toRadians(25)&&Math.abs(f.left().bend())<Math.toRadians(25),"no folded attack elbows in command");
   var grip=f.body().transformPosition(new Vector3f((family==Family.BOW?f.left():f.right()).hand()));
   check(grip.distance(f.grip())<.00001,"weapon attached to command hand");
   var mesh=new ApprovedCombatAudit.Mesh();var pose=new PoseStack();
   ApprovedAttackRenderer.body(pose,mesh,f,slim,Set.of(),15728880);
   double feet=AnimationFixAudit.vertices(mesh).stream().mapToDouble(v->v[1]).max().orElseThrow();check(Math.abs(feet-f.feet())<.003,"grounded command feet");
   var actualFamily=family==Family.NONE?Family.SWORD:family;mesh.material="weapon_"+actualFamily.name().toLowerCase(Locale.ROOT);
   pose.m_85837_(f.grip().x,f.grip().y,f.grip().z);pose.m_252781_(f.weapon());pose.m_85841_(f.scale(),f.scale(),f.scale());ApprovedCombatAudit.model(actualFamily,false).m_104301_(pose,mesh,15728880,655360);
   if(family==Family.FISTS){var hand=f.body().transformPosition(new Vector3f(f.left().hand()));var direction=f.body().transformDirection(new Vector3f(f.left().forward()));pose=new PoseStack();pose.m_85837_(hand.x,hand.y,hand.z);pose.m_252781_(new Quaternionf().rotationTo(new Vector3f(0,1,0),direction));pose.m_85841_(-f.scale(),f.scale(),f.scale());ApprovedCombatAudit.model(family,false).m_104301_(pose,mesh,15728880,655360);}
   double lowest=AnimationFixAudit.vertices(mesh).stream().mapToDouble(v->v[1]).max().orElseThrow();check(f.feet()-lowest>-.025,"carried weapon clears floor: "+family+" "+(f.feet()-lowest));
   if(!slim&&family!=Family.NONE)output.add(Map.of("family",family.name().toLowerCase(Locale.ROOT),"quads",mesh.quads,"feet",f.feet()));
  }
  check(Math.abs(BattleCommandPose.yaw(Vec3.f_82478_,new Vec3(0,0,6),0)-80)<.00001,"approved lateral formation orientation");
  check(Math.abs(BattleCommandPose.yaw(new Vec3(7,2,-4),new Vec3(13,3,-4),0)+10)<.00001,"formation rotation follows enemy side");
  check(BattleCommandPose.yaw(new Vec3(1,2,3),new Vec3(1,8,3),27)==27,"safe coincident formation fallback");
  var folder=Path.of("build/reports/command-pose");Files.createDirectories(folder);Files.writeString(folder.resolve("meshes.json"),new Gson().toJson(output));
  System.out.println("PASS command pose: "+checks+" checks. Approved lateral stance, relaxed hands, native weapon attachment and floor clearance; no JAR generated.");
 }
}
