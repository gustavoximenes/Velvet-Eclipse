package com.gx.velveteclipse.client;

/** GUI-scaled coordinates shared by drawing and input. */
public final class BattleMenuLayout {
    private BattleMenuLayout() {}

    public static Rect[] main(int width,int height){
        double u=unit(width,height);double top=height-360*u;
        return new Rect[]{at(187,292,112,40,"ATTACK",1,top,u),at(80,339,82,17,"SWAP",2,top,u),
            at(112,244,93,29,"PERSONA",3,top,u),at(131,275,55,23,"ITEM",4,top,u),
            at(206,261,108,24,"GUARD",5,top,u),at(245,339,91,17,"ESCAPE",6,top,u),at(225,223,98,25,"SHIFT",0,top,u)};
    }
    public static double unit(int w,int h){return Math.min(w/640.,h/360.);}
    private static Rect at(int x,int y,int w,int h,String label,int key,double top,double u){return new Rect((int)(x*u),(int)(top+y*u),Math.max(1,(int)(w*u)),Math.max(1,(int)(h*u)),label,key);}

    public static int rowStep(int height) { return height < 240 ? 10 : 18; }
    public static Rect submenuPanel(int width, int height) { return submenuPanel(width, height, 7); }
    public static Rect submenuPanel(int width, int height, int rows) {
        int panelHeight = 23 + Math.min(7, Math.max(0, rows)) * rowStep(height) + 18;
        return new Rect(8, height - panelHeight - 8, Math.min(236, width - 16), panelHeight, "", 0);
    }
    public static Rect submenuRow(int width, int height, int rows, int index) {
        Rect p = submenuPanel(width, height, rows);
        return new Rect(p.x + 5, p.y + 23 + index * rowStep(height), p.w - 10, rowStep(height) - 1, "", index + 1);
    }
    public static Rect back(int width, int height, int rows) {
        Rect p = submenuPanel(width, height, rows);
        return new Rect(p.x + 5, p.y + p.h - 17, p.w - 10, 15, "BACK", 0);
    }
    public record Rect(int x, int y, int w, int h, String label, int hotkey) {
        public boolean contains(double px, double py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }
}
