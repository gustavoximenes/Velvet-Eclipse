package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.party.PartyRoster;
import com.gx.velveteclipse.room.VelvetRoomLayout;
import com.gx.velveteclipse.core.fusion.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.client.vfx.SharedSummonFlames;
import com.gx.velveteclipse.client.menu.*;
import org.joml.Vector3f;
import java.util.*;
import java.util.List;
import java.nio.file.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import javax.imageio.ImageIO;

/** Focused behavioral checks and renders of production drawing code, without a game client. */
public final class VelvetReworkAudit {
 private static int checks;
 private static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  var roster=new PartyRoster();check(roster.capacity()==4,"default group size is four");roster.create("leader");
  for(int i=1;i<=3;i++){roster.invite("leader","p"+i,0);roster.accept("p"+i,1);}
  roster.invite("leader","blocked",2);check(roster.invitation("blocked",3)==null,"default admission stops at four");
  roster.capacity(6);roster.invite("leader","fifth",3);roster.invite("leader","sixth",3);roster.accept("fifth",4);roster.capacity(4);roster.accept("sixth",4);
  check(roster.group("sixth")==null,"pending invitation cannot bypass a reduced limit");check(roster.group("leader").members().size()==5,"lower limit preserves existing members");
  roster.capacity(6);roster.accept("sixth",5);check(roster.group("leader").members().size()==6,"raised capacity admits again");
  for(int limit:new int[]{1,4,5,8,9,16,64}){
   var seats=VelvetRoomLayout.seats(limit);check(seats.length==limit,"chair count follows limit");var unique=new HashSet<String>();var floor=new HashSet<String>();for(var cell:VelvetRoomLayout.cells(limit))if(cell.y()==63)floor.add(cell.x()+","+cell.z());
   for(var seat:seats){check(unique.add(Arrays.toString(seat)),"chairs do not overlap");check(floor.contains(seat[0]+","+seat[2]),"chairs have floor");check(seat[2]<VelvetRoomLayout.depth(limit)-2,"door stays behind all chairs");check(seat[0]!=0,"central exit aisle remains clear");}
  }
  PersonaRegistry.bootstrap();var id=UUID.randomUUID();var d=new PlayerPersonaData();d.setPlayerLevel(99);for(int i=0;i<8;i++)d.expandPersonaCapacity();for(String persona:List.of("pixie","apsaras","angel","silky","nekomata","jack_frost"))d.givePersona(persona);
  var inputs=d.rosterIds().stream().map(p->new PartyFusion.Source(id,p)).toList();var owners=Map.of(id,d);String before=FusionTransaction.fingerprint(d);var options=FusionSearch.available(owners,id,inputs);check(!options.isEmpty(),"result search finds available recipes");check(before.equals(FusionTransaction.fingerprint(d)),"search does not consume or modify inputs");
  for(var option:options){var p=PartyFusion.preview(owners,id,option.sources(),option.special(),List.of());check(p!=null&&p.allowed()&&p.result().definitionId().equals(option.id()),"search points to a valid authoritative recipe");}
  for(int i=0;i<inputs.size();i++)for(int j=i+1;j<inputs.size();j++){var p=PartyFusion.preview(owners,id,List.of(inputs.get(i),inputs.get(j)),"",List.of());if(p!=null&&p.allowed()){Set<PartyFusion.Source> pair=Set.of(inputs.get(i),inputs.get(j));check(options.stream().anyMatch(o->new HashSet<>(o.sources()).equals(pair)),"every available normal pair is searchable");}}
  var cooperative=new PlayerPersonaData();cooperative.setPlayerLevel(99);cooperative.givePersona("pixie");var owner=UUID.randomUUID();var withOther=new LinkedHashMap<UUID,PlayerPersonaData>(owners);withOther.put(owner,cooperative);var full=new ArrayList<>(inputs);full.add(new PartyFusion.Source(owner,"pixie"));check(FusionSearch.available(withOther,id,full).size()==options.size(),"equivalent donor copies do not duplicate a recipe");
  checkLevelVariants();
  String large="{\"persona\":\"Fusão / contribuição / Jack Frost\"}".repeat(45000);byte[] payload=FusionPayload.encode(large);check(payload.length<FusionPayload.WIRE_LIMIT&&FusionPayload.decode(payload).equals(large),"large recipe pages survive bounded UTF-8 network compression");
  try{FusionPayload.decode(new byte[]{1,2,3});throw new AssertionError("corrupt archive accepted");}catch(IllegalArgumentException expected){checks++;}
  var option=options.get(0);var preview=PartyFusion.preview(owners,id,option.sources(),option.special(),List.of());var inherited=preview.inheritable().stream().limit(2).toList();check(PartyFusion.commit(owners,id,option.sources(),option.special(),inherited),"valid fusion commits");for(String skill:inherited)check(d.instance(option.id()).learnedSkills().contains(skill),"chosen inheritance reaches actual result");check(!PartyFusion.commit(owners,id,option.sources(),option.special(),inherited),"consumed sources cannot be committed twice");
  for(double tick:new double[]{0,37,108,150})check(SharedSummonFlames.geometry(tick,new Vector3f(1,0,0),new Vector3f(0,0,-1)).isEmpty(),"effect has synchronized start and finish");
  for(double tick:new double[]{39,43,48,60,85,100,104,107}){
   var geometry=SharedSummonFlames.geometry(tick,new Vector3f(1,0,0),new Vector3f(0,0,-1));check(geometry.stream().anyMatch(f->f.color()==0x122BF5),"filled blue flame shells persist while summoned");check(geometry.stream().anyMatch(f->f.color()==0xC6FBFF),"white flame cores");
   for(var face:geometry){check(face.vertices().size()==4&&face.alpha()>=0&&face.alpha()<=1,"valid depth-tested flame face");for(var p:face.vertices())check(Float.isFinite(p.x+p.y+p.z)&&p.y>=0&&p.y<=3.3,"finite flame geometry above the ground");}
  }
  check(SharedSummonFlames.SEPARATION/2+.3<SharedSummonFlames.RADIUS*.79,"both bodies fit the single crown");
  for(int i=0;i<5;i++){var r=VelvetArchiveDesign.card(i);double a=Math.toRadians(VelvetArchiveDesign.angle(i));double x=r.x()+r.w()/2*Math.cos(a)-r.h()/2*Math.sin(a),y=r.y()+r.w()/2*Math.sin(a)+r.h()/2*Math.cos(a);check(VelvetArchiveDesign.pickCard(x,y)==i,"rotated card picking matches drawing");}
  var effectFrames=new LinkedHashMap<String,Object>();for(int tick=36;tick<=108;tick+=2)effectFrames.put(""+tick,SharedSummonFlames.geometry(tick,new Vector3f(1,0,0),new Vector3f(0,0,-1)));
  Files.createDirectories(Path.of("build/reports/velvet-rework"));Files.writeString(Path.of("build/reports/velvet-rework/flames.json"),new com.google.gson.Gson().toJson(effectFrames));
  render();System.out.println("PASS velvet rework: "+checks+" checks; capacity, seats/exit, search/consent, atomic fusion and inherited skills, continuous single-circle flames, native card geometry.");
 }
 private static void checkLevelVariants(){
  var owners=new LinkedHashMap<UUID,PlayerPersonaData>();var stock=new ArrayList<PartyFusion.Source>();
  for(int level:new int[]{0,45,80}){var id=UUID.randomUUID();var data=new PlayerPersonaData();data.setPlayerLevel(99);for(int i=0;i<8;i++)data.expandPersonaCapacity();
   for(String persona:List.of("pixie","apsaras","angel","silky","nekomata","jack_frost")){data.givePersona(persona);if(level>0)data.setPersonaLevel(persona,level);stock.add(new PartyFusion.Source(id,persona));}owners.put(id,data);
  }
  var receiver=owners.keySet().iterator().next();var options=FusionSearch.available(owners,receiver,stock);var actual=new HashSet<String>();for(var option:options)actual.add(recipeKey(option.id(),option.sources()));
  var expected=new HashSet<String>();for(int a=0;a<stock.size();a++)for(int b=a+1;b<stock.size();b++){
   var input=List.of(stock.get(a),stock.get(b));var p=PartyFusion.preview(owners,receiver,input,"",List.of());if(p!=null&&p.allowed())expected.add(recipeKey(p.result().definitionId(),input));
  }
  check(expected.equals(actual),"search preserves every distinct result of leveled copies across owners");
  for(var option:options)check(PartyFusion.preview(owners,receiver,option.sources(),option.special(),List.of()).allowed(),"matched contribution retains a Persona for every donor");
  owners.get(receiver).setPlayerLevel(1);check(FusionSearch.available(owners,receiver,stock).stream().allMatch(o->o.level()<=1),"search excludes outcomes above recipient level before owner assignment");
 }
 private static String recipeKey(String result,List<PartyFusion.Source> sources){return result+sources.stream().map(PartyFusion.Source::persona).sorted().toList();}
 private static void render()throws Exception{
  Path out=Path.of("build/reports/velvet-rework");Files.createDirectories(out);var body=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/body.ttf").toFile());var display=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());
  for(int page=0;page<3;page++){
   var image=new BufferedImage(1440,800,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();g.setColor(new Color(0x030421));g.fillRect(0,0,1440,800);Path bg=out.resolve(page==0?"home-background.png":"menu-0.png");if(Files.exists(bg))g.drawImage(ImageIO.read(bg.toFile()),0,0,1440,800,null);g.scale(1.6,1.6);var c=new Painter(g,body,display);
   if(page==0)VelvetArchiveDesign.home(c,3);else{
    VelvetArchiveDesign.listBackground(c,page==1?"COMPÊNDIO":"BUSCA POR RESULTADO",page==1?"Personas registradas • seu progresso é preservado.":"Resultados possíveis com suas Personas e contribuições.",page==1?"REGISTRADAS 7        YEN 18.600":"12 COMBINAÇÕES DISPONÍVEIS");
    var rows=new ArrayList<VelvetArchiveDesign.Row>();String[] names={"Orpheus","Apsaras","Pixie","Nekomata","Angel","Silky","Jack Frost"};for(int i=0;i<7;i++)rows.add(new VelvetArchiveDesign.Row("Lv "+(i+1)+"  "+names[i],i==6?"¥ 2.860":"POSSUÍDA"));VelvetArchiveDesign.rows(c,rows,6,0,7,0);
    VelvetArchiveDesign.footer(c,"ESC Voltar     ENTER Selecionar     R Atualizar",page==1?"X Inspect     SHIFT Levantar":"A / D Outras receitas");
   }
   g.dispose();ImageIO.write(image,"PNG",out.resolve("ui-production-"+page+".png").toFile());
  }
 }
 private static final class Painter implements MenuDesign.Canvas{
  private final Graphics2D g;private final Font body,display;
  Painter(Graphics2D g,Font body,Font display){this.g=g;this.body=body;this.display=display;g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);}
  public void fill(double x,double y,double w,double h,int color){g.setColor(new Color(color,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] p,int color){var path=new Path2D.Double();path.moveTo(p[0],p[1]);for(int i=2;i<p.length;i+=2)path.lineTo(p[i],p[i+1]);path.closePath();g.setColor(new Color(color,true));g.fill(path);}
  public void text(String s,double x,double y,double size,int color,double angle){var old=g.getTransform();g.rotate(Math.toRadians(angle),x,y);g.setFont((size>=20?display:body).deriveFont((float)size));g.setColor(new Color(color,true));g.drawString(s,(float)x,(float)(y+size*.85));g.setTransform(old);}
  public double width(String text,double size){g.setFont((size>=20?display:body).deriveFont((float)size));return g.getFontMetrics().stringWidth(text);}
  public void item(int slot,double x,double y){}
 }
}
