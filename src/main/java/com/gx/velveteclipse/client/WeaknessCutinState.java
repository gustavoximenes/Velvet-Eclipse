package com.gx.velveteclipse.client;

/** No local skill timer/affinity guess can trigger this effect. */
public final class WeaknessCutinState {
    private int lastId=Integer.MIN_VALUE,age=20;
    public void trigger(int id){if(id!=lastId){lastId=id;age=0;}}
    public void tick(){if(age<20)age++;}
    public void reset(){lastId=Integer.MIN_VALUE;age=20;}
    public boolean visible(){return age<20;}
    public double time(float partial){return Math.min(20,age+Math.max(0,Math.min(1,partial)));}
    public static double alpha(double t){return EvokerPose.smooth((t+.3)/2)*(1-EvokerPose.smooth((t-15)/5));}
}
