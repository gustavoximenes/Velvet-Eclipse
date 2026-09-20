package com.gx.velveteclipse.core.battle;
import java.util.*;
/** Exactly one reward grant and one independent release per battle member. */
public final class BattleResultsGate {
 private boolean started;
 private final Set<UUID> pending=new HashSet<>();
 public boolean begin(Collection<UUID> members){if(started)return false;started=true;pending.addAll(members);return true;}
 public boolean started(){return started;}
 public boolean release(UUID member){return started&&pending.remove(member);}
 public boolean waiting(UUID member){return pending.contains(member);}
}
