package com.gx.velveteclipse.client;

import java.util.*;
import org.joml.Vector3f;

/** Bounded cosmetic bolt geometry, relative to the selected enemy's feet. */
public final class LightningStrike {
    public record Segment(Vector3f from,Vector3f to) {}
    private LightningStrike() {}
    public static List<Segment> bolts(long seed) {
        var random=new Random(seed);var result=new ArrayList<Segment>();
        for(int bolt=0;bolt<3;bolt++) {
            var previous=new Vector3f((bolt-1)*1.3f,6.2f,(bolt-1)*.4f);
            for(int i=1;i<=9;i++) {
                float height=6.2f*(1-i/9f);
                var next=i==9?new Vector3f(0,.15f,0):new Vector3f((random.nextFloat()-.5f)*.85f+previous.x*.3f,height,(random.nextFloat()-.5f)*.6f);
                result.add(new Segment(previous,next));
                if(i==3||i==5) {
                    var branch=new Vector3f(previous).add((bolt==0?-1:1)*.8f,.5f,.3f);
                    result.add(new Segment(branch,previous));
                }
                previous=next;
            }
        }
        return List.copyOf(result);
    }
    public static double opacity(double tick) {
        if(tick<82||tick>=94)return 0;
        if(tick<86)return .35+(tick-82)*.12;
        return Math.max(0,1-(tick-86)/8)*(.8+.2*Math.cos((tick-86)*3));
    }
}
