package com.gx.velveteclipse.darkhour;
import net.minecraft.nbt.CompoundTag;
import java.util.UUID;
/** Slot ownership outlives a fight; escape never spends a finite encounter. */
public final class TartarusEncounterSlot {
 public static void begin(CompoundTag slot,UUID battle){slot.m_128359_("battle",battle.toString());slot.m_128379_("retained",true);}
 public static boolean release(CompoundTag slot,UUID battle){if(!battle.toString().equals(slot.m_128461_("battle")))return false;slot.m_128473_("battle");slot.m_128473_("retained");return true;}
 public static void recover(CompoundTag slot){slot.m_128473_("battle");if(!slot.m_128471_("retained"))slot.m_128473_("entity");slot.m_128473_("retained");}
}
