package com.gx.velveteclipse.darkhour;
import java.util.*;

/** Shared, testable host priority and finite, render-distance-bounded placement policy. */
public final class TartarusPlacement {
 public record Player(UUID id,boolean host,boolean configuredOwner,int permission){}
 public record Point(int x,int z){}
 public static UUID preferred(List<Player> players){return players.stream().min(Comparator.<Player>comparingInt(p->p.host()?0:p.configuredOwner()?1:p.permission()>=4?2:p.permission()>0?3:4).thenComparing(Comparator.comparingInt(Player::permission).reversed()).thenComparing(p->p.id().toString())).map(Player::id).orElse(null);}
 public static int range(int serverChunks,int clientChunks){return Math.max(2,Math.min(32,Math.min(serverChunks,clientChunks)))*16;}
 public static int distance(int range){return Math.max(16,Math.min(64,range-12));}
 public static Point candidate(int px,int pz,int range,int attempt){int distance=distance(range);int[][] offsets={{0,-1},{1,0},{-1,0},{0,1}};var direction=offsets[Math.floorMod(attempt,4)];return new Point(px+direction[0]*distance,pz+direction[1]*distance);}
 public static boolean visible(int px,int pz,int x,int z,int range){return Math.max(Math.abs(px-x),Math.abs(pz-z))<=range-8;}
}
