package com.gx.velveteclipse.client.vfx;
import org.joml.Vector3f;
import java.util.*;

/** Filled curved flames from the approved shared-circle study, in world units. */
public final class SharedSummonFlames {
 public static final double SEPARATION=.64,PERSONA_HEIGHT=1.10,RADIUS=.90;
 public record Face(List<Vector3f> vertices,int color,float alpha){}
 private record P(double x,double y){}
 public static double life(double tick){return smooth((tick-38)/5.2)*(1-smooth((tick-101)/7));}
 public static double strength(double tick){return life(tick)*(.8+.44*Math.exp(-Math.pow((tick-48)/6.8,2)));}
 private static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
 private static double cross(P a,P b,P c){return(b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x);}
 private static void bezier(List<P> points,P a,P b,P c,P d){for(int i=points.isEmpty()?0:1;i<=8;i++){double u=i/8.,v=1-u;points.add(new P(a.x*v*v*v+3*b.x*u*v*v+3*c.x*u*u*v+d.x*u*u*u,a.y*v*v*v+3*b.y*u*v*v+3*c.y*u*u*v+d.y*u*u*u));}}
 private static List<P> shape(double curl,double fork){var p=new ArrayList<P>();bezier(p,new P(-.5,0),new P(-.79,.23),new P(-.43,.35),new P(-.38,.52));bezier(p,p.get(p.size()-1),new P(-.4,.73),new P(curl-.05,.80),new P(curl,1));bezier(p,p.get(p.size()-1),new P(curl+.30,.78),new P(.02,.70),new P(.12,.50));bezier(p,p.get(p.size()-1),new P(.30,.52),new P(.43,.58),new P(.52,fork));bezier(p,p.get(p.size()-1),new P(.34,.39),new P(.81,.17),new P(.5,0));Collections.reverse(p);return p;}
 private static List<int[]> triangles(List<P> points){var index=new ArrayList<Integer>();for(int i=0;i<points.size();i++)index.add(i);var result=new ArrayList<int[]>();int budget=points.size()*points.size();while(index.size()>2&&budget-->0){boolean cut=false;for(int i=0;i<index.size();i++){int a=index.get((i+index.size()-1)%index.size()),b=index.get(i),c=index.get((i+1)%index.size());P pa=points.get(a),pb=points.get(b),pc=points.get(c);if(cross(pa,pb,pc)<=1e-12)continue;boolean inside=false;for(int j:index)if(j!=a&&j!=b&&j!=c){P p=points.get(j);if(cross(pa,pb,p)>=-1e-12&&cross(pb,pc,p)>=-1e-12&&cross(pc,pa,p)>=-1e-12){inside=true;break;}}if(!inside){result.add(new int[]{a,b,c,c});index.remove(i);cut=true;break;}}if(!cut)break;}return result;}
 private static Vector3f point(double x,double y,double z){return new Vector3f((float)x,(float)y,(float)z);}
 public static List<Face> geometry(double tick,Vector3f cameraRight,Vector3f back){
  double power=strength(tick);if(power<=0)return List.of();double t=tick/20;var out=new ArrayList<Face>();
  // Soft single floor glow, all annuli share the midpoint of the two actors.
  for(int j=0;j<18;j++){double r0=j*.078,r1=(j+1)*.078;for(int i=0;i<48;i++){double a=i*Math.PI*2/48,b=(i+1)*Math.PI*2/48;out.add(new Face(List.of(point(Math.cos(a)*r0,.012,Math.sin(a)*r0),point(Math.cos(b)*r0,.012,Math.sin(b)*r0),point(Math.cos(b)*r1,.012,Math.sin(b)*r1),point(Math.cos(a)*r1,.012,Math.sin(a)*r1)),0x164CFA,(float)(.18*power*Math.pow(1-j/18.,2))));}}
  var groups=new ArrayList<List<Face>>();
  for(int i=0;i<25;i++){
   double angle=i*Math.PI*2/25+.23,rx=Math.cos(angle),rz=Math.sin(angle),cycle=(t*(1.2+.075*(i%5))+i*.618+.13)%1;
   double rr=RADIUS*(.79+.13*Math.sin(t*5+i*2.4)),rear=Math.max(0,rx*back.x+rz*back.z);
   double h=(.58+.45*((i*7)%11)/10.)*(.58+.42*Math.sin(Math.PI*cycle))*power*(1+.32*rear),w=(.20+.095*((i*3)%7)/6.)*(.72+.28*power);
   var shape=shape(.35*Math.sin(t*8.1+i*2.39)+.20*Math.sin(t*14+i*1.11),.67+.16*Math.sin(t*7+i));var tris=triangles(shape);var group=new ArrayList<Face>();
   double[] sx={1,.78,.53},sy={1,.97,.90};int[] col={0x122BF5,0x1FADFF,0xC6FBFF};
   for(int layer=0;layer<3;layer++){var vertices=new ArrayList<Vector3f>();for(var p:shape)vertices.add(point(rx*rr+cameraRight.x*p.x*w*sx[layer],.025+p.y*h*sy[layer],rz*rr+cameraRight.z*p.x*w*sx[layer]));for(var indices:tris){var quad=new ArrayList<Vector3f>();for(int index:indices)quad.add(vertices.get(index));group.add(new Face(quad,col[layer],(float)Math.min(1,power*1.5)));}}
   groups.add(group);
  }
  // cameraRight cross up is the horizontal vector toward the camera.
  Vector3f toward=new Vector3f(cameraRight).cross(0,1,0);
  groups.sort(Comparator.comparingDouble(g->g.get(0).vertices().get(0).dot(toward)));for(var group:groups)out.addAll(group);
  for(int i=0;i<34;i++){double phase=(t*.44+i*.618+.03)%1,angle=i*2.399+t*.37,size=(.017+.015*(i%3)/2.)*Math.sin(Math.PI*phase)*life(tick);var pos=point(Math.cos(angle)*(.72+.15*phase),.20+phase*3,Math.sin(angle)*(.72+.15*phase));var right=new Vector3f(cameraRight).mul((float)size);out.add(new Face(List.of(new Vector3f(pos).add(0,(float)(-size*2.6),0),new Vector3f(pos).add(right),new Vector3f(pos).add(0,(float)(size*2.6),0),new Vector3f(pos).sub(right)),i%3==0?0xBCF9FF:0x44BFFF,(float)life(tick)));}
  double age=t-2.13;if(age>0&&age<.58){double radius=.68+age*2.2;for(int i=0;i<73;i++){double a=i*Math.PI*2/73,b=(i+1)*Math.PI*2/73;out.add(new Face(List.of(point(Math.cos(a)*radius,.035,Math.sin(a)*radius),point(Math.cos(b)*radius,.035,Math.sin(b)*radius),point(Math.cos(b)*(radius-.035),.035,Math.sin(b)*(radius-.035)),point(Math.cos(a)*(radius-.035),.035,Math.sin(a)*(radius-.035))),0x53DFFF,(float)((1-age/.58)*.85)));}}
  return out;
 }
 private SharedSummonFlames(){}
}
