package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.roguelike.*;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** Shared runtime/audit drawing and hit boxes. Tarot motifs are original vector artwork. */
public final class RogueDesign {
 public static final int GOLD=0xffe0c68b,CREAM=0xfff0eadb,NAVY=0xff071126;
 public record View(RogueNetwork.State state,int page,int selected,int keyboard,boolean waiting){}
 public record Button(Rect rect,String action,String value,String label,boolean enabled){}
 public static List<Button> buttons(View v){
  var s=v.state();var b=new ArrayList<Button>();boolean prompt=!s.card().isEmpty();
  if(prompt){var up=ArcanaUpCatalog.offered(s.card());b.add(new Button(new Rect(244,361,180,34),"cancel","","Cancel",!v.waiting()));b.add(new Button(new Rect(441,361,213,34),"confirm",s.token(),up==null?"Confirm activation":"Confirm rank up",!v.waiting()&&s.enabled()&&s.editable()&&(up==null||s.rank(up.id())<10)));return b;}
  String[] tabs={"Collection","Class focus","World / System","Arcana ranks"};for(int i=0;i<4;i++)b.add(new Button(new Rect(28+i*210,69,200,29),"page",""+i,tabs[i],true));
  if(v.page()==0)for(int i=0;i<RogueCatalog.CARDS.size();i++)b.add(new Button(tile(i),"select",""+i,RogueCatalog.CARDS.get(i).name(),true));
  if(v.page()==1){int i=0;for(var f:RogueCatalog.Focus.values()){if(f==RogueCatalog.Focus.NONE)continue;int col=i/2,row=i%2;if(f==RogueCatalog.Focus.WEAKNESS){col=2;row=0;}b.add(new Button(new Rect(42+col*284,236+row*78,248,59),"focus",f.id(),f.label,s.enabled()&&s.editable()&&!v.waiting()));i++;}}
  if(v.page()==2)b.add(new Button(new Rect(54,286,294,42),"toggle",""+!s.enabled(),s.enabled()?"Disable Roguelike":"Enable Roguelike",s.owner()&&s.editable()&&!v.waiting()));
  if(v.page()==3)for(int i=0;i<ArcanaUpCatalog.CARDS.size();i++)b.add(new Button(rankTile(i),"rank_select",""+i,ArcanaUpCatalog.CARDS.get(i).name(),true));
  b.add(new Button(new Rect(28,463,130,25),"back","","ESC  Back",true));return b;
 }
 public static Rect tile(int i){return new Rect(28+i%3*176,132+i/3*99,166,91);}
 public static Rect rankTile(int i){return new Rect(28+i%4*134,132+i/4*48,126,42);}
 public static int hit(View v,double x,double y){var b=buttons(v);for(int i=0;i<b.size();i++)if(b.get(i).rect().contains(x,y))return i;return -1;}
 private static void paragraph(Canvas c,String text,double x,double y,int w,int size,int color){for(var line:wrap(c,text,w,size)){c.body(line,x,y,size,color,0);y+=size+4;}}
 private static String roman(int n){if(n==0)return "0";StringBuilder out=new StringBuilder();int[] values={10,9,5,4,1};String[] names={"X","IX","V","IV","I"};for(int i=0;i<values.length;i++)while(n>=values[i]){out.append(names[i]);n-=values[i];}return out.toString();}
 public static void upTarot(Canvas c,ArcanaUpCatalog.Card card,double x,double y,double w,double h){
  tarot(c,new RogueCatalog.Card(card.id(),card.number(),card.name(),"Arcana Up","","",0,0,0,0,0,0,false),x,y,w,h,true);
  c.fill(x+w*.16,y+h*.67,w*.68,h*.14,NAVY);
  c.body("UP",x+w*.28,y+h*.675,w*.24,CYAN,0);
  c.poly(new double[]{x+w*.70,y+h*.73,x+w*.78,y+h*.67,x+w*.86,y+h*.73},CYAN);
 }
 public static void tarot(Canvas c,RogueCatalog.Card card,double x,double y,double w,double h,boolean known){
  c.fill(x,y,w,h,known?CREAM:0xff14233d);c.fill(x+2,y+2,w-4,h-4,GOLD);c.fill(x+3,y+3,w-6,h-6,NAVY);
  c.fill(x+w*.12,y+h*.20,w*.76,h*.60,known?0xffdcd9c8:0xff203a52);
  c.text(roman(card.number()),x+w*.34,y+h*.055,w*.18,GOLD,0);
  double cx=x+w/2,cy=y+h*.49,s=w*.30;int ink=known?NAVY:0xff6b8793;
  // Distinct silhouettes, not textures copied from the original game.
  switch(card.id()){
   case "fool"->{disc(c,cx,cy-s*.65,s*.26,ink);c.poly(new double[]{cx,cy-s*.3,cx-s*.5,cy+s*.65,cx+s*.5,cy+s*.65},ink);stroke(c,cx-s*.8,cy+s,cx+s*.7,cy-s,s*.12,ink);}
   case "empress"->{for(int i=0;i<6;i++){double a=i*Math.PI/3;disc(c,cx+Math.cos(a)*s*.55,cy+Math.sin(a)*s*.55,s*.31,ink);}disc(c,cx,cy,s*.25,CREAM);}
   case "emperor"->{c.poly(new double[]{cx-s,cy-s*.7,cx-s*.5,cy,cx,cy-s,cx+s*.5,cy,cx+s,cy-s*.7,cx+s*.8,cy+s*.55,cx-s*.8,cy+s*.55},ink);c.fill(cx-s*.8,cy+s*.75,s*1.6,s*.16,ink);}
   case "hierophant"->{c.fill(cx-s*.14,cy-s,s*.28,s*2,ink);c.fill(cx-s*.85,cy-s*.7,s*1.7,s*.2,ink);c.fill(cx-s*.65,cy-s*.2,s*1.3,s*.2,ink);c.fill(cx-s*.4,cy+s*.4,s*.8,s*.2,ink);}
   case "lovers"->{disc(c,cx-s*.46,cy-s*.43,s*.53,ink);disc(c,cx+s*.46,cy-s*.43,s*.53,ink);c.poly(new double[]{cx-s,cy-s*.3,cx+s,cy-s*.3,cx,cy+s},ink);}
   case "justice"->{stroke(c,cx,cy-s,cx,cy+s,s*.13,ink);stroke(c,cx-s,cy-s*.5,cx+s,cy-s*.5,s*.13,ink);for(int side:new int[]{-1,1}){double px=cx+side*s*.68;c.poly(new double[]{px,cy-s*.55,px-s*.34,cy+s*.2,px+s*.34,cy+s*.2},ink);}c.fill(cx-s*.5,cy+s*.85,s,s*.15,ink);}
   case "fortune"->{disc(c,cx,cy,s,ink);disc(c,cx,cy,s*.72,CREAM);for(int i=0;i<8;i++){double a=i*Math.PI/4;stroke(c,cx,cy,cx+Math.cos(a)*s,cy+Math.sin(a)*s,s*.13,ink);}disc(c,cx,cy,s*.25,ink);}
   case "death"->{stroke(c,cx-s*.2,cy+s,cx+s*.3,cy-s,s*.16,ink);c.poly(new double[]{cx-s*.9,cy-s*.6,cx+s*.3,cy-s,cx+s,cy-s*.1,cx+s*.15,cy-s*.6},ink);}
   case "tower"->{c.fill(cx-s*.5,cy-s*.55,s,s*1.45,ink);c.poly(new double[]{cx+s*.7,cy-s,cx-s*.1,cy-s*.1,cx+s*.35,cy-s*.1,cx-s*.6,cy+s*.8,cx,cy-s*.3,cx-s*.4,cy-s*.3},GOLD);c.fill(cx-s*.65,cy-s*.75,s*1.3,s*.2,ink);}
   case "moon"->{disc(c,cx,cy,s,ink);disc(c,cx+s*.5,cy-s*.25,s*.82,CREAM);star(c,cx+s*.65,cy+s*.68,s*.21,ink);}
   case "sun"->{star(c,cx,cy,s,ink);disc(c,cx,cy,s*.6,CREAM);disc(c,cx,cy,s*.42,ink);}
   case "judgement"->{c.poly(new double[]{cx-s*.75,cy-s*.2,cx+s*.3,cy-s*.2,cx+s,cy-s*.85,cx+s,cy+s*.85,cx+s*.3,cy+s*.2,cx-s*.75,cy+s*.2},ink);stroke(c,cx-s*.3,cy+s*.2,cx-s*.6,cy+s*.7,s*.15,ink);}
   case "world"->{disc(c,cx,cy,s,ink);disc(c,cx,cy,s*.72,CREAM);disc(c,cx,cy-s*.3,s*.2,ink);c.poly(new double[]{cx,cy-s*.1,cx-s*.35,cy+s*.45,cx+s*.35,cy+s*.45},ink);}
   case "aeon"->{c.poly(new double[]{cx-s,cy,cx-s*.4,cy-s*.6,cx+s*.4,cy-s*.6,cx+s,cy,cx+s*.4,cy+s*.6,cx-s*.4,cy+s*.6},ink);disc(c,cx,cy,s*.42,CREAM);disc(c,cx,cy,s*.23,ink);}
   case "magician"->{c.fill(cx-s*.10,cy-s,s*.20,s*2.1,ink);star(c,cx,cy-s,s*.58,ink);c.fill(cx-s*.65,cy+s*.7,s*1.3,s*.16,ink);}
   case "priestess"->{c.poly(new double[]{cx,cy-s,cx-s*.8,cy+s,cx+s*.8,cy+s},ink);c.fill(cx-s*.14,cy-s*.55,s*.28,s*.85,CREAM);}
   case "chariot"->{c.poly(new double[]{cx-s,cy-s*.6,cx+s,cy-s*.6,cx+s*.6,cy+s*.5,cx-s*.6,cy+s*.5},ink);star(c,cx-s*.6,cy+s*.8,s*.26,ink);star(c,cx+s*.6,cy+s*.8,s*.26,ink);}
   case "hermit"->{c.poly(new double[]{cx,cy-s,cx-s*.8,cy+s,cx+s*.7,cy+s},ink);c.fill(cx+s*.5,cy-s*.8,s*.16,s*1.9,ink);star(c,cx+s*.6,cy-s*.8,s*.4,ink);}
   case "strength"->{c.poly(new double[]{cx,cy-s,cx+s*.20,cy+s*.5,cx-s*.20,cy+s*.5},ink);c.fill(cx-s*.7,cy+s*.25,s*1.4,s*.16,ink);c.fill(cx-s*.12,cy+s*.5,s*.24,s*.5,ink);}
   case "hanged_man"->{c.fill(cx-s*.85,cy-s,s*1.7,s*.13,ink);c.fill(cx,cy-s,s*.10,s*.65,ink);c.poly(new double[]{cx,cy-s*.45,cx+s*.65,cy,cx,cy+s*.6,cx-s*.55,cy},ink);star(c,cx,cy+s*.85,s*.25,ink);}
   case "temperance"->{c.poly(new double[]{cx-s,cy-s*.7,cx-s*.15,cy-s*.7,cx-s*.35,cy,cx-s*.8,cy},ink);c.poly(new double[]{cx+s*.15,cy+s*.1,cx+s,cy+s*.1,cx+s*.8,cy+s*.8,cx+s*.35,cy+s*.8},ink);c.poly(new double[]{cx-s*.35,cy-s*.4,cx+s*.6,cy+s*.45,cx+s*.45,cy+s*.55,cx-s*.5,cy-s*.35},ink);}
   case "devil"->{c.poly(new double[]{cx-s,cy-s,cx-s*.65,cy+s*.6,cx,cy+s,cx+s*.65,cy+s*.6,cx+s,cy-s,cx,cy-s*.3},ink);c.fill(cx-s*.45,cy,s*.3,s*.13,CREAM);c.fill(cx+s*.15,cy,s*.3,s*.13,CREAM);}
   default->star(c,cx,cy,s,ink);
  }
  c.fill(x+w*.20,y+h*.85,w*.60,1,GOLD);c.fill(x+w*.34,y+h*.90,w*.32,1,GOLD);
 }
 private static void star(Canvas c,double x,double y,double r,int color){double[] p=new double[32];for(int i=0;i<16;i++){double a=i*Math.PI/8-Math.PI/2,d=i%2==0?r:r*.35;p[i*2]=x+Math.cos(a)*d;p[i*2+1]=y+Math.sin(a)*d;}c.poly(p,color);}
 private static void disc(Canvas c,double x,double y,double r,int color){double[] p=new double[48];for(int i=0;i<24;i++){p[i*2]=x+Math.cos(i*Math.PI/12)*r;p[i*2+1]=y+Math.sin(i*Math.PI/12)*r;}c.poly(p,color);}
 private static void stroke(Canvas c,double x,double y,double tx,double ty,double width,int color){double length=Math.hypot(tx-x,ty-y),dx=(ty-y)/length*width/2,dy=(x-tx)/length*width/2;c.poly(new double[]{x+dx,y+dy,tx+dx,ty+dy,tx-dx,ty-dy,x-dx,y-dy},color);}
 public static void paint(Canvas c,View v){
  var s=v.state();c.fill(0,0,900,500,NAVY);c.poly(new double[]{480,0,900,0,900,500,650,500},0xff101e43);
  for(int i=0;i<8;i++)c.poly(new double[]{680+i*40,0,682+i*40,0,430+i*40,500,428+i*40,500},0x122cd8ec);
  c.fill(28,22,3,31,GOLD);c.text("ARCANA BUFFS",42,18,35,CREAM,0);c.body(s.enabled()?"ROGUELIKE / ON":"ROGUELIKE / OFF",674,29,12,s.enabled()?CYAN:MUTED,0);
  c.body(v.page()==3?"Arcana Up: +1 rank per card, up to rank 10. Ranks survive death.":"Activated buffs survive death. Duplicate buffs do not stack.",29,108,11,MUTED,0);
  if(v.page()==0){
   for(int i=0;i<RogueCatalog.CARDS.size();i++){
    var card=RogueCatalog.CARDS.get(i);var r=tile(i);boolean active=s.active().contains(card.id()),known=s.collected().contains(card.id()),sel=i==v.selected();
    c.fill(r.x(),r.y(),r.w(),r.h(),sel?0xff2a3955:0xff102039);if(sel)c.fill(r.x(),r.y(),2,r.h(),GOLD);
    tarot(c,card,r.x()+9,r.y()+7,47,77,known);c.body(card.arcana(),r.x()+65,r.y()+12,11,CREAM,0);
    paragraph(c,card.name(),r.x()+65,r.y()+29,94,10,MUTED);
    c.body(active?(s.enabled()?"ACTIVE":"SUSPENDED"):known?"COLLECTED":"UNDISCOVERED",r.x()+65,r.y()+69,8,active?CYAN:GOLD,0);
   }
   var card=RogueCatalog.CARDS.get(Math.floorMod(v.selected(),RogueCatalog.CARDS.size()));
   c.fill(572,132,300,289,0xff0a162c);tarot(c,card,590,148,68,112,true);c.text(card.arcana(),672,148,21,CREAM,0);paragraph(c,card.name(),673,176,178,14,GOLD);
   c.body(card.pact()?"PACT / TRADE-OFF":"BLESSING",674,230,10,card.pact()?0xffff8595:CYAN,0);
   paragraph(c,card.benefit(),590,274,264,12,CREAM);if(card.pact())paragraph(c,card.drawback(),590,315,264,11,0xffffa3ac);
   paragraph(c,"Activate from the inventory item. This collection does not consume cards.",590,378,264,10,MUTED);
  }else if(v.page()==1){
   String[] names={"DPS","SUPPORT","WEAKNESS HITTER"},descriptions={"Choose physical or magical skill damage.","Choose healing or longer debuffs.","Exploit weaknesses for knockdowns."};
   for(int i=0;i<3;i++){c.fill(28+i*284,132,272,288,0xff102039);c.fill(28+i*284,132,272,2,GOLD);c.text(names[i],43+i*284,150,i==2?23:29,CREAM,0);paragraph(c,descriptions[i],43+i*284,195,242,11,MUTED);}
   paragraph(c,"Attacks cost half HP or SP and deal half damage. Weakness knockdowns remain intact.",611,312,242,12,MUTED);
   c.body("Current focus",43,389,11,GOLD,0);c.body(RogueCatalog.Focus.parse(s.focus()).label,160,389,12,CYAN,0);
  }else if(v.page()==3){
   for(int i=0;i<ArcanaUpCatalog.CARDS.size();i++){
    var up=ArcanaUpCatalog.CARDS.get(i);var r=rankTile(i);int rank=s.rank(up.id());boolean selected=i==v.selected();
    c.fill(r.x(),r.y(),r.w(),r.h(),selected?0xff2a3955:0xff102039);if(selected)c.fill(r.x(),r.y(),2,r.h(),GOLD);
    c.body(up.name(),r.x()+8,r.y()+5,10,CREAM,0);c.body(rank==10?"MAX":"Rank "+rank+" / 10",r.x()+8,r.y()+19,8,rank==10?CYAN:GOLD,0);
    for(int n=0;n<10;n++)c.fill(r.x()+8+n*11,r.y()+33,8,3,n<rank?CYAN:0xff33445b);
   }
   var up=ArcanaUpCatalog.CARDS.get(Math.floorMod(v.selected(),ArcanaUpCatalog.CARDS.size()));int rank=s.rank(up.id());
   c.fill(572,132,300,289,0xff0a162c);upTarot(c,up,590,147,74,122);c.text(up.name(),680,150,21,CREAM,0);
   c.body("Rank "+rank+" / 10",680,192,15,GOLD,0);c.body("+"+(rank*10)+"% EXP",680,222,16,CYAN,0);
   paragraph(c,"Bonus EXP for newly fused Personas of this Arcana, based on their next-level requirement.",590,282,264,12,CREAM);
   paragraph(c,s.enabled()?"Use an Arcana Up item to increase this rank.":"Ranks are preserved. Fusion bonuses resume when Roguelike is enabled.",590,360,264,11,MUTED);
  }else{
   c.fill(28,132,844,289,0xff102039);c.text("WORLD RULE",54,154,30,CREAM,0);
   paragraph(c,"Off by default. Only the world owner can change this mode.",54,203,470,15,CREAM);
   paragraph(c,"Classes and cards are suspended while disabled, and preserved when you enable it again.",54,242,470,12,MUTED);
   paragraph(c,s.owner()?"You are the world owner.":"Owner access is required to change the rule.",54,348,470,12,GOLD);
   paragraph(c,"Tartarus chests: 25% Arcana Buff, 10% Arcana Up, 65% no card. At most one card, activated after confirmation.",580,172,254,14,CREAM);
   paragraph(c,"No run reset yet. Changing focus is allowed outside battles and raids.",580,292,254,12,MUTED);
  }
  var buttons=buttons(v);if(s.card().isEmpty())for(int i=0;i<buttons.size();i++){
   var b=buttons.get(i);if(b.action().equals("select")||b.action().equals("rank_select")){if(i==v.keyboard())outline(c,b.rect(),CYAN);continue;}
   button(c,b,i==v.keyboard()||b.action().equals("page")&&b.value().equals(""+v.page())||b.action().equals("focus")&&b.value().equals(s.focus()));
   if(b.action().equals("focus")){var f=RogueCatalog.Focus.parse(b.value());String shortText=switch(f){case DPS_PHYSICAL,DPS_MAGIC->"+10% damage";case SUPPORT_HEAL->"+15% healing";case SUPPORT_DEBUFF->"+1 debuff turn";default->"-50% cost / -50% damage";};c.body(shortText,b.rect().x()+10,b.rect().y()+35,10,i==v.keyboard()||b.value().equals(s.focus())?NAVY:GOLD,0);}
  }
  c.body(fit(c,s.message().isEmpty()?"Right-click an inventory card to review its effect.":s.message(),840,11),28,438,11,CYAN,0);
  if(!s.card().isEmpty())confirmation(c,v,buttons);
  else c.body("TAB Select   ENTER Confirm   Arrows Browse",549,468,10,MUTED,0);
 }
 private static void confirmation(Canvas c,View v,List<Button> buttons){
  var up=ArcanaUpCatalog.offered(v.state().card());if(up!=null){
   int rank=v.state().rank(up.id());c.fill(0,0,900,500,0xe8030715);c.fill(220,77,460,339,CREAM);c.fill(223,80,454,333,NAVY);
   c.text("RAISE ARCANA RANK?",243,98,28,CREAM,0);upTarot(c,up,244,151,105,174);c.text(up.name(),369,150,24,GOLD,0);
   c.body("Rank "+rank+" > "+Math.min(10,rank+1)+" / 10",369,188,19,CREAM,0);
   paragraph(c,rank==10?"This Arcana is already rank 10. No card was consumed.":"Fusion bonus: +"+(rank*10)+"% > +"+((rank+1)*10)+"% of next-level EXP.",369,231,280,13,CYAN);
   paragraph(c,"Consumes one card. This Arcana rank remains after death.",369,313,280,10,MUTED);
   for(int i=0;i<buttons.size();i++)button(c,buttons.get(i),i==v.keyboard());return;
  }
  var card=RogueCatalog.card(v.state().card());if(card==null)return;
  c.fill(0,0,900,500,0xe8030715);c.fill(220,77,460,339,CREAM);c.fill(223,80,454,333,NAVY);
  c.text("ACTIVATE ARCANA?",243,98,28,CREAM,0);tarot(c,card,244,151,105,174,true);
  c.text(card.arcana(),369,150,24,GOLD,0);paragraph(c,card.benefit(),369,186,280,13,CREAM);
  paragraph(c,card.pact()?card.drawback():"No drawback.",369,236,280,12,card.pact()?0xffffa3ac:CYAN);
  paragraph(c,"Consumes one card. The buff remains after death.",369,313,280,10,MUTED);
  for(int i=0;i<buttons.size();i++)button(c,buttons.get(i),i==v.keyboard());
 }
 private static void outline(Canvas c,Rect r,int col){c.fill(r.x(),r.y(),r.w(),1,col);c.fill(r.x(),r.y()+r.h()-1,r.w(),1,col);c.fill(r.x(),r.y(),1,r.h(),col);c.fill(r.x()+r.w()-1,r.y(),1,r.h(),col);}
 private static void button(Canvas c,Button b,boolean selected){var r=b.rect();c.fill(r.x(),r.y(),r.w(),r.h(),selected?0xffd8c294:0xff1c3150);if(selected)outline(c,r,GOLD);c.body(fit(c,b.label(),(int)r.w()-20,b.action().equals("focus")?13:12),r.x()+10,r.y()+9,b.action().equals("focus")?13:12,!b.enabled()?0xff748295:selected?NAVY:CREAM,0);}
 private RogueDesign(){}
}
