package com.gx.velveteclipse.client;

import static com.gx.velveteclipse.client.HudDesign.*;

/** Compact command constellation inspired by Reload, driven only by the live snapshot. */
public final class BattleHudDesign {
    private static final int BLUE=0xFF1726FF,INK=0xE808103B,WHITE=0xFFF8FFFF,CYAN=0xFF66EFFF;
    public static void paint(Canvas c,View v,int w,int h,double mx,double my){
        double u=BattleMenuLayout.unit(w,h);int pad=Math.max(5,(int)(8*u));
        // There is exactly one real player in the current battle format.
        int pw=(int)(106*u),ph=(int)(73*u),px=w-pw-pad,py=h/2+(int)(12*u);
        ribbon(c,px,py,pw,ph,INK);
        int face=(int)(36*u);c.fill(px+pw-face-7,py-8,face+4,face+4,BLUE);
        c.portrait(px+pw-face-5,py-6,face);
        c.text(fit(c,c.playerName(),Math.max(16,pw-face-15)),px+5,py+5,CYAN);
        c.styled(Integer.toString(v.hp()),px+5,py+(int)(23*u),(float)(1.45*u),WHITE);
        c.text("HP",px+5,py+(int)(39*u),CYAN);
        c.fill(px+24,py+(int)(43*u),Math.max(1,pw-30),4,0xFF1B2943);
        c.fill(px+24,py+(int)(43*u),(int)(Math.max(1,pw-30)*v.hpRatio()),4,0xFFDDFF69);
        c.text(v.sp()+" SP",px+5,py+(int)(51*u),WHITE);
        c.fill(px+5,py+ph-8,(int)((pw-11)*v.spRatio()),3,CYAN);
        if(v.page().equals("TARGET")){c.fill(0,h-25,w,25,INK);c.text("Escolha o alvo   ENTER / Clique Confirmar   ESC Voltar",8,h-16,CYAN);return;}
        if(!v.page().equals("MAIN")){HudDesign.submenu(c,v,w,h,mx,my);return;}
        var rects=BattleMenuLayout.main(w,h);
        int cx=(int)(59*u),cy=h-(int)(66*u),radius=(int)(45*u);
        for(int y=-radius;y<=radius;y++){
            double edge=Math.sqrt(Math.max(0,radius*radius-y*y));int left=(int)(cx-edge);
            if(y<radius*.75)c.fill(left,cy+y,Math.max(2,(int)(7*u)),1,BLUE);
        }
        boolean available=v.playerTurn()&&!v.locked();
        int selected=available?0:-1;
        for(int i=0;i<rects.length;i++)if(rects[i].contains(mx,my)&&available)selected=i;
        for(int i=0;i<rects.length;i++){
            var r=rects[i];boolean chosen=i==selected,small=i==1||i==5;
            int ink=available?WHITE:0xFF809AB5;
            if(chosen){ribbon(c,r.x()-2,r.y()+1,r.w()+4,r.h(),0xFFFA164B);ribbon(c,r.x()+2,r.y()-1,r.w(),r.h(),BLUE);}
            else if(!small)ribbon(c,r.x(),r.y(),r.w(),r.h(),0x60020823);
            float scale=(float)((i==0?2.0:small?.72:1.42)*u);
            c.styled(r.label(),r.x()+(int)(5*u),r.y()+(int)(3*u),scale,ink);
            String key=fit(c,v.keys().get(i),(int)(15*u));
            c.text(key,r.x()+r.w()-Math.max(10,c.width(key)+3),r.y()+Math.max(1,(r.h()-8)/2),chosen?CYAN:0xFFB5F8FF);
        }
        String turn=v.locked()?"ACTION":available?"COMMAND":"AGUARDE";
        c.styled(turn,(int)(19*u),h-(int)(105*u),(float)(.9*u),CYAN);
        if(v.message()!=null&&!v.message().isBlank()){
            int x=(int)(202*u),y=h-(int)(33*u),width=w-x-pad;
            if(width>60){ribbon(c,x,y,width,(int)(24*u),INK);c.text(fit(c,v.message(),width-12),x+6,y+4,WHITE);}
        }
    }
    public static void ribbon(Canvas c,int x,int y,int w,int h,int color){
        for(int row=0;row<h;row++){int cut=(int)((1-row/(double)Math.max(1,h-1))*Math.min(8,h*.4));c.fill(x+cut,y+row,w-cut,1,color);}
    }
}
