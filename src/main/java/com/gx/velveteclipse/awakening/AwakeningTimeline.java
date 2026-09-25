package com.gx.velveteclipse.awakening;

import java.util.List;

/** Shared authoritative timing, in seconds. The choice has no automatic default. */
public final class AwakeningTimeline {
    public static final List<String> STARTERS=List.of("orpheus","izanagi","arsene");
    public static final double CHOICE=16, RESUME=23, CONTRACT=33.2;
    public static double speech(String id){return switch(id){case "izanagi"->11;case "arsene"->30.1;default->18;};}
    public static double duration(String id){return Math.ceil((42+3.2+speech(id)-1.2)*12.5)/12.5;}
    public static double visualTime(double real,String id){
        if(real>=24.6&&real<27.8)return 24.6;
        double t=real>=27.8?real-3.2:real;
        if(t>=30){double length=speech(id)+1;if(t<30+length)return 30+(t-30)/length*2.2;t-=speech(id)-1.2;}
        return t;
    }
    public static final class Session {
        private final long started;
        private long chosenAt=-1;
        private String persona="";
        private boolean finished;
        public Session(long now){started=now;}
        public boolean choose(String id,long now){
            if(finished||chosenAt>=0||now-started<16_000||!STARTERS.contains(id))return false;
            persona=id;chosenAt=now;return true;
        }
        public boolean ready(long now){return !finished&&chosenAt>=0&&now-chosenAt>=Math.ceil((duration(persona)-RESUME)*1000);}
        public boolean finish(long now){if(!ready(now))return false;finished=true;return true;}
        public String persona(){return persona;}
        public boolean selected(){return chosenAt>=0;}
    }
}
