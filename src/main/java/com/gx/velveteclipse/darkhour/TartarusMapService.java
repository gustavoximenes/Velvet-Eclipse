package com.gx.velveteclipse.darkhour;
import net.minecraft.server.level.*;import net.minecraft.world.entity.Entity;import net.minecraftforge.event.TickEvent;import net.minecraftforge.event.entity.player.PlayerEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.common.Mod;import java.util.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class TartarusMapService {
 private static final String KEY="velvet_eclipse:explored21";
 private static final Set<UUID> visible=new HashSet<>();
 private static final Map<UUID,TartarusMapData> charts=new HashMap<>();
 private record Outline(int minX,int minZ,int width,int height,Set<TartarusInteriorLayout.Point> tiles){}
 private static final Map<TartarusInteriorLayout.Plan,Outline> outlines=new IdentityHashMap<>();
 private static Outline outline(TartarusInteriorLayout.Plan plan){
  var old=outlines.get(plan);if(old!=null)return old;if(outlines.size()>12)outlines.clear();
  var tiles=new HashSet<TartarusInteriorLayout.Point>();for(var c:plan.cells())if(c.y()==0)tiles.add(new TartarusInteriorLayout.Point(c.x(),c.z()));
  for(var c:plan.cells())if(c.y()==1&&(c.material().equals("wall")||c.material().equals("column")||c.material().equals("gold")))tiles.remove(new TartarusInteriorLayout.Point(c.x(),c.z()));
  int x=tiles.stream().mapToInt(TartarusInteriorLayout.Point::x).min().orElse(0),z=tiles.stream().mapToInt(TartarusInteriorLayout.Point::z).min().orElse(0),w=tiles.stream().mapToInt(TartarusInteriorLayout.Point::x).max().orElse(0)-x+1,h=tiles.stream().mapToInt(TartarusInteriorLayout.Point::z).max().orElse(0)-z+1;
  var result=new Outline(x,z,w,h,Set.copyOf(tiles));outlines.put(plan,result);return result;
 }
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
  if(event.phase!=TickEvent.Phase.END)return;var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null||server.m_129783_().m_46467_()%10!=0)return;
  for(var p:server.m_6846_().m_11314_()){
   int f=TartarusDungeonService.floor(p);var l=p.m_284548_();
   if(!TartarusDungeonService.inside(p)||!TartarusDungeonService.ready(TartarusDungeonService.data(l),f)){if(visible.remove(p.m_20148_()))TartarusMapNetwork.send(p,TartarusMapNetwork.View.empty());continue;}
   var state=TartarusDungeonService.data(l);var plan=state.plan(l,f);var shape=outline(plan);int ox=TartarusInteriorLayout.origin(f);
   var chart=charts.computeIfAbsent(p.m_20148_(),id->TartarusMapData.load(p.getPersistentData().m_128469_(KEY)));boolean dirty=chart.reset(state.progress.cycle);
   dirty|=chart.reveal(f,p.m_20185_()-ox,p.m_20189_());var markers=new ArrayList<TartarusMapNetwork.Marker>();
   for(var prop:TartarusInteriorLayout.props(state.progress.seed(l.m_7328_(),f),f,plan))if(prop.kind().equals("treasure")&&(!state.progress.treasures.contains(f+":"+prop.index())||(com.gx.velveteclipse.progression.RewardLedger.permitChest(f,prop.index())&&!com.gx.velveteclipse.progression.ProgressionRewards.claimed(p,f)))){
    if(chart.known(f,prop.at().x(),prop.at().z())&&p.m_20275_(ox+prop.at().x()+.5,65,prop.at().z()+.5)<196){var saved=state.props.get(f+":treasure:"+prop.index());Entity chest=null;try{if(saved!=null)chest=l.m_8791_(UUID.fromString(saved.m_128461_("entity")));}catch(IllegalArgumentException ignored){}if(chest!=null&&p.m_142582_(chest))dirty|=chart.seeChest(f,prop.index());}
    if(chart.chest(f,prop.index()))markers.add(new TartarusMapNetwork.Marker(2,prop.at().x()+.5f,prop.at().z()+.5f));
   }
   if(chart.known(f,plan.stairs().x(),plan.stairs().z()+4))markers.add(new TartarusMapNetwork.Marker(1,plan.stairs().x()+.5f,plan.stairs().z()+4.5f));
   for(var slot:state.slots.entrySet())if(slot.getKey().startsWith(f+":")&&!slot.getValue().m_128471_("defeated")&&slot.getValue().m_128461_("battle").isEmpty())try{var e=l.m_8791_(UUID.fromString(slot.getValue().m_128461_("entity")));if(e!=null&&e.m_6084_()&&chart.known(f,e.m_20185_()-ox,e.m_20189_()))markers.add(new TartarusMapNetwork.Marker(0,(float)(e.m_20185_()-ox),(float)e.m_20189_()));}catch(IllegalArgumentException ignored){}
   dirty|=com.gx.velveteclipse.party.PartyService.mergeMap(p,chart,state.progress.cycle);
   // Refresh online peers immediately; the party chart also persists for offline members.
   for(var peer:com.gx.velveteclipse.party.PartyService.members(p))if(peer!=p){var shared=charts.computeIfAbsent(peer.m_20148_(),id->TartarusMapData.load(peer.getPersistentData().m_128469_(KEY)));boolean peerDirty=shared.reset(state.progress.cycle);dirty|=chart.merge(shared);peerDirty|=shared.merge(chart);if(peerDirty)peer.getPersistentData().m_128365_(KEY,shared.save());}
   byte[] tiles=new byte[(shape.width*shape.height+7)/8];for(var at:shape.tiles)if(chart.known(f,at.x(),at.z())){int i=(at.z()-shape.minZ)*shape.width+at.x()-shape.minX;tiles[i/8]|=(byte)(1<<(i%8));}
   if(dirty)p.getPersistentData().m_128365_(KEY,chart.save());visible.add(p.m_20148_());TartarusMapNetwork.send(p,new TartarusMapNetwork.View(f,shape.minX,shape.minZ,shape.width,shape.height,tiles,List.copyOf(markers)));
  }
 }
 @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone e){e.getEntity().getPersistentData().m_128365_(KEY,e.getOriginal().getPersistentData().m_128469_(KEY).m_6426_());charts.remove(e.getEntity().m_20148_());}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){charts.remove(e.getEntity().m_20148_());visible.remove(e.getEntity().m_20148_());}
 @SubscribeEvent public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent e){charts.clear();outlines.clear();visible.clear();}
}
