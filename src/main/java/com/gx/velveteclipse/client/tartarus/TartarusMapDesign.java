package com.gx.velveteclipse.client.tartarus;
import com.gx.velveteclipse.client.menu.MenuDesign.Canvas;
import com.gx.velveteclipse.darkhour.TartarusMapNetwork.View;
/** North-up floor silhouette; no geometry is drawn for unexplored sectors. */
public final class TartarusMapDesign {
 public static void paint(Canvas c,View data,double x,double y,double size,double px,double pz,double yaw,boolean expanded){
  c.poly(new double[]{x-9,y-10,x+size+12,y-6,x+size+6,y+size+33,x-14,y+size+33},0xC8071738);
  double scale=(size-8)/Math.max(data.width(),data.height()),left=x+(size-data.width()*scale)/2,top=y+(size-data.height()*scale)/2;
  for(int z=0;z<data.height();z++)for(int i=0;i<data.width();){if(!data.tile(i,z)){i++;continue;}int start=i;while(i<data.width()&&data.tile(i,z))i++;c.fill(left+start*scale,top+z*scale,(i-start)*scale,scale+.12,0xFF72BDEB);}
  for(int z=0;z<data.height();z++)for(int i=0;i<data.width();i++)if(data.tile(i,z)){
   double a=left+i*scale,b=top+z*scale,t=Math.max(.6,scale*.18);
   if(!data.tile(i-1,z))c.fill(a,b,t,scale,0xFFE9FBFF);if(!data.tile(i+1,z))c.fill(a+scale-t,b,t,scale,0xFFE9FBFF);
   if(!data.tile(i,z-1))c.fill(a,b,scale,t,0xFFE9FBFF);if(!data.tile(i,z+1))c.fill(a,b+scale-t,scale,t,0xFFE9FBFF);
  }
  double icon=expanded?2:1;for(var m:data.markers()){
   double a=left+(m.x()-data.minX())*scale,b=top+(m.z()-data.minZ())*scale;
   if(m.kind()==0){c.poly(new double[]{a,b-3*icon,a+3*icon,b,a,b+3*icon,a-3*icon,b},0xFFEF335C);}
   else if(m.kind()==1){c.fill(a-5*icon,b-5*icon,10*icon,10*icon,0xFF071A3C);for(int i=0;i<4;i++)c.fill(a+(-4+i*2)*icon,b+(3-i*2)*icon,(8-i*2)*icon,icon,0xFFEEFFFF);}
   else{c.fill(a-4*icon,b-3*icon,8*icon,6*icon,0xFFFFE276);c.fill(a-4*icon,b-icon,8*icon,icon,0xFF725029);c.fill(a-icon,b-icon,2*icon,2*icon,0xFFFCFFFF);}
  }
  double a=left+(px-data.minX())*scale,b=top+(pz-data.minZ())*scale,angle=Math.toRadians(yaw),dx=-Math.sin(angle),dz=Math.cos(angle),r=expanded?7:4;
  c.poly(new double[]{a+dx*r,b+dz*r,a-dx*r*.7+dz*r*.7,b-dz*r*.7-dx*r*.7,a-dx*r*.3,b-dz*r*.3,a-dx*r*.7-dz*r*.7,b-dz*r*.7+dx*r*.7},0xFFFFE75A);
  c.poly(new double[]{x-15,y+size+4,x+size+10,y+size+4,x+size+3,y+size+30,x-18,y+size+30},0xEE113386);
  String label=data.floor()==0?"HALL":data.floor()+"F";c.text("TÁRTARO",x-5,y+size+7,7,0xFFDFEEFF,0);double labelSize=data.floor()==0?17:20;c.text(label,x+size-c.width(label,labelSize)-3,y+size+13,labelSize,0xFFF4FFFF,-4);
  c.text("N",x+size-5,y-10,10,0xFF83F5FF,0);
 }
}
