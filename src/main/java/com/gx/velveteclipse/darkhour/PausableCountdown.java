package com.gx.velveteclipse.darkhour;

/** Interpolates server samples, excluding intervals where the local world is paused. */
public final class PausableCountdown {
    private long remainingNanos,last;
    private boolean initialized,wasPaused;
    public void synchronize(int milliseconds,long now,boolean paused){remainingNanos=Math.max(0,milliseconds)*1_000_000L;last=now;initialized=true;wasPaused=paused;}
    public int remaining(long now,boolean paused){
        if(!initialized)return 0;
        if(!wasPaused&&!paused)remainingNanos=Math.max(0,remainingNanos-Math.max(0,now-last));
        last=now;wasPaused=paused;
        return (int)Math.min(Integer.MAX_VALUE,(remainingNanos+999_999)/1_000_000);
    }
}
