package com.gx.velveteclipse.client.darkhour;
public final class DeadlineLayout {
    private DeadlineLayout(){}
    public static double x(int width,boolean battle){return battle?Math.max(8,width-140):16;}
}
