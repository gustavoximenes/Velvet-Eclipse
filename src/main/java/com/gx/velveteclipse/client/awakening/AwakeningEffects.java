package com.gx.velveteclipse.client.awakening;

import com.gx.velveteclipse.client.menu.MenuCanvas;
import java.util.Random;
import static java.lang.Math.*;

/** Procedural ribbons, fragments and distinct Agi / Zio / Eiga impact silhouettes. */
final class AwakeningEffects {
    private static final double TAU=PI*2;
    static void sky(MenuCanvas c,double t){
        for(int y=0;y<540;y+=4){double u=y/540.;int r=(int)(3+18*u),g=(int)(11+32*u),b=(int)(18+19*u);c.fill(0,y,960,4,0xFF000000|r<<16|g<<8|b);}
        for(int i=12;i>0;i--)circle(c,738,100,78+i*4,78+i*4,0x03206017);
        circle(c,738,100,78,78,0xFF89BD3B);var rng=new Random(51);
        for(int i=0;i<52;i++){double x=(rng.nextDouble()*2-1)*78,y=(rng.nextDouble()*2-1)*78,r=2+rng.nextDouble()*8;if(x*x+y*y<pow(78-r,2))circle(c,738+x,100+y,r,r,0x4575A137);}
        for(int i=0;i<11;i++){double x=(i*131+t*3)%1300-170,y=88+i*67%125;circle(c,x+90,y,120,14,0xA004181D);}
        for(int i=0;i<17;i++){int x=i*64-18,h=30+i*47%73;c.fill(x,240-h,53,h+10,0xFF051219);for(int k=0;k<3;k++)for(int j=0;j<3;j++)if((i+k+j)%3==0)c.fill(x+8+k*15,250-h+j*16,4,5,0xFF244033);}
    }
    static void paint(MenuCanvas c,AwakeningScene scene,double real,double t,String name,int color){
        if(t>14.4&&t<16){double a=AwakeningScene.smooth((t-14.4)/1.6);for(int i=0;i<8;i++){double ang=i*TAU/8+.2,r=a*680;line(c,461,259,461+cos(ang+.05)*r*.38,259+sin(ang+.05)*r*.38,1.6,alpha(0x94E7FF,a*.8));line(c,461+cos(ang+.05)*r*.38,259+sin(ang+.05)*r*.38,461+cos(ang)*r,259+sin(ang)*r,1.6,alpha(0x94E7FF,a*.8));}if(t>15.65)c.fill(0,0,960,540,alpha(0x071B4A,AwakeningScene.smooth((t-15.65)/.35)));}
        if(t>=26.2&&t<30)beam(c,scene,t);
        if(t>=26.2&&t<39){double a=1-AwakeningScene.smooth((t-39)/3);ring(c,scene,-1.7,.04,1.65,1.38*AwakeningScene.smooth((t-26.2)/1.9),alpha(t<30?0x2CB1FF:color,a*.65),t);}
        if(t>26.2&&t<34.7)shards(c,real,0x48C6FF,451,270,76,1,4);
        if(t>24.7&&t<26.2)shards(c,(t-24.7)*2.6,0x87E8FF,413,248,95,2,18);
        if(t>=34.7&&t<37.2)impact(c,scene,t,name,color);
        if(t>36&&t<39.1){double[] p=scene.project(1,.7,-5.2);shards(c,(t-36)*1.4,0x7CB6F0,p[0],p[1],72,1.25,12);}
        double flash=max(0,1-abs(t-24.88)/.23)*.9;if(t>=34.7)flash=max(flash,max(0,1-abs(t-35.15)/.18)*.65);if(flash>0)c.fill(0,0,960,540,alpha(0xDCF5FF,flash));
    }
    private static void beam(MenuCanvas c,AwakeningScene s,double t){
        double u=t-26.2,power=AwakeningScene.smooth(u/.28)*(1-AwakeningScene.smooth((u-2.05)/1.65));
        for(int i=0;i<26;i++){
            double a=i*TAU/26+t*1.35,r=.42+i%5*.23;double[] last=null;
            for(int j=0;j<32;j++){double y=.04+j*5.66/31,angle=a+y*.5+.1*sin(y*3+t*7);double[] p=s.project(-1.7+cos(angle)*r*(.87+.1*sin(y*2)),y,1.65+sin(angle)*r);
                if(last!=null){line(c,last[0],last[1],p[0],p[1],12,alpha(0x0969FF,power*.17));line(c,last[0],last[1],p[0],p[1],2,alpha(0x69E1FF,power*.65));}last=p;}
        }
        for(int i=0;i<24;i++){double y=(i*.293+u*1.4)%4.9+.1,a=i*2.4+u*.65,r=.65+i%4*.19,x=-1.7+cos(a)*r,z=1.65+sin(a)*r;double[] p=s.project(x,y,z),q=s.project(x+.15,y+.32,z),v=s.project(x+.03,y+.68,z),w=s.project(x-.12,y+.22,z);c.poly(new double[]{p[0],p[1],q[0],q[1],v[0],v[1],w[0],w[1]},alpha(0x48BFFF,power*.62));}
    }
    private static void impact(MenuCanvas c,AwakeningScene s,double t,String name,int col){
        double u=t-34.7,e=AwakeningScene.smooth(u/.35)*(1-AwakeningScene.smooth((u-1)/1.5));var p=s.project(1,.78,-5.2);double x=p[0],y=p[1];
        if(name.equals("orpheus")){
            double r=(35+50*sin(min(u,1.5)*1.5))*e;
            for(int i=0;i<22;i++){double a=i*TAU/22,rr=r*(.8+.2*sin(i*7+u*5)),xx=x+cos(a)*rr,yy=y+sin(a)*rr*.7;c.poly(new double[]{xx-9,yy+12,xx+6,yy-25-i%4*12,xx+13,yy+10},alpha(0xFF6917,e*.9));}
            circle(c,x,y,r*1.25,r*1.25,alpha(0xFF590A,e*.18));circle(c,x,y,r,r,alpha(0xFF5A0A,e*.92));circle(c,x,y,r*.69,r*.69,alpha(0xFFB837,e*.95));circle(c,x,y,r*.39,r*.39,alpha(0xFFF8AF,e));
            for(int i=0;i<22;i++){double a=i*2.4,rr=(65+u*85)*(1+i%3*.22);line(c,x+cos(a)*rr,y+sin(a)*rr*.7,x+cos(a)*(rr+18),y+sin(a)*rr*.7+sin(a)*18,2,alpha(0xFFCB55,e*.85));}
        }else if(name.equals("izanagi")){
            var rng=new Random(90+(int)(u*8));for(int i=0;i<5;i++){double lx=x+(i-2)*18,ly=30;for(int j=1;j<12;j++){double nx=x+(rng.nextDouble()*68-34)*(.4+j/16.),ny=30+(y-30)*j/11;line(c,lx,ly,nx,ny,13,alpha(0xFFBF19,e*.35));line(c,lx,ly,nx,ny,3,alpha(0xFFFBA7,e));lx=nx;ly=ny;}}circle(c,x,y,30*e,30*e,alpha(0xFBFDD6,e));
        }else{
            circle(c,x,y,35*e,51*e,alpha(0x0B0014,e));
            for(int i=0;i<6;i++){double a=i*TAU/6+u*1.4,lx=0,ly=0;for(int j=0;j<23;j++){double f=j/22.,r=13+(1-f)*125*e,ang=a+f*1.3,nx=x+cos(ang)*r,ny=y+sin(ang)*r*.72;if(j>0){line(c,lx,ly,nx,ny,12,alpha(0xED142D,e*.2));line(c,lx,ly,nx,ny,7,alpha(0xED142D,e));line(c,lx,ly,nx,ny,2,alpha(0xFFAB8A,e));}lx=nx;ly=ny;}}circle(c,x,y,11,18,alpha(0xF5414B,e));
        }
        ring(c,s,1,.045,-5.2,.5+u*1.1,alpha(name.equals("orpheus")?0xFF9027:col,e*.85),t);
    }
    private static void ring(MenuCanvas c,AwakeningScene s,double x,double y,double z,double r,int color,double t){
        for(double factor:new double[]{1,.91}){double[] last=null;for(int i=0;i<=90;i++){double a=i*TAU/90;var p=s.project(x+cos(a)*r*factor,y,z+sin(a)*r*factor);if(last!=null)line(c,last[0],last[1],p[0],p[1],1.5,color);last=p;}}
        for(int i=0;i<18;i++){double a=i*TAU/18+t*.22;var p=s.project(x+cos(a)*r*.95,y,z+sin(a)*r*.95);var q=s.project(x+cos(a)*r*1.05,y,z+sin(a)*r*1.05);line(c,p[0],p[1],q[0],q[1],2,color);}
    }
    private static void shards(MenuCanvas c,double t,int color,double cx,double cy,int amount,double strength,int seed){var rng=new Random(seed);for(int i=0;i<amount;i++){double phase=rng.nextDouble(),speed=rng.nextDouble(),angle=rng.nextDouble(),rad=rng.nextDouble(),life=(phase+t*(.25+speed*.16))%1,x=cx+cos(angle*TAU)*life*960*.54,y=cy+sin(angle*TAU)*life*540*.6-t*.7,size=(1-life)*(3+rad*7)*strength;c.poly(new double[]{x-size,y+size*.4,x+size*.7,y-size,x+size*.32,y+size},alpha(i%4==0?0xF0FCFF:color,(1-life)*.85));}}
    private static void circle(MenuCanvas c,double x,double y,double rx,double ry,int col){if(rx<=0||ry<=0)return;double[] points=new double[64];for(int i=0;i<32;i++){points[i*2]=x+cos(i*TAU/32)*rx;points[i*2+1]=y+sin(i*TAU/32)*ry;}c.poly(points,col);}
    private static void line(MenuCanvas c,double x,double y,double a,double b,double width,int color){double dx=a-x,dy=b-y,l=hypot(dx,dy);if(l<.01)return;double nx=-dy/l*width*.5,ny=dx/l*width*.5;c.poly(new double[]{x+nx,y+ny,a+nx,b+ny,a-nx,b-ny,x-nx,y-ny},color);}
    private static int alpha(int c,double a){return (int)(255*max(0,min(1,a)))<<24|c&0xFFFFFF;}
}
