package com.gx.velveteclipse.darkhour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class TartarusCommands {
 @SubscribeEvent public static void commands(net.minecraftforge.event.RegisterCommandsEvent event){register(event.getDispatcher());}
 public static void register(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher){
  for(String alias:new String[]{"tp","teleport"})dispatcher.register(net.minecraft.commands.Commands.m_82127_(alias).requires(s->s.m_6761_(2)).then(net.minecraft.commands.Commands.m_82127_("tartarus").then(net.minecraft.commands.Commands.m_82127_("floor").then(net.minecraft.commands.Commands.m_82129_("andar",com.mojang.brigadier.arguments.IntegerArgumentType.integer(0,250)).executes(c->TartarusDungeonService.teleportToFloor(c.getSource().m_81375_(),com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"andar")))))));
 }
}
