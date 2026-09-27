package com.gx.velveteclipse.client.vfx;

import com.gx.velveteclipse.core.persona.Element;
import java.util.*;

/** Bounded, deterministic effect choreography in target-local coordinates. */
public final class PhotonEffectPlan {
    public record Point(float x,float y,float z){}
    public record Beam(Point from,Point to,float width,int color,int delay,int life){}
    public record Plan(List<Beam> beams,int color,int sparks,float speed,float size,int life){}
    private static Point p(double x,double y,double z){return new Point((float)x,(float)y,(float)z);}
    public static int color(Element e){return switch(e){case FIRE->0xFFFF7030;case ICE->0xFF7AEFFF;case ELECTRIC->0xFFD3A4FF;case DARK->0xFFFF284B;case LIGHT->0xFFFFED9B;case WIND->0xFF72FFBB;case PSY->0xFFFF80DE;case NUCLEAR->0xFF70CFFF;case HEALING->0xFF7DFFB8;case SUPPORT->0xFF64DFFF;case AILMENT->0xFFD77AFF;default->0xFFD4EDFF;};}
    public static Plan create(Element e,float scale,long seed){
        scale=Math.max(.6f,Math.min(1.65f,scale));var out=new ArrayList<Beam>();int c=color(e);var random=new Random(seed);
        switch(e){
            case ELECTRIC->{
                for(int branch=0;branch<3;branch++){Point last=p((branch-1)*.7,5.8,0);for(int j=1;j<=9;j++){Point next=p(j==9?0:(random.nextDouble()-.5)*.6,5.8*(1-j/9.),(random.nextDouble()-.5)*.5);add(out,last,next,.065f,c,j/3+branch*2,10);last=next;}}
                ring(out,scale*1.1,.06,0,c,4,12,16);
            }
            case FIRE->{
                for(int i=0;i<8;i++){double a=i*Math.PI/4;Point base=p(Math.cos(a)*.2,.1,Math.sin(a)*.2),tip=p(Math.cos(a)*scale*1.4,1.2+random.nextDouble()*1.6,Math.sin(a)*scale*1.4);add(out,base,tip,.15f,c,3,14);}
                ring(out,scale*1.4,.07,0,c,3,15,24);
            }
            case ICE->{
                for(int i=0;i<7;i++){double a=i*Math.PI*2/7,r=.55*scale;Point base=p(Math.cos(a)*r,.03,Math.sin(a)*r),tip=p(base.x*1.45,1.3+random.nextDouble()*1.7,base.z*1.45);add(out,base,tip,.23f,c,i%3,26);add(out,base,tip,.06f,0xFFFFFFFF,i%3,20);}
                ring(out,1.1*scale,.04,0,c,0,24,20);
            }
            case DARK->{
                for(int i=0;i<4;i++)for(int j=0;j<10;j++){double a=i*Math.PI/2+j*.18,r=(1-j/12.)*1.7*scale;Point a1=p(Math.cos(a)*r,.15+j*.19,Math.sin(a)*r),b=p(Math.cos(a+.18)*(r-.12),.34+j*.19,Math.sin(a+.18)*(r-.12));add(out,a1,b,.085f,c,j/3,18);}
            }
            case WIND->{
                for(int band=0;band<2;band++)for(int j=0;j<24;j++){double a=j*Math.PI/8+band*Math.PI,r=scale*(1.2-j*.017),b=a+Math.PI/8;add(out,p(Math.cos(a)*r,j*.10,Math.sin(a)*r),p(Math.cos(b)*r,(j+1)*.10,Math.sin(b)*r),.075f,c,j/6,20);}
            }
            case LIGHT->{
                for(int i=0;i<6;i++){double a=i*Math.PI/3,x=Math.cos(a)*scale,z=Math.sin(a)*scale;add(out,p(x,4.5,z),p(x,.05,z),.11f,c,i,16);}
                ring(out,1.4*scale,.08,0,c,3,22,24);
            }
            case PSY,NUCLEAR,ALMIGHTY->{
                for(int band=0;band<3;band++)ring(out,scale*(1.1+band*.17),1.1,band*Math.PI/3+.5,c,band*2,22,16);
            }
            case HEALING,SUPPORT,AILMENT->{for(int i=0;i<3;i++)ring(out,.65*scale,.15+i*.6,0,c,i*3,24,16);}
            default->{
                for(int band=0;band<2;band++)for(int j=0;j<10;j++){double a=-1.2+j*.24,b=a+.24;add(out,p(Math.cos(a)*scale-.5,1+Math.sin(a)*scale,band*.15),p(Math.cos(b)*scale-.5,1+Math.sin(b)*scale,band*.15),.09f,band==0?c:0xFFFFFFFF,j/4,8);}
            }
        }
        return new Plan(List.copyOf(out),c,e==Element.FIRE?32:18,e==Element.ICE?.09f:.16f,e==Element.FIRE?.4f:.15f,24);
    }
    private static void add(List<Beam> out,Point a,Point b,float w,int c,int delay,int life){out.add(new Beam(a,b,w,c,delay,life));}
    private static void ring(List<Beam> out,double r,double y,double tilt,int color,int delay,int life,int n){for(int i=0;i<n;i++){double a=i*Math.PI*2/n,b=(i+1)*Math.PI*2/n;add(out,p(Math.cos(a)*r,y+Math.sin(a)*r*Math.sin(tilt),Math.sin(a)*r*Math.cos(tilt)),p(Math.cos(b)*r,y+Math.sin(b)*r*Math.sin(tilt),Math.sin(b)*r*Math.cos(tilt)),.065f,color,delay,life);}}
}
