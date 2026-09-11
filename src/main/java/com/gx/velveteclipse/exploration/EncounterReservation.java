package com.gx.velveteclipse.exploration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-thread only. An enemy and an owner can each belong to one preparation. */
public final class EncounterReservation {
    private final Map<UUID,UUID> owners=new HashMap<>();
    private final Map<UUID,UUID> shadows=new HashMap<>();
    public boolean reserve(UUID player,UUID shadow){
        if(owners.containsKey(player)||shadows.containsKey(shadow))return false;
        owners.put(player,shadow);shadows.put(shadow,player);return true;
    }
    public boolean reserved(UUID shadow){return shadows.containsKey(shadow);}
    public boolean owns(UUID player,UUID shadow){return shadow.equals(owners.get(player));}
    public void release(UUID player){UUID shadow=owners.remove(player);if(shadow!=null)shadows.remove(shadow,player);}
    public void clear(){owners.clear();shadows.clear();}
}
