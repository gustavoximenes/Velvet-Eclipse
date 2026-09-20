package com.gx.velveteclipse.darkhour;

import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.room.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class TartarusDungeonService {
 public static final ResourceKey<Level> DIMENSION=ResourceKey.m_135785_(Registries.f_256858_,new ResourceLocation("velvet_eclipse","tartarus"));
 private static final String RETURN="velvet_eclipse:tartarus_return",COOLDOWN="velvet_eclipse:tartarus_portal",SLOT="velvet_eclipse:tartarus_slot";
 public static boolean inside(Object p){return p instanceof Entity e&&e.m_9236_().m_46472_().equals(DIMENSION);}
 public static int floor(Object p){return inside(p)?TartarusInteriorLayout.floorAt(((Entity)p).m_20185_()):0;}
 public static boolean dungeonShadow(Object e){return e instanceof Entity entity&&entity.getPersistentData().m_128441_(SLOT);}
 private record Pending(int floor,boolean descending,ResourceKey<Level> from,double x,double y,double z){}
 private static final class Job {final TartarusInteriorLayout.Plan plan;int index;Job(long seed,int floor){plan=TartarusInteriorLayout.floor(seed,floor);}}
 public static final class State extends SavedData {
  public final Set<Integer> built=new HashSet<>();
  public final Map<String,CompoundTag> slots=new LinkedHashMap<>();
  final Map<Integer,Job> jobs=new LinkedHashMap<>();final Map<UUID,Pending> pending=new HashMap<>();
  final Map<Integer,TartarusInteriorLayout.Plan> plans=new LinkedHashMap<>(8,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<Integer,TartarusInteriorLayout.Plan> e){return size()>8;}};
  TartarusInteriorLayout.Plan plan(ServerLevel l,int f){return plans.computeIfAbsent(f,k->TartarusInteriorLayout.floor(l.m_7328_(),k));}
  public static State load(CompoundTag tag){var s=new State();for(int f:tag.m_128465_("built"))if(f>=0&&f<=250)s.built.add(f);var list=tag.m_128437_("slots",10);for(int i=0;i<list.size();i++){var t=list.m_128728_(i);s.slots.put(t.m_128461_("id"),t);}return s;}
  @Override public CompoundTag m_7176_(CompoundTag tag){tag.m_128385_("built",built.stream().mapToInt(Integer::intValue).toArray());var list=new ListTag();for(var e:slots.entrySet()){var t=e.getValue().m_6426_();t.m_128359_("id",e.getKey());list.add(t);}tag.m_128365_("slots",list);return tag;}
 }
 public static State data(ServerLevel l){return l.m_8895_().m_164861_(State::load,State::new,"velvet_eclipse_tartarus_interior");}
 public static void cross(ServerPlayer p,String route){
  if(BattleService.current(p)!=null||com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)||p.m_20159_())return;
  long now=System.currentTimeMillis();if(now-p.getPersistentData().m_128454_(COOLDOWN)<2000)return;
  int here=floor(p),target;
  if(route.equals("entrance")){
   if(inside(p)||!DarkHourService.active(p.m_9236_()))return;var tower=TartarusService.data(p.m_284548_());if(!tower.ready()||tower.removing)return;
   var back=new CompoundTag();back.m_128347_("x",tower.x+.5);back.m_128347_("z",tower.z+31.5);p.getPersistentData().m_128365_(RETURN,back);target=0;
  }else{
   if(!inside(p))return;
   if(route.equals("exit")){leave(p);return;}
   target=route.equals("summit")?0:route.equals("back")?Math.max(0,here-1):Math.min(250,here+1);
  }
  var l=p.m_20194_().m_129880_(DIMENSION);if(l==null){p.m_5661_(Component.m_237113_("Reinicie o mundo para carregar a dimensão do Tártaro."),false);return;}
  var s=data(l);if(s.pending.containsKey(p.m_20148_()))return;
  s.pending.put(p.m_20148_(),new Pending(target,target<here,p.m_9236_().m_46472_(),p.m_20185_(),p.m_20186_(),p.m_20189_()));
  if(!s.built.contains(target)){s.jobs.computeIfAbsent(target,f->new Job(l.m_7328_(),f));p.m_5661_(Component.m_237113_(target==0?"Tártaro • Abrindo o hall…":"Tártaro • Gerando andar "+target+"…"),true);}
 }
 private static void leave(ServerPlayer p){
  var level=p.m_20194_().m_129783_();var back=p.getPersistentData().m_128469_(RETURN);var spawn=level.m_220360_();double x=back.m_128441_("x")?back.m_128459_("x"):spawn.m_123341_()+.5,z=back.m_128441_("z")?back.m_128459_("z"):spawn.m_123343_()+.5;
  var tower=TartarusService.data(level);if(tower.ready()&&!tower.removing){x=tower.x+.5;z=tower.z+32.5;}
  int y=level.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)Math.floor(x),(int)Math.floor(z));
  p.getPersistentData().m_128356_(COOLDOWN,System.currentTimeMillis());p.m_8999_(level,x,y+.1,z,0,0);p.f_19789_=0;
 }
 private static BlockState state(String name){
  Block b=switch(name){case "trim"->DarkHourRegistry.TRIM.get();case "stairs"->DarkHourRegistry.STAIRS.get();default->DarkHourRegistry.INTERIOR.get(name).get();};
  var s=b.m_49966_();return b instanceof StairBlock?s.m_61124_(StairBlock.f_56841_,Direction.NORTH):s;
 }
 @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event){
  if(event.phase!=TickEvent.Phase.END||!(event.level instanceof ServerLevel l)||!l.m_46472_().equals(DIMENSION))return;
  var s=data(l);int budget=1500;
  var jobs=s.jobs.entrySet().iterator();while(jobs.hasNext()&&budget>0){var e=jobs.next();int f=e.getKey();var job=e.getValue();
   while(job.index<job.plan.cells().size()&&budget-->0){var c=job.plan.cells().get(job.index++);var pos=new BlockPos(TartarusInteriorLayout.origin(f)+c.x(),64+c.y(),c.z());
    if(c.material().equals("velvet_door"))VelvetRoomService.placeDoor(l,pos,Direction.EAST);else l.m_7731_(pos,state(c.material()),2);
   }
   if(job.index==job.plan.cells().size()){s.built.add(f);jobs.remove();s.m_77762_();}
  }
  var pending=s.pending.entrySet().iterator();while(pending.hasNext()){
   var entry=pending.next();var request=entry.getValue();var p=l.m_7654_().m_6846_().m_11259_(entry.getKey());
   if(p==null||!p.m_9236_().m_46472_().equals(request.from)||p.m_20275_(request.x,request.y,request.z)>36||BattleService.current(p)!=null){pending.remove();continue;}
   if(!s.built.contains(request.floor))continue;
   var plan=s.plan(l,request.floor);var at=request.descending&&request.floor>0?new TartarusInteriorLayout.Point(plan.stairs().x(),plan.stairs().z()+7):plan.entry();
   p.getPersistentData().m_128356_(COOLDOWN,System.currentTimeMillis());p.m_8999_(l,TartarusInteriorLayout.origin(request.floor)+at.x()+.5,65.05,at.z()+.5,180,0);p.f_19789_=0;
   p.m_5661_(Component.m_237113_(request.floor==0?"Tártaro • Hall de entrada":request.floor==250?"Tártaro • Andar 250 — Último andar":"Tártaro • Andar "+request.floor+" / 250"),false);pending.remove();
  }
  if(l.m_46467_()%100==0){var occupied=new HashSet<Integer>();for(var p:l.m_6907_())if(p.m_6084_()&&floor(p)>0)occupied.add(floor(p));for(int f:occupied)if(s.built.contains(f))spawn(l,s,f);}
 }
 private static void spawn(ServerLevel l,State s,int floor){
  var plan=s.plan(l,floor);if(ShadowRegistry.shadowType()==null)return;
  for(int i=0;i<7;i++){
   String id=floor+":"+i;var slot=s.slots.computeIfAbsent(id,k->new CompoundTag());String owner=slot.m_128461_("battle");
   if(!owner.isEmpty()){
    boolean active=false;try{active=BattleService.encounterActive(UUID.fromString(owner));}catch(IllegalArgumentException ignored){}
    if(active)continue;slot.m_128473_("battle");slot.m_128473_("entity");s.m_77762_();
   }
   if(!slot.m_128461_("entity").isEmpty())continue;
   var at=plan.spawns().get(i*plan.spawns().size()/7);double x=TartarusInteriorLayout.origin(floor)+at.x()+.5,z=at.z()+.5;
   if(l.m_6907_().stream().anyMatch(p->p.m_20275_(x,65,z)<64))continue;
   l.m_46745_(new BlockPos((int)Math.floor(x),65,(int)Math.floor(z)));
   Entity shadow=ShadowRegistry.shadowType().m_20615_(l);if(shadow==null)continue;
   shadow.m_6034_(x,65.05,z);shadow.m_20049_("velvet_encounter_shadow");shadow.getPersistentData().m_128359_(SLOT,id);
   if(shadow instanceof net.minecraft.world.entity.Mob mob)mob.m_21530_();
   slot.m_128359_("entity",shadow.m_20148_().toString());if(!l.m_7967_(shadow))slot.m_128473_("entity");s.m_77762_();
  }
 }
 public static void encounterStarted(Object player,Object shadow){
  if(!(player instanceof ServerPlayer p)||!(shadow instanceof Entity e)||!dungeonShadow(e))return;
  var s=data(p.m_284548_());var slot=s.slots.get(e.getPersistentData().m_128461_(SLOT));var battle=BattleService.current(p);if(slot!=null&&battle!=null){slot.m_128359_("battle",battle.id().toString());s.m_77762_();}
 }
 @SubscribeEvent public static void removed(EntityLeaveLevelEvent event){
  var e=event.getEntity();if(!(event.getLevel() instanceof ServerLevel l)||!inside(e)||!dungeonShadow(e))return;
  // Chunk unload is not a defeat: retain the persistent slot and never duplicate the mob.
  if(e.m_146911_()!=Entity.RemovalReason.KILLED&&e.m_146911_()!=Entity.RemovalReason.DISCARDED)return;
  var s=data(l);var slot=s.slots.get(e.getPersistentData().m_128461_(SLOT));if(slot!=null&&slot.m_128461_("battle").isEmpty()){slot.m_128473_("entity");s.m_77762_();}
 }
}
