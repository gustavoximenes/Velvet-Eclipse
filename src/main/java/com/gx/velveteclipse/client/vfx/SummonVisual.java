package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.client.SpellImpact.Stroke;
import org.joml.Vector3f;
import java.util.*;

/** Blue flame crown, white hot foot ring and detached rising fragments. No opaque body column. */
public final class SummonVisual {
 public static final int LIFE=42;
 private static Vector3f p(double x,double y,double z){return new Vector3f((float)x,(float)y,(float)z);}
 private static void line(List<Stroke> out,Vector3f a,Vector3f b,double width,int color,double alpha){if(alpha>0)out.add(new Stroke(a,b,(float)width,color,(float)Math.min(1,alpha)));}
 public static List<Stroke> geometry(double t,long seed){
  if(t<0||t>=LIFE)return List.of();var out=new ArrayList<Stroke>();var random=new Random(seed);
  double fade=Math.min(1,(t+1)/4)*Math.min(1,(LIFE-t)/12.),pulse=1+.055*Math.sin(t*.7);
  for(int i=0;i<52;i++){
   double a=i*Math.PI*2/52,b=(i+1)*Math.PI*2/52,r=.72*pulse;
   line(out,p(Math.cos(a)*r,.08,Math.sin(a)*r),p(Math.cos(b)*r,.08,Math.sin(b)*r),.12,0x153BFF,fade*.7);
   line(out,p(Math.cos(a)*r,.10,Math.sin(a)*r),p(Math.cos(b)*r,.10,Math.sin(b)*r),.028,0xCFFFFF,fade);
  }
  for(int i=0;i<19;i++){
   double angle=i*Math.PI*2/19,peak=(.45+random.nextDouble()*.85)*(1+.12*Math.sin(t*.55+i)),r=.55+random.nextDouble()*.25;
   for(int j=0;j<7;j++){
    double u=j/7.,v=(j+1)/7.;var a=flame(angle,r,peak,u,t+i);var b=flame(angle,r,peak,v,t+i);
    line(out,a,b,.105*(1-u)+.008,0x2445FF,fade*(1-u*.5));line(out,a,b,.032*(1-u)+.005,0xC4FAFF,fade*(1-u*.8));
   }
  }
  for(int i=0;i<30;i++){
   double age=(t+random.nextDouble()*24)%32,angle=random.nextDouble()*Math.PI*2+age*.045,r=.65+random.nextDouble()*.8;
   double y=.2+age*.12,size=.025+random.nextDouble()*.07,alpha=fade*Math.sin(age/32*Math.PI);
   var at=p(Math.cos(angle)*r,y,Math.sin(angle)*r);var end=new Vector3f(at).add((float)(size*.35),(float)(size*2.8),(float)(size*.2));
   line(out,at,end,size*.5,i%3==0?0xFFFFFF:0x85E9FF,alpha);
   if(i%4==0){line(out,new Vector3f(at).add((float)-size,0,0),new Vector3f(at).add((float)size,0,0),.012,0xECFFFF,alpha);}
  }
  return out;
 }
 private static Vector3f flame(double angle,double r,double peak,double u,double t){double sway=Math.sin(t*.5+u*3)*.14*u;return p(Math.cos(angle+sway)*r*(1-u*.25),.08+peak*u,Math.sin(angle+sway)*r*(1-u*.25));}
 private SummonVisual(){}
}
