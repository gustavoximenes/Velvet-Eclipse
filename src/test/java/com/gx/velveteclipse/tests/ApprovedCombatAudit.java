package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.exploration.WeaponModels;
import com.gx.velveteclipse.core.battle.WeaponAttackTiming;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.gx.velveteclipse.network.BattleRosterS2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import com.mojang.blaze3d.vertex.*;
import io.netty.buffer.Unpooled;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import java.util.zip.GZIPOutputStream;
import com.google.gson.Gson;
public final class ApprovedCombatAudit {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Vector3f v(double x,double y,double z){return new Vector3f((float)x,(float)y,(float)z);}
 static class Mesh implements VertexConsumer {
  List<Map<String,Object>> quads=new ArrayList<>();List<double[]> vertices=new ArrayList<>();double[] p;String material="skin";
  public VertexConsumer m_5483_(double x,double y,double z){p=new double[]{x,y,z,0,0};return this;}public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}
  public VertexConsumer m_7421_(float u,float v){p[3]=u;p[4]=v;return this;}public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float a,float b,float c){return this;}
  public void m_5752_(){vertices.add(p);if(vertices.size()==4){quads.add(Map.of("points",vertices,"material",material));vertices=new ArrayList<>();}}public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
 static ModelPart model(Family f,boolean arrowOnly)throws Exception{
  if(f!=Family.BOW){var m=WeaponModels.class.getDeclaredMethod("model",Family.class);m.setAccessible(true);return (ModelPart)m.invoke(null,f);}
  var in=ApprovedCombatAudit.class.getResourceAsStream("/assets/velvet_eclipse/weapons/bow.json");var data=new Gson().fromJson(new InputStreamReader(in),com.google.gson.JsonObject.class);var cubes=new ArrayList<ModelPart.Cube>();
  for(var el:data.getAsJsonArray("cubes")){var c=el.getAsJsonObject();String name=c.get("name").getAsString();boolean arrow=name.startsWith("arrow_")||name.equals("fletching");if(arrow!=arrowOnly||name.equals("taut_string"))continue;
   var a=c.getAsJsonArray("origin");var s=c.getAsJsonArray("size");var uv=c.getAsJsonArray("uv");cubes.add(new ModelPart.Cube(uv.get(0).getAsInt(),uv.get(1).getAsInt(),a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat(),s.get(0).getAsFloat(),s.get(1).getAsFloat(),s.get(2).getAsFloat(),0,0,0,false,128,128,EnumSet.allOf(Direction.class)));}
  in.close();return new ModelPart(cubes,Map.of());
 }
 static double[] arr(Vector3f p){return new double[]{p.x,p.y,p.z};}

 public static void main(String[] args)throws Exception{
  Path out=Path.of("build/reports/approved-combat/meshes");Files.createDirectories(out);
  var rigMethod=ApprovedAttackRenderer.class.getDeclaredMethod("rig",boolean.class);rigMethod.setAccessible(true);
  for(boolean slim:new boolean[]{false,true}){
   var rig=rigMethod.invoke(null,slim);var method=rig.getClass().getDeclaredMethod("limbs");method.setAccessible(true);var limbs=(RigidSkinLimb[])method.invoke(rig);
   for(var family:Family.values()){
    var frames=new ArrayList<Object>();var prop=model(family==Family.NONE?Family.SWORD:family,false);var arrow=family==Family.BOW?model(family,true):null;
    for(int n=-1;n<=58;n++){
     double t=n<0?-1:n/50.;var f=ApprovedWeaponPose.sample(family,t,slim);var mesh=new Mesh();
     ApprovedAttackRenderer.body(new PoseStack(),mesh,f,slim,Set.of(),15728880);
     double floor=mesh.quads.stream().flatMap(q->((List<double[]>)q.get("points")).stream()).mapToDouble(q->q[1]).max().orElseThrow();
     check(Math.abs(floor-f.feet())<.003,"feet grounded: "+family+" slim="+slim+" t="+t+" error="+(floor-f.feet()));
     for(int i=0;i<2;i++){
      boolean left=i==1;var arm=left?f.left():f.right();var hand=new Quaternionf().rotationX(arm.bend()).transform(v((left?1:-1)*(slim?.03125:.0625),.375,0)).add(0,.25f,0);
      arm.rotation().transform(hand);hand.add(left?.3125f:-.3125f,slim?.15625f:.125f,0);
      check(hand.distance(arm.hand())<.0001,"native wrist: "+family+" slim="+slim+" t="+t);
     }
     check(f.grip().isFinite()&&f.tip().isFinite()&&f.height()>=0&&f.height()<=.43,"finite clip/limited jump");
     check(f.weapon().lengthSquared()>.999&&f.weapon().lengthSquared()<1.001,"normalized weapon transform");
     var primary=f.body().transformPosition(new Vector3f((family==Family.BOW?f.left():f.right()).hand()));check(primary.distance(f.grip())<.0001,"weapon stays in actual hand");
     var ps=new PoseStack();ps.m_85837_(f.grip().x,f.grip().y,f.grip().z);ps.m_252781_(f.weapon());ps.m_85841_(f.scale(),f.scale(),f.scale());mesh.material="weapon_"+(family==Family.NONE?"sword":family.name().toLowerCase(Locale.ROOT));prop.m_104301_(ps,mesh,15728880,655360);
     if(family==Family.FISTS){var hand=f.body().transformPosition(new Vector3f(f.left().hand()));var direction=f.body().transformDirection(new Vector3f(f.left().forward()));ps=new PoseStack();ps.m_85837_(hand.x,hand.y,hand.z);ps.m_252781_(new Quaternionf().rotationTo(v(0,1,0),direction));ps.m_85841_(-f.scale(),f.scale(),f.scale());prop.m_104301_(ps,mesh,15728880,655360);}
     if(family==Family.BOW&&t>=.16&&t<.52){var at=new Vector3f(f.nock()).add(f.weapon().transform(v(-1,0,0)).mul(.625f*f.scale()));ps=new PoseStack();ps.m_85837_(at.x,at.y,at.z);ps.m_252781_(f.weapon());ps.m_85841_(f.scale(),f.scale(),f.scale());arrow.m_104301_(ps,mesh,15728880,655360);}
     for(var quad:mesh.quads)for(var vertex:(List<double[]>)quad.get("points"))for(double number:vertex)check(Double.isFinite(number),"rendered vertex finite");
     frames.add(Map.of("time",t,"height",f.height(),"advance",t<0?0:WeaponAttackTiming.advance(family,t/1.16*WeaponAttackTiming.seconds(family)*20),"quads",mesh.quads,"tip",arr(f.tip()),"leftHand",arr(f.body().transformPosition(new Vector3f(f.left().hand()))),"rightHand",arr(f.body().transformPosition(new Vector3f(f.right().hand()))),"string",List.of(arr(f.stringTop()),arr(f.nock()),arr(f.stringBottom())),"shot",arr(f.nock())));
    }
    if(!slim&&family!=Family.NONE)try(var writer=new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(out.resolve(family.name().toLowerCase(Locale.ROOT)+".json.gz"))),java.nio.charset.StandardCharsets.UTF_8)){new Gson().toJson(frames,writer);}
    double impact=family==Family.BOW?16:19;check(Math.abs(WeaponAttackTiming.authored(family,WeaponAttackTiming.battleElapsed(family,impact))-WeaponAttackTiming.authoredImpact(family))<1e-8,"battle impact uses approved hit frame");
    check(WeaponAttackTiming.battleStart(family)>0&&WeaponAttackTiming.battleStart(family)+WeaponAttackTiming.seconds(family)*20<30,"whole clip fits attack hold");
   }
  }
  var roster=new BattleRosterS2C(Arrays.stream(Family.values()).map(f->new BattleRosterS2C.Target(UUID.randomUUID(),f.ordinal(),f.name(),true,100,100,"orpheus","",false,50,50,f.name())).toList());
  var buffer=new FriendlyByteBuf(Unpooled.buffer());try{BattleRosterS2C.encode(roster,buffer);check(roster.equals(BattleRosterS2C.decode(buffer))&&!buffer.isReadable(),"remote weapons roundtrip");}finally{buffer.release();}
  var points=new ArrayList<Vec3>();for(double x:new double[]{-1.3,0,1.3})for(double y:new double[]{.05,2.0})points.add(new Vec3(x,y,6));
  for(double aspect:new double[]{4./3,16./9,21./9}){var cam=BattleFraming.command(Vec3.f_82478_,points,36,aspect,(a,b)->b);var subjects=new ArrayList<>(points);subjects.add(Vec3.f_82478_);subjects.add(new Vec3(0,1.95,0));check(BattleFraming.commandContains(subjects,cam.eye(),cam.focus(),cam.fov(),aspect),"camera includes player/enemies "+aspect);}
  System.out.println("PASS approved combat: "+checks+" checks. Native meshes, slim wrists, grounded feet, weapon anchors, clip timings, multiplayer roster and framing. Offline only.");
 }
}
