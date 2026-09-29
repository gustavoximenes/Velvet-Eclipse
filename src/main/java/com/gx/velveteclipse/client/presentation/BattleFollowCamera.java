package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Local opt-in view policy. It never changes camera entity, movement ownership or turns. */
public final class BattleFollowCamera {
 public static final KeyMapping TOGGLE=new KeyMapping("key.velvet_eclipse.follow_turn",KeyConflictContext.IN_GAME,InputConstants.Type.KEYSYM,92,"key.categories.velvet_eclipse");
 private static boolean follow;
 public static boolean enabled(){return follow;}
 public static void tick(){var mc=Minecraft.m_91087_();while(TOGGLE.m_90859_())if(mc.f_91080_==null&&ClientBattleState.snapshot().active()){follow=!follow;}}
 public static boolean direct(boolean own,boolean shared){return own||shared||follow;}
 public static void paint(HudDesign.Canvas c,int w,int h){
  if(!ClientBattleState.snapshot().active())return;var actor=BattleTargetHud.roster().stream().filter(t->t.turn()).findFirst().orElse(null);
  String who=actor==null?"":actor.name();while(who.length()>22)who=who.substring(0,who.length()-1);
  String text="["+ClientKeyMappings.label(TOGGLE)+"] "+(follow?"SEGUIR TURNO: ":"CÂMERA PRÓPRIA • TURNO: ")+who;
  c.text(text,Math.max(8,(w-c.width(text))/2),h-13,0xFF9AEFFF);
 }
 @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
 public static final class Registration {@SubscribeEvent public static void register(RegisterKeyMappingsEvent e){e.register(TOGGLE);}}
 private BattleFollowCamera(){}
}
