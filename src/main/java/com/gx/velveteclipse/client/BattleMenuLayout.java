package com.gx.velveteclipse.client;

/** GUI-scaled coordinates shared by drawing and input. */
public final class BattleMenuLayout {
    private BattleMenuLayout() {}

    public static Rect[] main(int width, int height) {
        double u=unit(width,height);
        return new Rect[]{at(64,40,126,27,"ATTACK",1,height,u),at(24,11,80,9,"SWAP",2,height,u),
            at(33,91,124,25,"PERSONA",3,height,u),at(28,64,67,20,"ITEM",4,height,u),
            at(101,64,84,20,"GUARD",5,height,u),at(111,11,80,9,"ESCAPE",6,height,u)};
    }
    public static double unit(int w,int h){return Math.min(1.5,Math.min(w/427.0,h/240.0));}
    private static Rect at(int x,int bottom,int w,int h,String label,int key,int height,double u){return new Rect((int)(x*u),height-(int)(bottom*u),(int)(w*u),Math.max(3,(int)(h*u)),label,key);}

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
