package com.gx.velveteclipse.client.vfx;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.presentation.*;
import com.gx.velveteclipse.core.persona.*;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class ReloadFeedback {
 private static final FeedbackSession gate=new FeedbackSession();private static Object world;private static long lastWorld=Long.MIN_VALUE;
 public static void accept(BattleFeedbackS2C p){var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;if(world!=mc.f_91073_){clear();world=mc.f_91073_;}
  if(p.session().equals(BattleFeedbackS2C.WORLD)){
   if(!p.kind().equals("world")||p.event()<=lastWorld||p.targets().isEmpty())return;lastWorld=p.event();var t=p.targets().get(0).point();ReloadAudio.clip(p.key(),false,t.x(),t.y(),t.z());return;
  }
  if(p.kind().equals("start")){if(!p.session().equals(gate.id())){ReloadPhoton.reset();ReloadAudio.reset(true);}gate.start(p.session());com.gx.velveteclipse.client.exploration.EntryTransition.membership(p.session());return;}
  if(!gate.accept(p.session(),p.event()))return;
  if(p.kind().equals("stop")){com.gx.velveteclipse.client.exploration.EntryTransition.reset();gate.clear();ReloadPhoton.reset();ReloadAudio.reset(true);return;}
  if(p.kind().equals("cancel")){ReloadPhoton.reset();ReloadAudio.reset(true);return;}
  var at=p.targets().isEmpty()?null:p.targets().get(0).point();double x=at==null?0:at.x(),y=at==null?0:at.y(),z=at==null?0:at.z();
  switch(p.kind()){
   case "voice"->ReloadAudio.route("voice_for_"+p.key(),true,x,y,z);
   case "audio"->{if(p.key().startsWith("prepare_")){var clips=FeedbackLibrary.audio("physical_"+p.key().substring(8));if(!clips.isEmpty())ReloadAudio.clip(clips.get(0),true,x,y,z);}else if(p.key().startsWith("swing_")){String family=p.key().substring(6);var clips=FeedbackLibrary.audio("physical_"+family);if(!clips.isEmpty())ReloadAudio.clip(clips.get(family.equals("bow")||family.equals("gun")?1:0),true,x,y,z);}else ReloadAudio.route(p.key(),true,x,y,z);}
   case "skill","item","melee"->{
    var direct=p.targets().stream().filter(t->!Set.of("miss","reflect","absorb","null").contains(t.outcome())).map(BattleFeedbackS2C.Target::point).toList();
    if(p.kind().equals("skill")){String audio=p.key();if(FeedbackLibrary.audio("skill_"+audio).isEmpty())audio=ReloadPhoton.family(SpellVisuals.family(audio));ReloadAudio.route("skill_"+audio,true,x,y,z);ReloadPhoton.batch(p.key(),direct);}
    if(p.kind().equals("item")){
     ReloadAudio.route("item_"+p.key(),true,x,y,z);var item=com.gx.velveteclipse.equipment.DamageItems.get(p.key());
     if(item!=null)for(var t:direct)PhotonBattleEffects.impact(item.element(),1,t.x(),t.y(),t.z(),t.seed());
     else {var supply=com.gx.velveteclipse.equipment.Supplies.get(p.key());if(supply!=null&&!supply.cures().isEmpty())ReloadPhoton.batch("dekunda",direct);else if(p.key().equals("pulse_tonic")||supply!=null&&supply.hp()!=0)ReloadPhoton.batch("dia",direct);}
    }
    if(p.kind().equals("melee")&&!direct.isEmpty()){
     var clips=FeedbackLibrary.audio("physical_"+p.key());if(!clips.isEmpty())ReloadAudio.clip(clips.get(clips.size()-1),true,x,y,z);
     var family=switch(p.key()){case "bow","gun","pierce"->Element.PIERCE;case "strike","tap"->Element.PHYSICAL;default->Element.SLASH;};
     for(var t:direct)PhotonBattleEffects.impact(family,p.key().equals("tap")?.6f:1,t.x(),t.y(),t.z(),t.seed());
    }
    var played=new HashSet<String>();for(var t:p.targets())if(Set.of("reflect","absorb","null").contains(t.outcome())){ReloadPhoton.outcome(t.outcome(),t.point());if(played.add(t.outcome()))ReloadAudio.route("battle_"+t.outcome(),true,t.point().x(),t.point().y(),t.point().z());}
    if(p.targets().stream().anyMatch(t->t.outcome().equals("critical")))ReloadAudio.route("battle_critical",true,x,y,z);
   }
  }
 }
 public static void clear(){com.gx.velveteclipse.client.exploration.EntryTransition.reset();gate.clear();ReloadPhoton.reset();ReloadAudio.reset(false);world=null;lastWorld=Long.MIN_VALUE;}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase!=TickEvent.Phase.END)return;if(world!=null&&Minecraft.m_91087_().f_91073_!=world){clear();return;}ReloadPhoton.tick();ReloadAudio.tick();}
}
