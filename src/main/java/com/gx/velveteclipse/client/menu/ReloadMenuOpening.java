package com.gx.velveteclipse.client.menu;

/** Opening studied at 2.30–3.02 s in the supplied Pause Menu recording. */
public final class ReloadMenuOpening {
    public static final double DURATION=.72;
    public record Portrait(double angle,double x,double y){}
    public static boolean active(double age,boolean motion){return motion&&age<DURATION;}
    private static double progress(double age,double delay,double span,boolean motion){return motion?MenuMotion.ease((age-delay)/span):1;}
    public static double panel(double age,boolean motion){return progress(age,.055,.30,motion);}
    public static double option(double age,int index,boolean motion){return progress(age,.20+index*.014,.32,motion);}
    public static Portrait portrait(double age,boolean motion){double p=progress(age,.025,.47,motion);return new Portrait(211-205*(1-p),305-510*(1-p),252-105*(1-p));}
    public static double splash(double age,boolean motion){return motion?1-MenuMotion.smooth((age-.10)/.48):0;}
    public static MenuDesign.Canvas layer(MenuDesign.Canvas c,double dx,double dy,double alpha){return new MenuDesign.Canvas(){
        public void fill(double x,double y,double w,double h,int color){c.fill(x+dx,y+dy,w,h,MenuDesign.alpha(color,alpha));}
        public void poly(double[] points,int color){double[] p=points.clone();for(int i=0;i<p.length;i+=2){p[i]+=dx;p[i+1]+=dy;}c.poly(p,MenuDesign.alpha(color,alpha));}
        public void text(String t,double x,double y,double size,int color,double angle){c.text(t,x+dx,y+dy,size,MenuDesign.alpha(color,alpha),angle);}
        public void body(String t,double x,double y,double size,int color,double angle){c.body(t,x+dx,y+dy,size,MenuDesign.alpha(color,alpha),angle);}
        public double width(String t,double size){return c.width(t,size);}
        public double bodyWidth(String t,double size){return c.bodyWidth(t,size);}
        public void item(int slot,double x,double y){c.item(slot,x+dx,y+dy);}
    };}
    public static void paint(MenuDesign.Canvas target,double age,boolean motion){
        double fade=splash(age,motion);if(fade<=0)return;var c=ReloadMenuDesign.scaled(target);
        c.fill(0,0,1280,720,MenuDesign.alpha(0xA548F8F2,fade));
        // Ascending water trails; the irregular edges break into rings as the menu settles.
        for(int i=0;i<6;i++){
            double x=210+i*122+Math.sin(i*1.7+age*5)*42,top=-100-age*750,bottom=690-age*280,w=(56+i%3*21)*fade;
            double[] edge=new double[100];
            for(int j=0;j<25;j++){double t=j/24.,y=top+(bottom-top)*t,center=x+Math.sin(t*7+i+age*4)*30,half=w*(.35+.22*Math.sin(t*17+i))*Math.sqrt(1-t*t);edge[j*2]=center-half;edge[j*2+1]=y;edge[(49-j)*2]=center+half;edge[(49-j)*2+1]=y;}
            c.poly(edge,MenuDesign.alpha(0xDCB0FFFF,fade));
        }
        for(int i=0;i<28;i++){
            double x=125+i*173%1090+Math.sin(i+age*3)*24,y=(i*97%800)-age*(110+i%5*40),r=(5+i%6*5)*(1+age*.7);
            for(int j=0;j<14;j++){double a=j*Math.PI/7,b=(j+1)*Math.PI/7,inner=Math.max(1,r-2.3);
                c.poly(new double[]{x+Math.cos(a)*r,y+Math.sin(a)*r,x+Math.cos(b)*r,y+Math.sin(b)*r,x+Math.cos(b)*inner,y+Math.sin(b)*inner,x+Math.cos(a)*inner,y+Math.sin(a)*inner},MenuDesign.alpha(0xCBB1FFFF,fade));}
        }
    }
}
