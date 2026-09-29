package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.HudDesign;
import dev.kosmx.playerAnim.core.util.Vec3f;

/** Shared clock: offer, light transfer, recipient acknowledgement, settle. */
public final class ShiftPresentation {
 public static final int TRANSFER=24,END=60;
 private static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
 public static double weight(double t){return smooth(t/8)*(1-smooth((t-46)/14));}
 public static Vec3f rotation(String part,double t,boolean recipient){
  double w=weight(t),reach=recipient?smooth((t-16)/10):1-smooth((t-24)/15);
  return switch(part){
   case "body"->new Vec3f((float)(-.06*w),(float)((recipient?-.12:.16)*w),0);
   case "head"->new Vec3f((float)((recipient?-.05:.04)*w),(float)((recipient?.12:-.16)*w),0);
   case "leftArm"->new Vec3f((float)(-(.25+1.02*reach)*w),(float)(-.3*w),(float)(-.2*w));
   case "rightArm"->new Vec3f((float)(.12*w),(float)(-.1*w),(float)(.08*w));
   default->Vec3f.ZERO;
  };
 }
 public static Vec3f bend(String part,double t,boolean recipient){return new Vec3f(0,part.equals("leftArm")?(float)(-.55*weight(t)):0,0);}
 public static void paint(HudDesign.Canvas c,int w,int h,double t,String from,String to){
  if(t<0||t>=END)return;double u=Math.min(w/640.,h/360.),in=smooth(t/7)*(1-smooth((t-49)/11));
  int x=(int)((w-340*u)/2),y=(int)(h*.13),alpha=(int)(in*245);
  if(t>=13&&t<21){double sweep=(t-13)/8,edge=(sweep*1.7-.35)*w;c.poly(new double[]{edge-w*.6,0,edge,0,edge+w*.25,h,edge-w*.35,h},0xE92390DF);}
  c.poly(new double[]{x,y,x+350*u,y-8*u,x+332*u,y+62*u,x-16*u,y+69*u},alpha<<24|0x082657);
  c.angled("SHIFT",x+(int)(10*u),y+(int)(4*u),(float)(2.4*u),alpha<<24|0x61EEFF,-6);
  String name=to;while(name.length()>1&&c.width(name)>200*u)name=name.substring(0,name.length()-1);
  c.text(from+"  →",x+(int)(142*u),y+(int)(9*u),alpha<<24|0xAEE9FF);
  c.styled(name,x+(int)(140*u),y+(int)(26*u),(float)(1.5*u),alpha<<24|0xFFFFFF);
  c.text(t<TRANSFER?"PASSANDO O TURNO":"TURNO RECEBIDO • DANO +50%",x+(int)(13*u),y+(int)(53*u),alpha<<24|0x73F5FF);
 }
 private ShiftPresentation(){}
}
