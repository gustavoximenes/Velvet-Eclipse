package com.gx.velveteclipse.client;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import dev.kosmx.playerAnim.core.util.Vec3f;

/** One cinematic clock for the hand path, elbow and prop. Model space, in blocks. */
public final class EvokerPose {
    private static final double[] TIMES={0,5,11,20,25,31,37,39,42,46,50,60};
    private static final double[][] HAND={{-.375,.745,0},{-.51,.51,-.09},{-.90,.04,-.10},
        {-.90,.02,-.10},{-.69,-.02,-.15},{-.52,-.18,-.04},{-.52,-.18,-.04},
        {-.55,-.20,-.04},{-.52,-.18,-.04},{-.86,0,-.12},{-.67,.35,-.08},{-.375,.745,0}};
    public record Arm(Vec3f rotation,Vec3f bend,Vector3f target) {}
    public static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
    public static double blend(double t){return smooth(t/3)*(1-smooth((t-54)/6));}
    public static Vector3f hand(double t){
        int i=0;while(i<TIMES.length-2&&t>TIMES[i+1])i++;
        double a=smooth((t-TIMES[i])/(TIMES[i+1]-TIMES[i]));
        return new Vector3f((float)mix(HAND[i][0],HAND[i+1][0],a),(float)mix(HAND[i][1],HAND[i+1][1],a),(float)mix(HAND[i][2],HAND[i+1][2],a));
    }
    public static Arm arm(double t,boolean slim){
        Vector3f target=hand(t),shoulder=new Vector3f(-5/16f,(slim?2.5f:2)/16f,0);
        float offset=slim?-.03125f:-.0625f;
        Vector3f initialUpper=new Vector3f(offset,.25f,0),delta=new Vector3f(target).sub(shoulder);
        float upper=initialUpper.length(),lower=.375f,d=Math.max(Math.abs(upper-lower)+.001f,Math.min(upper+lower-.001f,delta.length()));
        Vector3f direction=new Vector3f(delta).normalize();
        float a=(upper*upper-lower*lower+d*d)/(2*d),height=(float)Math.sqrt(Math.max(0,upper*upper-a*a));
        Vector3f outward=new Vector3f(-1,.15f,.12f);outward.sub(new Vector3f(direction).mul(outward.dot(direction))).normalize();
        Vector3f elbow=new Vector3f(shoulder).add(new Vector3f(direction).mul(a)).add(outward.mul(height));
        Quaternionf upperRotation=new Quaternionf().rotationTo(initialUpper.normalize(),new Vector3f(elbow).sub(shoulder).normalize());
        Vector3f lowerDirection=new Quaternionf(upperRotation).invert().transform(new Vector3f(target).sub(elbow).normalize());
        float bend=(float)Math.acos(Math.max(-1,Math.min(1,lowerDirection.y)));
        // Bendy's UP cuboid rotates around (cos(axis),0,sin(axis)).
        float axis=(float)Math.atan2(-lowerDirection.x,lowerDirection.z);
        // Explicit ZYX extraction: the bundled JOML 1.10.5 helper uses an
        // incorrect sign in its X denominator for combined-axis rotations.
        Quaternionf q=upperRotation;
        Vec3f angles=new Vec3f((float)Math.atan2(2*(q.w*q.x+q.y*q.z),1-2*(q.x*q.x+q.y*q.y)),
            (float)Math.asin(Math.max(-1,Math.min(1,2*(q.w*q.y-q.z*q.x)))),
            (float)Math.atan2(2*(q.w*q.z+q.x*q.y),1-2*(q.y*q.y+q.z*q.z)));
        return new Arm(angles,new Vec3f(axis,bend,0),target);
    }
    public static double spin(double t){return 2*Math.PI*smooth((t-10)/10);}
    public static Quaternionf weaponRotation(double t){
        double lift=smooth((t-4)/7),lower=smooth((t-43)/15);
        double angle=(1-lift+lower)*.55+spin(t);
        double recoil=t>=38&&t<=42?Math.sin((t-38)/4*Math.PI):0;
        return new Quaternionf().rotationY((float)Math.PI).rotateZ((float)(angle-.06*recoil));
    }
    private static double mix(double a,double b,double t){return a+(b-a)*t;}
}
