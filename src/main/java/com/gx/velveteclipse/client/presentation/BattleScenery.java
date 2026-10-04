package com.gx.velveteclipse.client.presentation;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import com.gx.velveteclipse.client.ClientBattleState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Local visual cutaway only. Server blocks, lighting, collision and the minimap stay authoritative. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class BattleScenery {
 private static volatile List<CutawayVolume> volumes=List.of();
 private static Object level;private static long changed=Long.MIN_VALUE;private static Boolean previousSmartCull;
 public static boolean tartarus(){var w=Minecraft.m_91087_().f_91073_;return w!=null&&w.m_46472_().m_135782_().toString().equals("velvet_eclipse:tartarus");}
 public static List<CutawayVolume> snapshot(){return volumes;}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END&&(!tartarus()||!ClientBattleState.snapshot().active()||level!=Minecraft.m_91087_().f_91073_))clear();}
 public static void clear(){var old=volumes;volumes=List.of();var mc=Minecraft.m_91087_();if(mc!=null){if(level==mc.f_91073_)dirty(old);if(previousSmartCull!=null){mc.f_90980_=previousSmartCull;mc.f_91060_.m_109826_();}}previousSmartCull=null;level=null;changed=Long.MIN_VALUE;}
 public static void update(Vec3 eye,List<Vec3> subjects,double floor,long tick){
  if(!tartarus()||!ClientBattleState.snapshot().active()){clear();return;}
  var world=Minecraft.m_91087_().f_91073_;if(level!=world){clear();level=world;}
  // Collision-only walls still mark sections opaque in vanilla's visibility graph.
  // The local camera may now be behind them; use frustum culling until this battle ends.
  var mc=Minecraft.m_91087_();if(previousSmartCull==null){previousSmartCull=mc.f_90980_;mc.f_90980_=false;mc.f_91060_.m_109826_();}
  if(subjects.isEmpty()||changed!=Long.MIN_VALUE&&tick-changed<4)return;
  for(var old:volumes)if(old.eye().m_82554_(eye)<.65&&old.subjects().size()==subjects.size()){
   boolean same=true;for(int i=0;i<subjects.size();i++)if(old.subjects().get(i).m_82554_(subjects.get(i))>.8){same=false;break;}if(same)return;
  }
  var added=new CutawayVolume(eye,subjects,floor);var next=new ArrayList<>(volumes);next.add(added);
  // Retain openings through an action to prevent walls flickering back between shots.
  var dirty=new ArrayList<CutawayVolume>();dirty.add(added);if(next.size()>32)dirty.add(next.remove(0));
  volumes=List.copyOf(next);changed=tick;dirty(dirty);
 }
 private static void dirty(List<CutawayVolume> changed){
  var mc=Minecraft.m_91087_();if(mc.f_91073_==null)return;var sections=new HashSet<String>();
  for(var volume:changed){var b=volume.bounds();for(int x=(int)Math.floor(b[0]/16);x<=(int)Math.floor(b[3]/16);x++)for(int y=(int)Math.floor(b[1]/16);y<=(int)Math.floor(b[4]/16);y++)for(int z=(int)Math.floor(b[2]/16);z<=(int)Math.floor(b[5]/16);z++)if(sections.add(x+","+y+","+z))mc.f_91060_.m_109770_(x,y,z);}
 }
 private BattleScenery(){}
}
