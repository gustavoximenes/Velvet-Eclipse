package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.presentation.*;
import com.gx.velveteclipse.client.vfx.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.AllOutPreferences;
import com.gx.velveteclipse.network.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import io.netty.buffer.Unpooled;
import com.mojang.blaze3d.vertex.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.Gson;

/** Regression checks for moving/killed targets, obstructed cameras, saved styles and real skin rigs. */
public final class BattleChoreographyAudit {
 private static int checks;
 private static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static final class Player {private final CompoundTag data=new CompoundTag();public CompoundTag getPersistentData(){return data;}}
 public static void main(String[] args)throws Exception{
  PersonaRegistry.bootstrap();var out=Path.of("build/reports/choreography");Files.createDirectories(out);
  var identity=UUID.randomUUID();var strike=new BattleLightningS2C.Strike(16000000.125,65,-16000000.375,42,11,identity);
  var anchor=new ImpactAnchor(strike);var moved=new Vec3(16000002.25,65,-16000001.125);
  check(anchor.update(null,null).equals(new Vec3(strike.x(),strike.y(),strike.z())),"unloaded target uses exact server position");
  check(anchor.update(identity,moved).equals(moved),"interpolated target motion updates anchor");
  check(anchor.update(null,null).equals(moved),"killing hit keeps last target position");
  check(anchor.update(UUID.randomUUID(),Vec3.f_82478_).equals(moved),"recycled numeric entity ID never redirects impact");
  var second=new ImpactAnchor(new BattleLightningS2C.Strike(3,65,7,43,12,UUID.randomUUID()));
  check(second.update(identity,moved).equals(new Vec3(3,65,7)),"each AoE target has independent identity");
  var b=new FriendlyByteBuf(Unpooled.buffer());try{
   for(int n=0;n<=5;n++){
    var p=new BattleSpellS2C("eiga",Collections.nCopies(n,strike));BattleSpellS2C.encode(p,b);check(p.equals(BattleSpellS2C.decode(b)),"spell target identity and precise coordinates roundtrip");
    var p2=new BattleLightningS2C(Collections.nCopies(n,strike));BattleLightningS2C.encode(p2,b);check(p2.equals(BattleLightningS2C.decode(b)),"lightning targets roundtrip");
   }
   for(var style:AllOutStyle.values())for(boolean victory:new boolean[]{false,true}){
    var p=new AllOutCueS2C(true,7,List.of(7,10,13),victory,style.id(),Map.of(7,"BOW",10,"FISTS",13,"RAPIER"));
    AllOutCueS2C.encode(p,b);check(p.equals(AllOutCueS2C.decode(b)),"party receives initiating player's style and each participant's weapon");
   }check(!b.isReadable(),"packet buffers consumed exactly");
  }finally{b.release();}
  var focus=new Vec3(0,1.2,0);var desired=new Vec3(1.6,1.85,3);
  check(BattleCameraSafety.choose(focus,desired,(a,z)->z).equals(desired),"unobstructed camera preserves authored shot");
  var clear=BattleCameraSafety.choose(focus,desired,(a,z)->z.f_82481_>0?a.m_165921_(z,.1):z);
  check(clear.f_82481_<=0&&clear.m_82554_(focus)>2.4,"camera rotates away from front obstruction");
  var enclosed=BattleCameraSafety.choose(focus,desired,(a,z)->a.m_165921_(z,.1));
  check(enclosed.m_82554_(focus)<.5,"enclosed room never forces camera through collision");
  for(double t=0;t<108;t+=.25){var s=BattleShot.sample("skill",t);if(t<45)check(s.depth()>2.7&&s.focusHeight()<1.3,"summon keeps waist-up distance and front angle");}
  check(MeleeTimeline.progress(MeleeTimeline.IMPACT)==1&&MeleeTimeline.progress(MeleeTimeline.END)==0,"enemy reaches target at hit and returns home");
  check(MeleeApproach.reachable(8,x->x<3)<3,"melee cannot cross obstacle");
  var player=new Player();check(AllOutPreferences.get(player)==AllOutStyle.MAKOTO,"default style migrates old players");
  for(var style:AllOutStyle.values()){
   check(AllOutPreferences.set(player,style.id())&&AllOutPreferences.get(player)==style,"style saved on player");
   var clone=new Player();AllOutPreferences.copy(player,clone);check(AllOutPreferences.get(clone)==style,"style survives respawn");
   for(double t=12;t<176;t+=.25)for(String bone:List.of("head","right_arm","left_arm","right_leg","left_leg")){
    var p=AllOutPose.rotation(bone,t,true,style);var before=AllOutPose.rotation(bone,t-.01,true,style);
    check(Float.isFinite(p.getX()+p.getY()+p.getZ()+AllOutPose.bend(bone,t,true,style)),"finite style pose");
    check(Math.abs(p.getX()-before.getX())<.04&&Math.abs(p.getY()-before.getY())<.04&&Math.abs(p.getZ()-before.getZ())<.04,"no visible pose snap: "+style+" "+t+" "+bone);
   }
   for(boolean slim:new boolean[]{false,true})for(int t:new int[]{24,50,102,145}){
    var mesh=new Mesh();AllOutPlayerRenderer.body(new PoseStack(),mesh,slim,Set.of(),t,true,style);
    check(mesh.quads.size()>30,"standard/slim native mesh emitted");
    for(var q:mesh.quads)for(var v:q)check(Double.isFinite(v[0]+v[1]+v[2])&&v[3]>=0&&v[3]<=1&&v[4]>=0&&v[4]<=1,"valid real skin coordinates");
    Files.writeString(out.resolve("pose-"+style.id()+"-"+t+(slim?"-slim":"")+".json"),new Gson().toJson(mesh.quads));
   }
   var canvas=new Preview();AllOutPresentation.victoryBackdrop(canvas,900,500,"PLAYER",style);canvas.save(out.resolve(style.id()+"-back.png"));
   canvas=new Preview();AllOutPresentation.finisherText(canvas,900,500,style);canvas.save(out.resolve(style.id()+"-text.png"));
  }
  check(!AllOutPreferences.set(player,"invalid")&&AllOutPreferences.get(player)==AllOutStyle.MITSURU,"invalid request preserves preference");
  var shapes=new LinkedHashMap<String,Object>();
  for(String skill:List.of("agi","agidyne","eiga","garudyne","bufudyne","kougaon","ziodyne","freidyne","psiodyne","megidolaon")){
   check(SpellVisuals.supported(skill),"preview uses a registered active spell: "+skill);
   var family=SpellVisuals.family(skill);int visible=0;
   for(double age=0;age<29;age+=.5){var strokes=SpellCoreRenderer.geometry(skill,family,SpellVisuals.scale(skill),age,42);visible+=strokes.size();
    for(var s:strokes){check(Float.isFinite(s.from().length()+s.to().length()+s.width()+s.alpha()),"finite VFX body");check(s.from().length()<15&&s.to().length()<15&&s.alpha()>0&&s.alpha()<=1,"bounded geometry around target feet");}
   }
   check(visible>0&&SpellCoreRenderer.geometry(skill,family,SpellVisuals.scale(skill),60,42).isEmpty(),"visible spell expires");
   shapes.put(skill,SpellCoreRenderer.geometry(skill,family,SpellVisuals.scale(skill),7,42));
  }
  Files.writeString(out.resolve("effects.json"),new Gson().toJson(shapes));
  System.out.println("PASS Battle choreography: "+checks+" checks; selected/moving/killed targets, camera obstruction, saved styles, party weapons, standard/slim skin meshes and spell geometry. Offline only.");
 }
 private static final class Preview implements HudDesign.Canvas{
  final java.awt.image.BufferedImage image=new java.awt.image.BufferedImage(900,500,2);final java.awt.Graphics2D g=image.createGraphics();final java.awt.Font display;
  Preview()throws Exception{display=java.awt.Font.createFont(0,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,java.awt.RenderingHints.VALUE_ANTIALIAS_ON);}
  public void fill(int x,int y,int w,int h,int color){g.setColor(new java.awt.Color(color,true));g.fillRect(x,y,w,h);}
  public void poly(double[] p,int c){var shape=new java.awt.geom.Path2D.Double();shape.moveTo(p[0],p[1]);for(int i=2;i<p.length;i+=2)shape.lineTo(p[i],p[i+1]);shape.closePath();g.setColor(new java.awt.Color(c,true));g.fill(shape);}
  public int width(String text){return text.length()*6;}
  public void text(String t,int x,int y,int color){angled(t,x,y,1,color,0);}
  public void angled(String text,int x,int y,float scale,int color,float angle){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setFont(display.deriveFont(scale*11));g.setColor(new java.awt.Color(color,true));g.drawString(text,0,g.getFontMetrics().getAscent());g.setTransform(old);}
  void save(Path p)throws Exception{javax.imageio.ImageIO.write(image,"png",p.toFile());g.dispose();}
 }
 private static final class Mesh implements VertexConsumer {
  List<List<double[]>> quads=new ArrayList<>();List<double[]> vertices=new ArrayList<>();double[] point;
  public VertexConsumer m_5483_(double x,double y,double z){point=new double[]{x,y,z,0,0};return this;}
  public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){point[3]=u;point[4]=v;return this;}
  public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float a,float b,float c){return this;}
  public void m_5752_(){vertices.add(point);if(vertices.size()==4){quads.add(vertices);vertices=new ArrayList<>();}}public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
 }
}
