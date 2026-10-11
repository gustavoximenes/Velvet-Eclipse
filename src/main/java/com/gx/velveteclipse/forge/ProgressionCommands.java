package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Operator tools change persistent mod progression, never Minecraft's XP level. */
public final class ProgressionCommands {
 private static LiteralArgumentBuilder<CommandSourceStack> literal(String text){return LiteralArgumentBuilder.literal(text);}
 private static RequiredArgumentBuilder<CommandSourceStack,Integer> level(boolean persona){
  return RequiredArgumentBuilder.<CommandSourceStack,Integer>argument("nivel",IntegerArgumentType.integer(1,99))
   .executes(c->set(c,persona,c.getSource().m_81375_()))
   .then(RequiredArgumentBuilder.<CommandSourceStack,net.minecraft.commands.arguments.selector.EntitySelector>argument("jogador",EntityArgument.m_91466_())
    .executes(c->set(c,persona,EntityArgument.m_91474_(c,"jogador"))));
 }
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
  dispatcher.register(literal("velvet")
   .then(literal("player").then(literal("level").requires(s->s.m_6761_(2)).then(level(false))))
   .then(literal("persona").then(literal("level").requires(s->s.m_6761_(2))
    .then(RequiredArgumentBuilder.<CommandSourceStack,String>argument("id",StringArgumentType.word())
     .suggests((c,b)->{java.util.stream.Stream.concat(java.util.stream.Stream.of("active"),PersonaRegistry.personas().keySet().stream().sorted()).filter(id->id.startsWith(b.getRemaining())).forEach(b::suggest);return b.buildFuture();})
     .then(level(true))))));
 }
 private static int set(CommandContext<CommandSourceStack> c,boolean persona,ServerPlayer target)throws CommandSyntaxException{
  var source=c.getSource();
  if(BattleService.current(target)!=null||com.gx.velveteclipse.raid.RaidService.member(target)){
   source.m_81352_(com.gx.velveteclipse.localization.ModText.component("Defina os níveis fora de batalhas e antes de entrar na raid."));return 0;
  }
  int level=IntegerArgumentType.getInteger(c,"nivel");var data=PlayerPersonaData.load(target);String subject="Jogador";
  if(persona){String id=StringArgumentType.getString(c,"id");if(id.equals("active"))id=data.activePersona();
   if(!data.setPersonaLevel(id,level)){source.m_81352_(com.gx.velveteclipse.localization.ModText.component("O jogador não possui a Persona: "+id));return 0;}
   subject=PersonaRegistry.persona(id).orElseThrow().displayName();
  }else data.setPlayerLevel(level);
  data.save(target);String message=target.m_7755_().getString()+" • "+subject+" definido para Lv "+level+" • EXP 0";
  BattleService.sync(target,message);com.gx.velveteclipse.menu.MenuService.refresh(target,message);com.gx.velveteclipse.room.FusionRoomService.refresh(target,message);
  source.m_288197_(() -> com.gx.velveteclipse.localization.ModText.component(message),true);
  if(source.m_81373_()!=target)target.m_5661_(com.gx.velveteclipse.localization.ModText.component(message),false);
  return level;
 }
}
