package com.gx.velveteclipse.client.menu;

/** Wall-clock driven transitions keep the same duration at different frame rates. */
public final class MenuMotion {
    public static double clamp(double v){return Math.max(0,Math.min(1,v));}
    public static double ease(double t){t=clamp(t);return 1-Math.pow(1-t,3);}
    public static double smooth(double t){t=clamp(t);return t*t*(3-2*t);}
    public static double approach(double value,double target,double seconds){return target+(value-target)*Math.exp(-Math.min(.1,Math.max(0,seconds))*19);}
    public static double entry(double elapsed,int index){return ease((elapsed-index*.021)/.31);}
    public static double wipe(double elapsed){return smooth(elapsed/.28);}
}
