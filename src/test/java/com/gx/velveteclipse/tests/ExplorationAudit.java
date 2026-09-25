package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.exploration.*;
import com.gx.velveteclipse.client.*;
import java.util.*;
import static com.gx.velveteclipse.exploration.ExplorationRules.*;

public final class ExplorationAudit {
    private static int checks;
    private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        for(Mode m:Mode.values()){
            check(canToggle(m)==(m==Mode.NORMAL||m==Mode.EXPLORATION),"toggle guard "+m);
            check(canAttack(m)==(m==Mode.EXPLORATION),"attack guard "+m);
        }
        check(cameraMode(true,true,true,true,true)==Mode.SKILL_CINEMATIC,"skill priority");
        check(cameraMode(false,true,true,true,true)==Mode.BATTLE_ENTRY,"entry priority");
        check(cameraMode(false,false,true,true,true)==Mode.BATTLE,"battle priority");
        check(cameraMode(false,false,false,false,false)==Mode.NORMAL,"vanilla fallback");
        check(inSweep(0,0,2,0,1),"front shadow");check(!inSweep(0,0,-2,0,1),"behind rejected");
        check(!inSweep(4,0,0,0,1),"far rejected");check(!inSweep(0,3,2,0,1),"different floor rejected");
        check(!inSweep(2,0,0,0,1),"side rejected");
        for(int t=0;t<100;t++)check(hitWindow(t)==(t>=4&&t<=7),"hit window "+t);
        double prev=0;for(int t=0;t<=ENTRY_READY;t++){double next=entryProgress(t);check(next>=prev&&next>=0&&next<=1,"entry path monotonic");prev=next;}
        check(entryProgress(ENTRY_RUN_BEGIN)==0&&entryProgress(ENTRY_RUN_END)==1,"entry endpoints");check(ENTRY_READY<100&&ENTRY_READY>ENTRY_RUN_END,"session begins after movement");
        for(double hit=0;hit<5;hit+=.05){double f=collisionFraction(4,hit);check(f>=0&&f<=1,"camera clamp");check(f*4<=Math.max(0,hit-.18)+.0001,"camera wall padding");}
        var reservations=new EncounterReservation();UUID p=new UUID(1,1),p2=new UUID(2,2),s=new UUID(3,3),s2=new UUID(4,4);
        check(reservations.reserve(p,s),"reserve once");for(int n=0;n<100;n++){check(!reservations.reserve(p,s),"spam");check(!reservations.reserve(p2,s),"two players same shadow");check(!reservations.reserve(p,s2),"one player two shadows");}
        reservations.release(p);reservations.release(p);check(reservations.reserve(p2,s),"release on cancel");reservations.clear();check(!reservations.reserved(s),"server stop cleanup");
        for(int[] size:new int[][]{{854,480},{1280,720},{1920,1080},{1024,768},{2560,1080}})for(int scale=1;scale<=4;scale++){
            int w=size[0]/scale,h=size[1]/scale;var rects=BattleMenuLayout.main(w,h);
            for(int i=0;i<rects.length;i++){var a=rects[i];check(a.x()>=0&&a.y()>=0&&a.x()+a.w()<=w&&a.y()+a.h()<=h,"bounds");check(a.contains(a.x()+a.w()/2.,a.y()+a.h()/2.),"click center");
                for(int j=i+1;j<rects.length;j++){var b=rects[j];check(!(a.x()<b.x()+b.w()&&a.x()+a.w()>b.x()&&a.y()<b.y()+b.h()&&a.y()+a.h()>b.y()),"overlapping commands");}
            }
            for(int rows=1;rows<=7;rows++){var panel=BattleMenuLayout.submenuPanel(w,h,rows);check(panel.y()>=0,"submenu fits");for(int row=0;row<rows;row++){var r=BattleMenuLayout.submenuRow(w,h,rows,row);check(r.y()+r.h()<=panel.y()+panel.h(),"submenu click bounds");}}
        }
        String digest=java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(CombatScenario.run().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        // Captured from the published 0.6.28 JAR. The 0.5.30 fixture predates the approved combat updates.
        var golden=ExplorationAudit.class.getResourceAsStream("/combat-0.6.28.sha256");check(golden!=null,"published combat baseline exists");
        try(golden){check(digest.equals(new String(golden.readAllBytes()).trim()),"battle behavior differs from 0.6.28");}
        System.out.println("PASS "+checks+" checks; 20 HUD sizes/scales; 120 seeded battle regressions; digest "+digest);
    }
}
