package com.gx.velveteclipse.client.presentation;

import com.google.gson.Gson;
import com.gx.velveteclipse.client.exploration.WeaponModels;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.*;
import java.lang.Math;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.GZIPOutputStream;
import static com.gx.velveteclipse.client.presentation.ApprovedWeaponPose.*;

/** Audit-only choreography: never referenced by the runtime or packaged in the mod. */
public final class EntryAuditExport {
 static class Mesh implements VertexConsumer {
  List<Map<String,Object>> quads=new ArrayList<>();List<double[]> points=new ArrayList<>();double[] point;String material="skin";
  public VertexConsumer m_5483_(double x,double y,double z){point=new double[]{x,y,z,0,0};return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}
  public VertexConsumer m_7421_(float u,float v){point[3]=u;point[4]=v;return this;}
  public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}
  public VertexConsumer m_5601_(float x,float y,float z){return this;}
  public void m_5752_(){points.add(point);if(points.size()==4){quads.add(Map.of("points",points,"material",material));points=new ArrayList<>();}}
  public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
 static Arm arm(boolean left,boolean slim,double x,double y,double z,double bend){
  var q=new Quaternionf().rotationZYX(rad(z),rad(y),rad(x));float b=rad(bend);
  var hand=shoulder(left,slim).add(q.transform(v((left?1:-1)*(slim?.03125:.0625),.25,0))).add(new Quaternionf(q).rotateX(b).transform(v(0,.375,0)));
  return new Arm(q,b,hand,new Quaternionf(q).rotateX(b).transform(v(0,1,0)));
 }
 static Frame run(double time,boolean slim){
  double phase=time*2*Math.PI*2.05,stride=Math.sin(phase);
  // Forward lean in the runner's sagittal plane; no sideways body roll.
  var body=new Matrix4f().translation(0,.75f,0).rotate(new Quaternionf().rotationZYX(0,rad(stride*4),rad(12))).translate(0,-.75f,0);
  var right=arm(false,slim,-14-stride*21,-9,13,-38-Math.max(0,stride)*19);
  var left=arm(true,slim,20+stride*46,10,-20,-68+stride*17);
  var rl=v(stride*53,0,6);var ll=v(-stride*53,0,-6);
  float rk=rad(18+Math.max(0,-stride)*66),lk=rad(18+Math.max(0,stride)*66);
  var grip=body.transformPosition(new Vector3f(right.hand()));
  var direction=body.transformDirection(v(-.15,.70,-.70).normalize());
  var weapon=new Quaternionf().rotationTo(v(0,1,0),direction);
  var tip=new Matrix4f().translation(grip).rotate(weapon).scale(.82f).transformPosition(v(0,1.625,0));
  return new Frame(body,new Quaternionf().rotationX(rad(-9)),right,left,rl,ll,rk,lk,grip,weapon,.82f,tip,grip,grip,grip,.045+.03*Math.cos(phase*2),feet(body,rl,ll,rk,lk));
 }
 static Frame guard(boolean slim){
  var body=new Matrix4f().translation(0,.75f,0).rotateX(rad(12)).translate(0,-.75f,0);
  var right=arm(false,slim,-50,-22,18,-65);var left=arm(true,slim,-55,18,-25,-75);
  var rl=v(-22,0,8);var ll=v(16,0,-8);float rk=rad(35),lk=rad(21);
  var grip=body.transformPosition(new Vector3f(right.hand()));var weapon=new Quaternionf().rotationTo(v(0,1,0),v(.16,-.94,-.30).normalize());
  var tip=new Matrix4f().translation(grip).rotate(weapon).scale(.82f).transformPosition(v(0,1.625,0));
  return new Frame(body,new Quaternionf().rotationX(rad(-8)),right,left,rl,ll,rk,lk,grip,weapon,.82f,tip,grip,grip,grip,0,feet(body,rl,ll,rk,lk));
 }
 public static void main(String[] args)throws Exception{
  var method=WeaponModels.class.getDeclaredMethod("model",Family.class);method.setAccessible(true);var sword=(ModelPart)method.invoke(null,Family.SWORD);
  var result=new LinkedHashMap<String,Object>();int checks=0;
  for(boolean slim:new boolean[]{false,true})for(String clip:List.of("run","command","guard","current")){
   var frames=new ArrayList<Object>();
   for(int n=0;n<60;n++){
    double t=n/30.;var frame=switch(clip){case "run"->run(t,slim);case "guard"->guard(slim);case "current"->ApprovedAttackRenderer.travelPose(Family.SWORD,slim,t*20*.52,1);default->BattleCommandPose.sample(Family.SWORD,slim);};
    var mesh=new Mesh();ApprovedAttackRenderer.body(new PoseStack(),mesh,frame,slim,Set.of(),15728880);
    var ps=new PoseStack();ps.m_85837_(frame.grip().x,frame.grip().y,frame.grip().z);ps.m_252781_(frame.weapon());ps.m_85841_(frame.scale(),frame.scale(),frame.scale());mesh.material="weapon_sword";sword.m_104301_(ps,mesh,15728880,655360);
    for(var q:mesh.quads)for(var p:(List<double[]>)q.get("points"))for(double a:p){if(!Double.isFinite(a))throw new AssertionError("Non-finite audit mesh");checks++;}
    if(frame.body().transformPosition(new Vector3f(frame.right().hand())).distance(frame.grip())>.00001)throw new AssertionError("Disconnected weapon");
    frames.add(Map.of("time",t,"feet",frame.feet(),"height",frame.height(),"quads",mesh.quads));
   }
   result.put(clip+(slim?"-slim":""),frames);
  }
  Path out=Path.of("build/reports/entry-audit-01/native-meshes.json.gz");Files.createDirectories(out.getParent());
  try(var w=new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(out)),java.nio.charset.StandardCharsets.UTF_8)){new Gson().toJson(result,w);}
  System.out.println("PASS audit meshes: "+checks+" finite coordinates/UVs; 480 native frames; standard/slim; weapon attachment. Draft only, no runtime entry changes.");
 }
}
