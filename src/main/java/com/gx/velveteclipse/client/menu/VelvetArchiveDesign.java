package com.gx.velveteclipse.client.menu;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** Coordinates are shared by the live screen, pointer picking and offline audit. */
public final class VelvetArchiveDesign {
 public static final String[] HOME={"Fusão normal","Fusão especial","Busca por resultado","Compêndio","Contribuição","Fusões supremas"};
 public static final String[] SUB={"Normal Fusion","Special Fusion","Fusion Search","Persona Compendium","Party Fusion","Supreme Fusions"};
 public record Row(String title,String detail){}
 public static Rect card(int index){double a=Math.toRadians(angle(index)),w=301,h=46,cx=205+(index%2)*32,cy=116+index*62;return new Rect(cx-w/2*Math.cos(a)+h/2*Math.sin(a),cy-w/2*Math.sin(a)-h/2*Math.cos(a),w,h);}
 public static double angle(int index){return index%2==0?-3:3;}
 public static int pickCard(double x,double y){for(int i=0;i<HOME.length;i++){var r=card(i);double a=Math.toRadians(angle(i)),dx=x-r.x(),dy=y-r.y();if(new Rect(0,0,r.w(),r.h()).contains(dx*Math.cos(a)+dy*Math.sin(a),-dx*Math.sin(a)+dy*Math.cos(a)))return i;}return -1;}
 public static Rect row(int visible){return new Rect(29,147+visible*37,439,32);}
 public static int pickRow(double x,double y){for(int i=0;i<7;i++)if(row(i).contains(x,y))return i;return -1;}
 private static double[] transform(double[] points,Rect r,double degrees){double a=Math.toRadians(degrees),s=Math.sin(a),c=Math.cos(a);double[] out=points.clone();for(int i=0;i<out.length;i+=2){out[i]=r.x()+points[i]*c-points[i+1]*s;out[i+1]=r.y()+points[i]*s+points[i+1]*c;}return out;}
 private static void cardText(Canvas c,String text,Rect r,double x,double y,int size,int color,double angle){double a=Math.toRadians(angle);c.text(text,r.x()+x*Math.cos(a)-y*Math.sin(a),r.y()+x*Math.sin(a)+y*Math.cos(a),size,color,angle);}
 public static void home(Canvas c,int selected){
  c.poly(new double[]{0,0,290,0,374,500,0,500},0x5E050B33);
  title(c,"ESCOLHA UMA FUSÃO","");
  for(int i=0;i<HOME.length;i++){
   var r=card(i);double a=angle(i),w=r.w(),h=r.h();int ink=i==selected?INK:WHITE;
   c.poly(transform(new double[]{-2,-2,w+2,-2,w+2,h+2,-2,h+2},r,a),i==selected?0xFF38FAF3:0xFFD3E6FF);
   c.poly(transform(new double[]{0,0,w,0,w,h,0,h},r,a),i==selected?0xFF08D8DC:0xF2122469);
   for(int x:new int[]{12,289})for(int y:new int[]{10,39})c.poly(transform(new double[]{x,y-3,x+3,y,x,y+3,x-3,y},r,a),ink);
   cardText(c,HOME[i],r,30,8,18,ink,a);cardText(c,SUB[i],r,32,31,9,ink,a);
  }
  c.poly(new double[]{426,385,865,372,853,450,422,462},0xEF040726);c.text("IGOR",444,393,13,CYAN,0);
  c.text("Duas almas abrem caminho",444,420,16,WHITE,0);c.text("a uma nova possibilidade.",444,438,16,WHITE,0);
  footer(c,"Setas  Escolher     ENTER  Confirmar","SHIFT  Levantar    ESC  Voltar à sala");
 }
 public static void title(Canvas c,String title,String subtitle){
  c.poly(new double[]{0,15,526,0,498,66,0,84},0xF0030517);c.poly(new double[]{22,70,475,51,475,52,22,71},0xFF7CC9FF);
  c.text(title,26,29,26,WHITE,-3);if(!subtitle.isBlank())c.text(fit(c,subtitle,435,11),33,81,11,MUTED,-2);
 }
 public static void listBackground(Canvas c,String title,String subtitle,String info){
  c.poly(new double[]{8,105,472,94,481,460,14,470},0xCD070C32);
  c.poly(new double[]{482,91,885,50,900,442,490,474},0xBA0D1B58);
  c.poly(new double[]{477,91,483,90,500,473,492,473},0xFF5340E0);
  title(c,title,subtitle);c.poly(new double[]{24,105,466,94,466,132,24,143},0xE5081032);c.text(info,37,114,13,CYAN,-1);
 }
 public static void rows(Canvas c,List<Row> rows,int selected,int offset,int total,int base){
  for(int i=0;i<7&&i+offset<rows.size();i++){
   int index=i+offset;var r=row(i);boolean active=index==selected;int ink=active?INK:WHITE;
   c.poly(new double[]{r.x(),r.y(),r.x()+r.w(),r.y()-7,r.x()+r.w()+2,r.y()+r.h()-7,r.x(),r.y()+r.h()},active?0xFF1AE0DC:0xEB25296E);
   c.text(fit(c,rows.get(index).title(),414,16),r.x()+11,r.y()+1,16,ink,0);
   c.text(fit(c,rows.get(index).detail(),414,9),r.x()+12,r.y()+20,9,active?INK:MUTED,0);
  }
  if(rows.isEmpty())c.text("Nenhum registro disponível.",42,162,15,WHITE,0);
  c.text((rows.isEmpty()?0:base+selected+1)+" / "+total+"    Setas Selecionar",34,419,10,MUTED,0);
 }
 public static void footer(Canvas c,String left,String right){c.fill(0,474,900,26,0xF203061E);c.text(left,20,482,11,WHITE,0);c.text(right,520,482,11,CYAN,0);}
 public static String supremeStatus(com.gx.velveteclipse.core.fusion.SupremeFusion.Entry e){
  if(!e.gated())return "NORMAL MODE / NO RANK LOCK";
  return (e.unlocked()?"UNLOCKED / ":"LOCKED / ")+(e.allRanks()?e.maxed()+" / "+e.total()+" maxed Arcanas":"Rank "+e.rank()+" / 10");
 }
 public static void supremeDetail(Canvas c,com.gx.velveteclipse.core.fusion.SupremeFusion.Entry e,List<com.gx.velveteclipse.core.fusion.FusionSearch.Option> options,int combination){
  c.fill(516,300,363,140,0xEF050C2D);c.text(fit(c,supremeStatus(e),337,12),529,307,12,e.unlocked()?CYAN:0xffffacbd,0);
  c.text(e.arcana()+" / Required Lv "+e.level(),529,329,11,MUTED,0);
  int index=options.isEmpty()?0:Math.floorMod(combination,options.size());
  var ingredients=options.isEmpty()?e.ingredients():options.get(index).sources().stream().map(com.gx.velveteclipse.core.fusion.PartyFusion.Source::persona).toList();
  String recipe=!e.unlocked()?(e.allRanks()?"Reach rank 10 in all 22 fusion Arcanas.":"Use Arcana Up cards to reach rank 10."):ingredients.isEmpty()?"No valid recipe with your current level and authorized ingredients.":String.join(" + ",ingredients.stream().map(id->com.gx.velveteclipse.core.persona.PersonaRegistry.persona(id).orElseThrow().displayName()).toList());
  var lines=wrap(c,recipe,332,11);for(int i=0;i<Math.min(3,lines.size());i++)c.text(lines.get(i),529,351+i*14,11,WHITE,0);
  c.text(!e.unlocked()?"Raise rank, then refresh with R.":options.isEmpty()?"Collect the ingredients and refresh with R.":"Combination "+(index+1)+" / "+options.size()+"   A / D",529,408,10,CYAN,0);
  c.text(e.unlocked()?"ENTER  Inspect / inheritance":"Rank 10 required",529,425,10,MUTED,0);
 }
 public static void dialog(Canvas c,String title,String text,String note){c.fill(0,0,900,474,0xA2020517);c.poly(new double[]{154,158,764,146,747,348,144,363},0xFF081A55);c.text(title,181,182,24,CYAN,0);var lines=wrap(c,text,538,16);for(int i=0;i<lines.size();i++)c.text(lines.get(i),182,229+i*22,16,WHITE,0);c.text(fit(c,note,538,12),182,301,12,MUTED,0);c.text("ENTER  Confirmar      ESC  Cancelar",182,334,14,CYAN,0);}
 private VelvetArchiveDesign(){}
}
