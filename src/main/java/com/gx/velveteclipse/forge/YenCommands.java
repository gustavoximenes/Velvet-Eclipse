package com.gx.velveteclipse.forge;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
public final class YenCommands {
 public static void register(CommandDispatcher<CommandSourceStack> d){d.register(LiteralArgumentBuilder.<CommandSourceStack>literal("velvet").then(LiteralArgumentBuilder.<CommandSourceStack>literal("yen").requires(s->s.m_6761_(2)).then(LiteralArgumentBuilder.<CommandSourceStack>literal("add").then(RequiredArgumentBuilder.<CommandSourceStack,Long>argument("quantidade",LongArgumentType.longArg(1,9_999_999_999L)).executes(c->add(c.getSource(),c.getSource().m_81375_(),LongArgumentType.getLong(c,"quantidade"))).then(RequiredArgumentBuilder.<CommandSourceStack,net.minecraft.commands.arguments.selector.EntitySelector>argument("jogador",EntityArgument.m_91466_()).executes(c->add(c.getSource(),EntityArgument.m_91474_(c,"jogador"),LongArgumentType.getLong(c,"quantidade"))))))));}
 private static int add(CommandSourceStack source,ServerPlayer p,long amount){var d=PlayerPersonaData.load(p);long before=d.yen();d.addYen(amount);d.save(p);String message="Adicionado ¥ "+(d.yen()-before)+" a "+p.m_7755_().getString()+". Saldo: ¥ "+d.yen();BattleService.sync(p,message);source.m_288197_(() -> Component.m_237113_(message),true);return 1;}
}
