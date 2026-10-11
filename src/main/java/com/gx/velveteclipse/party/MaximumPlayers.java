package com.gx.velveteclipse.party;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** World-owned mod capacity. Never changes the server's login limit. */
public final class MaximumPlayers extends SavedData {
 public static final int DEFAULT=4, MAXIMUM=64;
 private int limit=DEFAULT;
 public static int valid(int value){return Math.max(1,Math.min(MAXIMUM,value));}
 private static MaximumPlayers load(CompoundTag tag){var data=new MaximumPlayers();data.limit=tag.m_128441_("maximum_player")?valid(tag.m_128451_("maximum_player")):DEFAULT;return data;}
 private static MaximumPlayers data(MinecraftServer server){return server.m_129783_().m_8895_().m_164861_(MaximumPlayers::load,MaximumPlayers::new,"velvet_rules");}
 public static int get(MinecraftServer server){return data(server).limit;}
 public static void set(MinecraftServer server,int value){var data=data(server);data.limit=valid(value);data.m_77762_();var room=server.m_129880_(com.gx.velveteclipse.room.VelvetRoomService.DIMENSION);if(room!=null)com.gx.velveteclipse.room.VelvetRoomService.build(room);}
 @Override public CompoundTag m_7176_(CompoundTag tag){tag.m_128405_("maximum_player",limit);return tag;}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
  dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal("velvet").then(
   LiteralArgumentBuilder.<CommandSourceStack>literal("maximum_player").requires(s->s.m_6761_(2))
    .executes(c->{int value=get(c.getSource().m_81377_());c.getSource().m_288197_(() -> com.gx.velveteclipse.localization.ModText.component("Limite por grupo e combate: "+value+" (padrão 4)."),false);return value;})
    .then(RequiredArgumentBuilder.<CommandSourceStack,Integer>argument("quantidade",IntegerArgumentType.integer(1,MAXIMUM)).executes(c->{
     int value=IntegerArgumentType.getInteger(c,"quantidade");set(c.getSource().m_81377_(),value);
     c.getSource().m_288197_(() -> com.gx.velveteclipse.localization.ModText.component("Limite por grupo e combate: "+value+". Grupos e combates em andamento preservam seus integrantes."),true);return value;
    }))));
 }
}
