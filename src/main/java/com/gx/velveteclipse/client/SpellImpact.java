package com.gx.velveteclipse.client;

import java.util.*;
import org.joml.Vector3f;

/** Original, bounded procedural geometry. Time zero is cinematic tick 82; impact is tick 86. */
public final class SpellImpact {
    public record Stroke(Vector3f from,Vector3f to,float width,int color,float alpha){}
    public static final int LIFETIME=23;
    private static final double TAU=Math.PI*2;
    private SpellImpact(){}
    private static Vector3f p(double x,double y,double z){return new Vector3f((float)x,(float)y,(float)z);}
    private static void add(List<Stroke> out,Vector3f a,Vector3f b,double w,int color,double alpha){if(w>0&&alpha>0)out.add(new Stroke(a,b,(float)w,color,(float)Math.min(1,alpha)));}
    public static List<Stroke> geometry(String id,double age,long seed){
        if(age<0||age>=LIFETIME)return List.of();
        var out=new ArrayList<Stroke>();var r=new Random(seed);
        double grow=Math.min(1,(age+1)/5),fade=age<8?1:Math.max(0,(LIFETIME-age)/(LIFETIME-8.0));
        if(id.equals("eiga"))curse(out,age,grow,fade,r);
        else if(id.equals("agi"))fire(out,age,grow,fade,r);
        return List.copyOf(out);
    }
    private static void ring(List<Stroke> out,double radius,double y,double width,int color,double alpha){
        for(int i=0;i<48;i++){double a=i*TAU/48,b=(i+1)*TAU/48;add(out,p(Math.cos(a)*radius,y,Math.sin(a)*radius),p(Math.cos(b)*radius,y,Math.sin(b)*radius),width,color,alpha);}
    }
    private static void curse(List<Stroke> out,double age,double grow,double fade,Random r){
        // Six sweeping hooked blades converge around the selected enemy, with red barbs.
        for(int blade=0;blade<6;blade++){
            double angle=blade*TAU/6+age*.035,tilt=(blade%2==0?1:-1)*.28;
            for(int i=0;i<20;i++){
                double t=i/20.0,u=(i+1)/20.0;
                var a=hook(t,angle,tilt,grow);var b=hook(u,angle,tilt,grow);
                double taper=Math.pow(Math.sin(Math.PI*(t+.02)),.7)*fade;
                add(out,a,b,.17*taper,0xE61925,fade*.38);
                add(out,a,b,.058*taper,0xFF402B,fade);
                add(out,a,b,.014*taper,0xFFE9D5,fade*.9);
                if(i==6||i==12){var tip=new Vector3f(b).add((float)(Math.cos(angle)*.28),.35f,(float)(Math.sin(angle)*.28));add(out,b,tip,.035*taper,0xFF422F,fade);}
            }
        }
        if(age>=4){
            ring(out,.65+(age-4)*.04,.07,.045,0xD41B38,fade*.6);
            sparks(out,age,fade,r,0xFFF5E6,14);
        }
    }
    private static Vector3f hook(double t,double angle,double tilt,double grow){
        double a=-1.3+t*4.65,radius=(.55+Math.sin(t*Math.PI)*.75)*grow;
        double radial=Math.cos(a)*radius;
        return p(Math.cos(angle)*radial-Math.sin(angle)*tilt*Math.sin(a),.95+Math.sin(a)*radius,Math.sin(angle)*radial+Math.cos(angle)*tilt*Math.sin(a));
    }
    private static void fire(List<Stroke> out,double age,double grow,double fade,Random r){
        ring(out,(.35+grow*.8)+(Math.max(0,age-4)*.035),.055,.07,0xFF3514,fade);
        ring(out,(.24+grow*.64)+(Math.max(0,age-4)*.025),.065,.026,0xFFCF50,fade);
        // Layered curved flames make a compact fireball that blossoms upward at impact.
        for(int flame=0;flame<12;flame++){
            double angle=flame*TAU/12+r.nextDouble()*.12,height=.8+r.nextDouble()*.85;
            for(int i=0;i<12;i++){
                double t=i/12.0,u=(i+1)/12.0;
                var a=flame(t,angle,height,grow,age);var b=flame(u,angle,height,grow,age);
                double width=(1-t)*.12*fade;
                add(out,a,b,width*2.6,0xFF3310,fade*.28);
                add(out,a,b,width,0xFF9A20,fade*.9);
                add(out,a,b,width*.37,0xFFF2A1,fade*.85);
            }
        }
        if(age>=4){
            // A filled, irregular fireball core beneath the rising tongues, not a wire cage.
            double pulse=1+Math.sin(age*1.7)*.05;
            for(int band=1;band<=5;band++){
                double latitude=band*Math.PI/6,radius=Math.sin(latitude)*.60*pulse;
                double y=.76+Math.cos(latitude)*.61;
                for(int i=0;i<24;i++){
                    double a=i*TAU/24,b=(i+1)*TAU/24;
                    double ra=radius*(1+.09*Math.sin(a*5+age)),rb=radius*(1+.09*Math.sin(b*5+age));
                    add(out,p(Math.cos(a)*ra,y,Math.sin(a)*ra),p(Math.cos(b)*rb,y,Math.sin(b)*rb),.13*fade,band<3?0xFFBA38:0xFF6A17,fade*.8);
                }
            }
            sparks(out,age,fade,r,0xFFBA43,20);
        }
    }
    private static Vector3f flame(double t,double a,double h,double grow,double age){
        double radius=Math.sin(t*Math.PI)*.65*grow,twist=a+t*1.7+age*.07;
        return p(Math.cos(twist)*radius,.12+t*h*grow,Math.sin(twist)*radius);
    }
    private static void sparks(List<Stroke> out,double age,double fade,Random r,int color,int n){
        double distance=.6+Math.max(0,age-4)*.12;
        for(int i=0;i<n;i++){
            double a=r.nextDouble()*TAU,y=r.nextDouble()*1.2-.3;
            var dir=p(Math.cos(a),y,Math.sin(a)).normalize();
            var from=new Vector3f(dir).mul((float)distance).add(0,.65f,0);
            var to=new Vector3f(from).fma((float)(.4+r.nextDouble()*.6),dir);
            add(out,from,to,.015,0xFFFFFF,fade);
            add(out,from,to,.04,color,fade*.65);
        }
    }
}
