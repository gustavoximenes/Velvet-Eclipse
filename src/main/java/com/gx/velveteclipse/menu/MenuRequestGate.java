package com.gx.velveteclipse.menu;
import java.util.*;

/** Separate read and action windows so refresh packets cannot swallow an equip action. */
public final class MenuRequestGate {
    private final Map<UUID,Long> actions=new HashMap<>(),refreshes=new HashMap<>();
    public boolean allow(UUID player,String action,long tick){
        boolean refresh="refresh".equals(action);var recent=refresh?refreshes:actions;
        Long last=recent.get(player);
        if(last!=null&&tick>=last&&tick-last<(refresh?20:4))return false;
        recent.put(player,tick);return true;
    }
    public void remove(UUID player){actions.remove(player);refreshes.remove(player);}
}
