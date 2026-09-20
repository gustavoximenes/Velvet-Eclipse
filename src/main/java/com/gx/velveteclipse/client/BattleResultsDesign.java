package com.gx.velveteclipse.client;
import com.gx.velveteclipse.network.BattleResultsS2C;
import com.gx.velveteclipse.core.battle.AllOutTimeline;
public final class BattleResultsDesign {
 public static void paint(HudDesign.Canvas c,BattleResultsS2C r,int w,int h,double age){
  double u=Math.min(w/900.,h/500.),slide=(1-AllOutTimeline.ease(age/.45))*w;
  c.fill(0,0,w,h,0xA5030925);
  BattleGraphics.poly(c,new double[]{0,0,w*.7,0,w*.47,h,w*.08,h},0xF208288F);
  BattleGraphics.poly(c,new double[]{0,h*.17,w*.69,h*.38,w*.51,h*.98,0,h*.71},0xFF1625F4);
  BattleGraphics.poly(c,new double[]{w*.63,0,w*.98,0,w,h,w*.69,h},0x8B053891);
  c.angled(r.victory()?"RESULT":"DEFEAT",(int)(-18*u+slide),(int)(4*u),(float)(12*u),0xFF0871FF,-9);
  c.angled(r.victory()?"VICTORY":"BATALHA ENCERRADA",(int)(30*u),(int)(113*u),(float)(2.5*u),0xFF75F9FF,-9);
  int xp=(int)(r.exp()*AllOutTimeline.ease((age-.3)/.9));
  panel(c,35*u,178*u,350*u,120*u,0xFF063DB2);
  c.styled("EXP",(int)(57*u),(int)(170*u),(float)(5*u),0xFF0BF5EE);
  c.styled("+"+xp,(int)(180*u),(int)(219*u),(float)(4.3*u),0xFFFFFFFF);
  c.text("JOGADOR  Lv "+r.beforeLevel()+"  >  "+r.level(),(int)(57*u),(int)(267*u),0xFFC9FFFF);
  int bar=(int)(275*u);c.fill((int)(57*u),(int)(286*u),bar,(int)(5*u),0xFF102050);c.fill((int)(57*u),(int)(286*u),(int)(bar*Math.min(1,r.experience()/(double)Math.max(1,r.next()))),(int)(5*u),0xFF67FFFF);
  c.text(r.experience()+" / "+r.next()+" EXP",(int)(57*u),(int)(299*u),0xFFC9FFFF);
  panel(c,185*u,339*u,385*u,105*u,0xFF1721EE);
  c.styled("ITEM",(int)(205*u),(int)(324*u),(float)(4.5*u),0xFF04F4F0);
  c.styled(r.items(),(int)(215*u),(int)(373*u),(float)(1.65*u),0xFFFFFFFF);
  c.text(r.persona()+"  Lv "+r.personaLevel()+"   +"+r.exp()+" EXP",(int)(215*u),(int)(402*u),0xFFBBEFFF);
  if(r.shuffle())c.styled("SHUFFLE TIME +1",(int)(215*u),(int)(423*u),(float)(1.5*u),0xFF9CFF57);
  c.fill(0,h-(int)(33*u),w,(int)(33*u),0xEF060D35);
  c.text("RECOMPENSAS DA BATALHA",(int)(20*u),h-(int)(21*u),0xFFBCE9FF);
  c.styled("[ENTER] CONTINUAR",w-(int)(213*u),h-(int)(26*u),(float)(1.7*u),age>.7?0xFF7DFFFF:0xFF476582);
 }
 private static void panel(HudDesign.Canvas c,double x,double y,double w,double h,int color){BattleGraphics.poly(c,new double[]{x,y,x+w,y-43,x+w-18,y+h-27,x-14,y+h},color);BattleGraphics.poly(c,new double[]{x,y,x+w,y-43,x+w,y-40,x,y+3},0xFF12F4F6);}
}
