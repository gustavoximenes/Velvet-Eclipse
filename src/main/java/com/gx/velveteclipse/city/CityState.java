package com.gx.velveteclipse.city;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
/** One immutable choice per world. A pending generation can resume after a restart. */
public final class CityState extends SavedData {
 public int choice; public boolean initialized,ready; public int cursor; public String owner="";
 public int revision,cleanupCursor,conflicts,targetRevision; public boolean rebuilding;
 public static CityState get(MinecraftServer s){return s.m_129783_().m_8895_().m_164861_(CityState::load,CityState::new,"velvet_city");}
 private static CityState load(CompoundTag t){var s=new CityState();s.choice=t.m_128451_("choice");s.initialized=t.m_128471_("initialized");s.ready=t.m_128471_("ready");s.cursor=t.m_128451_("cursor");s.owner=t.m_128461_("owner");s.revision=t.m_128451_("revision");s.cleanupCursor=t.m_128451_("cleanupCursor");s.conflicts=t.m_128451_("conflicts");s.rebuilding=t.m_128471_("rebuilding");s.targetRevision=t.m_128451_("targetRevision");return s;}
 @Override public CompoundTag m_7176_(CompoundTag t){t.m_128405_("choice",choice);t.m_128379_("initialized",initialized);t.m_128379_("ready",ready);t.m_128405_("cursor",cursor);t.m_128359_("owner",owner);t.m_128405_("revision",revision);t.m_128405_("cleanupCursor",cleanupCursor);t.m_128405_("conflicts",conflicts);t.m_128379_("rebuilding",rebuilding);t.m_128405_("targetRevision",targetRevision);return t;}
}
