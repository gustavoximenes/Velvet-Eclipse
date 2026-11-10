package com.gx.velveteclipse.client;
import com.gx.velveteclipse.client.menu.PersonaDetails;
import com.gx.velveteclipse.core.persona.*;
import java.util.List;
/** Responsive Reload-inspired skill list. All hit boxes use this exact coordinate system. */
public final class PersonaBattleDesign {
 private static final int WHITE=0xFFF7FFFF,CYAN=0xFF51F5FF,BLUE=0xFF161BFF,INK=0xFF02061B;
 public record View(String persona,List<SkillDefinition> skills,int selected,int hp,int maxHp,int sp,boolean pending,double age,java.util.Map<String,com.gx.velveteclipse.roguelike.SkillPayment> payments){
  public View(String persona,List<SkillDefinition> skills,int selected,int hp,int maxHp,int sp,boolean pending,double age){this(persona,skills,selected,hp,maxHp,sp,pending,age,java.util.Map.of());}
 }
 public static double unit(int w,int h){return Math.min(w/820.0,h/390.0);}
 public static double top(int w,int h){return h-360*unit(w,h);}
 public static double entrance(double age){double p=Math.max(0,Math.min(1,age/.28));return 1-Math.pow(1-p,3);}
 private static double slide(double age){return -580*(1-entrance(age));}
 private static int rowOffset(int i,double age){return (int)Math.round(-24*(1-EvokerPose.smooth((age-.05-i*.012)/.18)));}
 public static int edge(double value){return (int)Math.round(value);}
 private static BattleMenuLayout.Rect rect(int w,int h,double x,double y,double width,double height,int i){
  double u=unit(w,h),top=top(w,h);int left=edge(x*u),up=edge(top+y*u);
  return new BattleMenuLayout.Rect(left,up,edge((x+width)*u)-left,edge(top+(y+height)*u)-up,"",i);
 }
 public static BattleMenuLayout.Rect row(int w,int h,int i){return row(w,h,i,1);}
 public static BattleMenuLayout.Rect row(int w,int h,int i,double age){return rect(w,h,187+slide(age)+rowOffset(i,age),76+i*24,330,22,i);}
 public static BattleMenuLayout.Rect previous(int w,int h){return previous(w,h,1);}
 public static BattleMenuLayout.Rect previous(int w,int h,double age){return rect(w,h,8+slide(age),13,92,40,0);}
 public static BattleMenuLayout.Rect next(int w,int h){return next(w,h,1);}
 public static BattleMenuLayout.Rect next(int w,int h,double age){return rect(w,h,430+slide(age),13,94,40,1);}
 public static BattleMenuLayout.Rect footer(int w,int h){return rect(w,h,0,338,530,22,0);}
 public static void paint(HudDesign.Canvas target,View v,int width,int height){
  double scale=unit(width,height),left=slide(v.age)*scale,top=top(width,height);
  HudDesign.Canvas c=new HudDesign.Canvas(){
   // Shared edges prevent alpha strips overlapping at fractional GUI scales.
   public void fill(int x,int y,int w,int h,int color){int xx=edge(left+x*scale),yy=edge(top+y*scale);target.fill(xx,yy,edge(left+(x+w)*scale)-xx,edge(top+(y+h)*scale)-yy,color);}
   public void text(String s,int x,int y,int color){target.styled(s,edge(left+x*scale),edge(top+y*scale),(float)scale,color);}
   public int width(String s){return target.width(s);}
   public void styled(String s,int x,int y,float size,int color){target.styled(s,edge(left+x*scale),edge(top+y*scale),(float)(size*scale),color);}
   public void angled(String s,int x,int y,float size,int color,float degrees){target.angled(s,edge(left+x*scale),edge(top+y*scale),(float)(size*scale),color,degrees);}
   public void personaPortrait(int x,int y,int w,int h,float a){target.personaPortrait(edge(left+x*scale),edge(top+y*scale),edge(w*scale),edge(h*scale),a);}
  };
  paintVirtual(c,v);
 }
 private static void paintVirtual(HudDesign.Canvas c,View v){
  BattleGraphics.poly(c,new double[]{0,8,480,8,570,145,495,282,185,360,0,360},BLUE);
  BattleGraphics.poly(c,new double[]{38,8,156,8,327,298,241,360},0xFF0739C3);
  for(int i=0;i<3;i++)BattleGraphics.poly(c,new double[]{58+i*92,8,67+i*92,8,264+i*43,340,257+i*43,351},0x2833B8FF);
  c.angled("PERSONA",94,236,8.4f,0xFF071453,-16);
  float portrait=(float)EvokerPose.smooth((v.age-.035)/.27);
  c.personaPortrait(-8-(int)(20*(1-portrait)),59,190,279,portrait);
  BattleGraphics.poly(c,new double[]{8,13,524,13,508,53,8,53},WHITE);
  BattleGraphics.poly(c,new double[]{8,13,108,13,84,53,8,53},CYAN);
  c.styled("< Q",16,19,2.15f,INK);c.styled("E >",448,19,2.15f,INK);
  c.styled(HudDesign.fit(c,v.persona,230),117,26,1.3f,INK);
  int first=v.selected/7*7;
  for(int i=first;i<Math.min(first+7,v.skills.size());i++){
   var skill=v.skills.get(i);boolean chosen=i==v.selected;
   var payment=v.payments.get(skill.id());boolean affordable=payment!=null?payment.affordable(v.hp,v.sp):skill.spCost()<=v.sp&&(skill.hpPercentCost()==0||v.hp>Math.max(1,v.maxHp*skill.hpPercentCost()/100));
   int rx=187+rowOffset(i-first,v.age),ry=76+(i-first)*24,rw=330,rh=22;
   if(chosen){BattleHudDesign.ribbon(c,rx-3,ry-2,rw+6,rh+4,0xFF33DAFF);BattleHudDesign.ribbon(c,rx,ry,rw,rh,WHITE);}
   else BattleHudDesign.ribbon(c,rx+4,ry,rw-4,rh,0xE8021747);
   BattleGraphics.poly(c,new double[]{rx+8,ry+3,rx+51,ry+3,rx+45,ry+rh-3,rx+2,ry+rh-3},INK);
   BattleGraphics.icon(c,skill.element(),rx+27,ry+rh*.5,8);
   c.text(HudDesign.fit(c,skill.displayName(),190),rx+57,ry+7,chosen?INK:affordable?CYAN:0xFF7F98BD);
   c.fill(rx+rw-69,ry+3,64,rh-6,INK);
   c.text(payment==null?PersonaDetails.cost(skill):payment.label(),rx+rw-64,ry+7,affordable?0xFFFFEF49:0xFFFF5975);
  }
  if(v.skills.isEmpty())c.text("Nenhuma habilidade aprendida",196,90,WHITE);
  else{
   var skill=v.skills.get(Math.min(v.selected,v.skills.size()-1));int infoY=Math.max(177,76+Math.min(7,v.skills.size()-first)*24+12);
   BattleHudDesign.ribbon(c,192,infoY-5,330,49,0xE9010A37);
   c.styled("Info",198,infoY,.9f,CYAN);
   wrap(c,PersonaDetails.description(skill),198,infoY+12,309,CYAN,2);
   c.text(skill.isDamageSkill()?"Poder "+skill.power()+"   Acerto "+skill.accuracy()+"%":"Efeito de suporte",198,infoY+34,WHITE);
  }
  c.styled("Qual habilidade usar?",198,317,1.1f,WHITE);
  c.fill(0,338,530,22,0xF303092A);
  c.text(v.pending?"Trocando Persona...":"Q / E Persona   SETAS Habilidade   ENTER Selecionar   ESC Voltar",7,345,WHITE);
 }
 private static void wrap(HudDesign.Canvas c,String text,int x,int y,int width,int color,int lines){text=com.gx.velveteclipse.localization.DisplayText.text(text);
  String line="";int row=0;for(String word:text.split(" ")){String next=line.isEmpty()?word:line+" "+word;
   if(c.width(next)>width&&!line.isEmpty()){c.text(line,x,y+row++*10,color);line=word;if(row>=lines)return;}else line=next;
  }if(!line.isEmpty()&&row<lines)c.text(line,x,y+row*10,color);
 }
}
