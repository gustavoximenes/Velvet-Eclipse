package com.gx.velveteclipse.roguelike;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import java.util.function.BiConsumer;

public final class RogueRules {
 public static final String NAME="velvetRoguelike";
 public static final GameRules.Key<GameRules.BooleanValue> ENABLED=register();
 @SuppressWarnings("unchecked") private static GameRules.Key<GameRules.BooleanValue> register(){
  try{
   var create=ObfuscationReflectionHelper.findMethod(GameRules.BooleanValue.class,"m_46252_",boolean.class,BiConsumer.class);
   var add=ObfuscationReflectionHelper.findMethod(GameRules.class,"m_46189_",String.class,GameRules.Category.class,GameRules.Type.class);
   BiConsumer<MinecraftServer,GameRules.BooleanValue> changed=(server,value)->{if(server!=null)RogueService.broadcast(server);};
   return (GameRules.Key<GameRules.BooleanValue>)add.invoke(null,NAME,GameRules.Category.PLAYER,create.invoke(null,false,changed));
  }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot register "+NAME,e);}
 }
 public static boolean enabled(MinecraftServer s){return s.m_129900_().m_46207_(ENABLED);}
 public static void initialize(){}
 private RogueRules(){}
}
