package com.gx.velveteclipse.roguelike;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Dedicated worlds have an explicit console-appointed owner, never first-join ownership. */
public final class RogueOwner extends SavedData {
 public String uuid="";
 public static RogueOwner get(MinecraftServer s){return s.m_129783_().m_8895_().m_164861_(RogueOwner::load,RogueOwner::new,"velvet_roguelike_owner");}
 private static RogueOwner load(CompoundTag t){var v=new RogueOwner();v.uuid=t.m_128461_("owner");return v;}
 @Override public CompoundTag m_7176_(CompoundTag t){t.m_128359_("owner",uuid);return t;}
}
