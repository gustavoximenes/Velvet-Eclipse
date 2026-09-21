package com.gx.velveteclipse.darkhour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class TartarusCommands {
 @SubscribeEvent public static void commands(net.minecraftforge.event.RegisterCommandsEvent event){register(event.getDispatcher());}
 public static void register(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher){
  dispatcher.register(net.minecraft.commands.Commands.m_82127_("velvet").then(net.minecraft.commands.Commands.m_82127_("tartarus").then(net.minecraft.commands.Commands.m_82127_("shadows").requires(s->s.m_6761_(2)).then(net.minecraft.commands.Commands.m_82129_("minimo",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,64)).then(net.minecraft.commands.Commands.m_82129_("maximo",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,64)).executes(c->{int min=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"minimo"),max=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"maximo");if(min>max){c.getSource().m_81352_(net.minecraft.network.chat.Component.m_237113_("O mínimo não pode superar o máximo."));return 0;}var l=c.getSource().m_81377_().m_129880_(TartarusDungeonService.DIMENSION);if(l==null)return 0;var s=TartarusDungeonService.data(l);s.population.range(min,max);s.m_77762_();c.getSource().m_288197_(() -> net.minecraft.network.chat.Component.m_237113_("Próximos andares gerados: "+min+"–"+max+" Shadows, limitadas a uma por sala (máximo atual: 14); sem reposição de derrotadas."),true);return 1;}))))));
  for(String alias:new String[]{"tp","teleport"})dispatcher.register(net.minecraft.commands.Commands.m_82127_(alias).requires(s->s.m_6761_(2)).then(net.minecraft.commands.Commands.m_82127_("tartarus").then(net.minecraft.commands.Commands.m_82127_("floor").then(net.minecraft.commands.Commands.m_82129_("andar",com.mojang.brigadier.arguments.IntegerArgumentType.integer(0,250)).executes(c->TartarusDungeonService.teleportToFloor(c.getSource().m_81375_(),com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"andar")))))));
 }
}
