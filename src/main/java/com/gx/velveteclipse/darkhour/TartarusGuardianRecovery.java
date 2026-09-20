package com.gx.velveteclipse.darkhour;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;
/** Reconcile the saved slot only after its stationary guardian chunk finishes entity loading. */
public final class TartarusGuardianRecovery {
 public enum Action { DEFEATED, WAIT, PRESENT, SPAWN }
 public static Action reconcile(CompoundTag slot,boolean defeated,boolean battleActive,boolean entitiesLoaded,UUID live){
  if(defeated)return Action.DEFEATED;
  if(battleActive||!entitiesLoaded)return Action.WAIT;
  slot.m_128473_("battle");
  if(live!=null){slot.m_128359_("entity",live.toString());return Action.PRESENT;}
  slot.m_128473_("entity");return Action.SPAWN;
 }
}
