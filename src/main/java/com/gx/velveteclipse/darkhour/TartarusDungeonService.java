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
 private static final TicketType<Integer> GUARDIAN_TICKET=TicketType.m_9465_("velvet_guardian",Comparator.<Integer>naturalOrder(),200);
 private static final String RETURN="velvet_eclipse:tartarus_return",COOLDOWN="velvet_eclipse:tartarus_portal",SLOT="velvet_eclipse:tartarus_slot";
 public static boolean inside(Object p){return p instanceof Entity e&&e.m_9236_().m_46472_().equals(DIMENSION);}
 public static int floor(Object p){return inside(p)?TartarusInteriorLayout.floorAt(((Entity)p).m_20185_()):0;}
 public static boolean dungeonShadow(Object e){return e instanceof Entity entity&&entity.getPersistentData().m_128441_(SLOT);}
 private record Pending(int floor,boolean descending,ResourceKey<Level> from,double x,double y,double z){}
 private static final class Job {
  final TartarusInteriorLayout.Plan plan;final int floor;int index,clear;final int clearCount;
  Job(long seed,int floor){this.floor=floor;plan=TartarusInteriorLayout.floor(seed,floor);clearCount=floor==0?61*65*35:97*97*14+47*98*19;}
  BlockPos clearPos(){int n=clear++,x,y,z;if(floor==0){x=n%61-30;n/=61;z=n%65-32;y=n/65;}else if(n<97*97*14){x=n%97-48;n/=97;z=n%97-48;y=n/97;}else{n-=97*97*14;x=n%47-23;n/=47;z=n%98+207;y=n/98;}return new BlockPos(TartarusInteriorLayout.origin(floor)+x,64+y,z);}
 }
 public static final class State extends SavedData {
  public final Set<Integer> built=new HashSet<>();
  public final Set<Integer> repaired=new HashSet<>();
  public final Set<Integer> intervals=new HashSet<>(),defeated=new HashSet<>(),transitions19=new HashSet<>();
  public final Map<String,CompoundTag> slots=new LinkedHashMap<>();
  public TartarusProgress progress=new TartarusProgress();public final Map<String,CompoundTag> props=new LinkedHashMap<>();
  final Map<Integer,Job> jobs=new LinkedHashMap<>();final Map<UUID,Pending> pending=new HashMap<>();
  final Map<Integer,TartarusInteriorLayout.Plan> plans=new LinkedHashMap<>(8,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<Integer,TartarusInteriorLayout.Plan> e){return size()>8;}};
  TartarusInteriorLayout.Plan plan(ServerLevel l,int f){return plans.computeIfAbsent(f,k->TartarusInteriorLayout.floor(progress.seed(l.m_7328_(),k),k));}
  public static State load(CompoundTag tag){var s=new State();s.progress=TartarusProgress.load(tag.m_128469_("progress18"));for(int f:tag.m_128465_("transitions19"))s.transitions19.add(f);var propList=tag.m_128437_("props18",10);for(int i=0;i<propList.size();i++){var t=propList.m_128728_(i);s.props.put(t.m_128461_("id"),t);}for(int f:tag.m_128465_("built"))if(f>=0&&f<=250)s.built.add(f);for(int f:tag.m_128465_("backPortalV2"))s.repaired.add(f);for(int f:tag.m_128465_("intervalsV1"))s.intervals.add(f);for(int f:tag.m_128465_("guardiansDefeated"))s.defeated.add(f);var list=tag.m_128437_("slots",10);for(int i=0;i<list.size();i++){var t=list.m_128728_(i);s.slots.put(t.m_128461_("id"),t);}return s;}
  @Override public CompoundTag m_7176_(CompoundTag tag){tag.m_128385_("transitions19",transitions19.stream().mapToInt(Integer::intValue).toArray());tag.m_128365_("progress18",progress.save());var ps=new ListTag();props.forEach((id,v)->{var t=v.m_6426_();t.m_128359_("id",id);ps.add(t);});tag.m_128365_("props18",ps);tag.m_128385_("built",built.stream().mapToInt(Integer::intValue).toArray());tag.m_128385_("backPortalV2",repaired.stream().mapToInt(Integer::intValue).toArray());tag.m_128385_("intervalsV1",intervals.stream().mapToInt(Integer::intValue).toArray());tag.m_128385_("guardiansDefeated",defeated.stream().mapToInt(Integer::intValue).toArray());var list=new ListTag();for(var e:slots.entrySet()){var t=e.getValue().m_6426_();t.m_128359_("id",e.getKey());list.add(t);}tag.m_128365_("slots",list);return tag;}
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
   if((route.equals("forward")||route.equals("summit"))&&TartarusInteriorLayout.bossFloor(here)&&!data(p.m_284548_()).defeated.contains(here)){p.m_5661_(Component.m_237113_("Derrote o Guardião para liberar a passagem."),true);return;}
   target=route.equals("summit")?0:route.equals("back")?Math.max(0,here-1):Math.min(250,here+1);
  }
  var l=p.m_20194_().m_129880_(DIMENSION);if(l==null){p.m_5661_(Component.m_237113_("Reinicie o mundo para carregar a dimensão do Tártaro."),false);return;}
  var s=data(l);prepareFloor(l,s,target);if(s.pending.containsKey(p.m_20148_()))return;
  s.pending.put(p.m_20148_(),new Pending(target,target<here,p.m_9236_().m_46472_(),p.m_20185_(),p.m_20186_(),p.m_20189_()));
  if(!ready(s,target)){p.m_5661_(Component.m_237113_(target==0?"Tártaro • Abrindo o hall…":"Tártaro • Gerando andar "+target+"…"),true);}
 }
 private static void leave(ServerPlayer p){
  var level=p.m_20194_().m_129783_();var back=p.getPersistentData().m_128469_(RETURN);var spawn=level.m_220360_();double x=back.m_128441_("x")?back.m_128459_("x"):spawn.m_123341_()+.5,z=back.m_128441_("z")?back.m_128459_("z"):spawn.m_123343_()+.5;
  var tower=TartarusService.data(level);if(tower.ready()&&!tower.removing){x=tower.x+.5;z=tower.z+32.5;}
  int y=level.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)Math.floor(x),(int)Math.floor(z));
  p.getPersistentData().m_128356_(COOLDOWN,System.currentTimeMillis());p.m_8999_(level,x,y+.1,z,0,0);p.f_19789_=0;
 }
 private static BlockState state(String name){
  Block b=switch(name){case "barrier"->Blocks.f_50375_;case "boss_gate"->DarkHourRegistry.INTERIOR.get("window").get();case "trim"->DarkHourRegistry.TRIM.get();case "stairs"->DarkHourRegistry.STAIRS.get();default->DarkHourRegistry.INTERIOR.get(name).get();};
  var s=b.m_49966_();return b instanceof StairBlock?s.m_61124_(StairBlock.f_56841_,Direction.NORTH):s;
 }
 @SubscribeEvent public static void tick(TickEvent.LevelTickEvent event){
  if(event.phase!=TickEvent.Phase.END||!(event.level instanceof ServerLevel l)||!l.m_46472_().equals(DIMENSION))return;
  var s=data(l);
  for(var p:List.copyOf(l.m_6907_()))if(p.getPersistentData().m_128471_("velvet_eclipse:tartarus_dawn")&&BattleService.current(p)==null){p.getPersistentData().m_128473_("velvet_eclipse:tartarus_dawn");leave(p);}
  // Regeneration is deferred while an encounter owns a floor. Move observers out first.
  for(int f:l.m_6907_().stream().map(TartarusDungeonService::floor).distinct().toList())if(!ready(s,f)&&!s.jobs.containsKey(f))prepareFloor(l,s,f);
  for(int f:s.pending.values().stream().map(Pending::floor).distinct().toList())prepareFloor(l,s,f);
  int budget=2500,clearBudget=12000;
  var jobs=s.jobs.entrySet().iterator();while(jobs.hasNext()&&budget>0){var e=jobs.next();int f=e.getKey();var job=e.getValue();
   while(job.clear<job.clearCount&&clearBudget-->0){var at=job.clearPos();if(!l.m_8055_(at).m_60795_())l.m_7731_(at,Blocks.f_50016_.m_49966_(),2);}
   if(job.clear<job.clearCount)continue;
   while(job.index<job.plan.cells().size()&&budget-->0){var c=job.plan.cells().get(job.index++);var pos=new BlockPos(TartarusInteriorLayout.origin(f)+c.x(),64+c.y(),c.z());
    if(c.material().equals("velvet_door"))VelvetRoomService.placeDoor(l,pos,Direction.EAST);else l.m_7731_(pos,state(c.material()),2);
   }
   if(job.index==job.plan.cells().size()){s.built.add(f);if(TartarusInteriorLayout.transitionFloor(f))s.transitions19.add(f);s.progress.generated.put(f,s.progress.generation(f));s.repaired.add(f);if(TartarusInteriorLayout.bossFloor(f))s.intervals.add(f);jobs.remove();s.m_77762_();}
  }
  var pending=s.pending.entrySet().iterator();while(pending.hasNext()){
   var entry=pending.next();var request=entry.getValue();var p=l.m_7654_().m_6846_().m_11259_(entry.getKey());
   if(p==null||!p.m_9236_().m_46472_().equals(request.from)||p.m_20275_(request.x,request.y,request.z)>36||BattleService.current(p)!=null){pending.remove();continue;}
   if(!ready(s,request.floor))continue;
   repair(l,s,request.floor);if(s.defeated.contains(request.floor))openGate(l,request.floor);
   var plan=s.plan(l,request.floor);var at=request.descending&&request.floor>0?new TartarusInteriorLayout.Point(plan.stairs().x(),plan.stairs().z()+7):plan.entry();
   p.getPersistentData().m_128356_(COOLDOWN,System.currentTimeMillis());p.m_8999_(l,TartarusInteriorLayout.origin(request.floor)+at.x()+.5,65.05,at.z()+.5,180,0);p.f_19789_=0;TartarusTerminals.discover(p,request.floor);TartarusTerminals.ensureProps(l,s,request.floor);if(request.floor>0)spawn(l,s,request.floor);
   p.m_5661_(Component.m_237113_(request.floor==0?"Tártaro • Hall de entrada":request.floor==250?"Tártaro • Andar 250 — Último andar":"Tártaro • Andar "+request.floor+" / 250"),false);pending.remove();
  }
  if(l.m_46467_()%100==0){var occupied=new HashSet<Integer>();for(var p:l.m_6907_())if(p.m_6084_())occupied.add(floor(p));for(int f:occupied)if(ready(s,f)){TartarusTerminals.discoverFloor(l,f);TartarusTerminals.ensureProps(l,s,f);if(f>0)spawn(l,s,f);}}
 }

 private static void repair(ServerLevel l,State s,int floor){
  if(floor==0||TartarusInteriorLayout.bossFloor(floor)||s.repaired.contains(floor))return;int ox=TartarusInteriorLayout.origin(floor);
  for(int x=-38;x<=-34;x++)for(int y=1;y<=4;y++){var p=new BlockPos(ox+x,64+y,-29);if(l.m_8055_(p).m_60713_(DarkHourRegistry.INTERIOR.get("back").get()))l.m_7731_(p,Blocks.f_50016_.m_49966_(),2);}
  var plan=s.plan(l,floor);int z=plan.cells().stream().filter(c->c.material().equals("back")).findFirst().orElseThrow().z();
  for(var c:plan.cells())if(c.x()>=-39&&c.x()<=-33&&c.z()>=z-1&&c.z()<=z&&c.y()<=5)l.m_7731_(new BlockPos(ox+c.x(),64+c.y(),c.z()),state(c.material()),2);
  s.repaired.add(floor);s.m_77762_();
 }
 private static void spawn(ServerLevel l,State s,int floor){
  var plan=s.plan(l,floor);if(ShadowRegistry.shadowType()==null||TartarusInteriorLayout.shadowLimit(floor)==0)return;
  boolean boss=TartarusInteriorLayout.bossFloor(floor);if(boss&&s.defeated.contains(floor)){openGate(l,floor);return;}int limit=TartarusInteriorLayout.shadowLimit(floor);
  for(int i=0;i<limit;i++){
   String id=floor+":"+i;var slot=s.slots.computeIfAbsent(id,k->new CompoundTag());String owner=slot.m_128461_("battle");
   if(!owner.isEmpty()){
    boolean active=false;try{active=BattleService.encounterActive(UUID.fromString(owner));}catch(IllegalArgumentException ignored){}
    if(active)continue;slot.m_128473_("battle");slot.m_128473_("entity");s.m_77762_();
   }
   if(boss){
    var center=new BlockPos(TartarusInteriorLayout.origin(floor),65,TartarusInteriorLayout.BOSS_Z);var chunk=new ChunkPos(center);
    // Terrain loading alone does not finish the asynchronous entity read from disk.
    // A short, refreshed ticket keeps this stationary room loaded only while occupied.
    l.m_7726_().m_8387_(GUARDIAN_TICKET,chunk,2,floor);l.m_46745_(center);
    boolean loaded=l.m_143319_(chunk.m_45588_());UUID live=null;
    if(loaded)for(var candidate:l.m_8583_())if(candidate.m_6084_()&&guardian(candidate)&&id.equals(candidate.getPersistentData().m_128461_(SLOT))){live=candidate.m_20148_();break;}
    var action=TartarusGuardianRecovery.reconcile(slot,false,false,loaded,live);
    if(action==TartarusGuardianRecovery.Action.WAIT)continue;
    s.m_77762_();if(action==TartarusGuardianRecovery.Action.PRESENT)continue;
   }else if(!slot.m_128461_("entity").isEmpty())continue;
   var at=plan.spawns().get(i*plan.spawns().size()/limit);double x=TartarusInteriorLayout.origin(floor)+at.x()+.5,z=at.z()+.5;
   if(!boss&&l.m_6907_().stream().anyMatch(p->p.m_20275_(x,65,z)<64))continue;
   l.m_46745_(new BlockPos((int)Math.floor(x),65,(int)Math.floor(z)));
   Entity shadow=ShadowRegistry.shadowType().m_20615_(l);if(shadow==null)continue;
   shadow.m_6034_(x,65.05,z);shadow.m_20049_("velvet_encounter_shadow");shadow.getPersistentData().m_128359_(SLOT,id);shadow.getPersistentData().m_128356_("tartarus_generation",s.progress.generation(floor));if(boss)markGuardian(shadow);
   if(shadow instanceof net.minecraft.world.entity.Mob mob){mob.m_21530_();if(boss)mob.m_21557_(true);}
   slot.m_128359_("entity",shadow.m_20148_().toString());if(!l.m_7967_(shadow))slot.m_128473_("entity");s.m_77762_();
  }
 }
 public static void encounterStarted(Object player,Object shadow){
  if(!(player instanceof ServerPlayer p)||!(shadow instanceof Entity e)||!dungeonShadow(e))return;
  var s=data(p.m_284548_());var slot=s.slots.get(e.getPersistentData().m_128461_(SLOT));var battle=BattleService.current(p);if(slot!=null&&battle!=null){slot.m_128359_("battle",battle.id().toString());s.m_77762_();}
 }
 public static boolean guardian(Object e){return e instanceof Entity entity&&entity.getPersistentData().m_128471_("velvet_guardian");}
 public static void markGuardian(Entity e){e.getPersistentData().m_128379_("velvet_guardian",true);e.m_6593_(Component.m_237113_("Guardião do Tártaro"));e.m_20331_(true);}
 @SubscribeEvent public static void holdGuardian(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event){
  var e=event.getEntity();if(e.m_9236_().f_46443_||!guardian(e)||!dungeonShadow(e))return;
  int f=floor(e);e.m_20256_(net.minecraft.world.phys.Vec3.f_82478_);e.m_6034_(TartarusInteriorLayout.origin(f)+.5,65.05,TartarusInteriorLayout.BOSS_Z+.5);e.m_146922_(0);e.m_5616_(0);e.m_5618_(0);
 }
 private static void openGate(ServerLevel l,int floor){if(!TartarusInteriorLayout.bossFloor(floor))return;for(int x=-2;x<=2;x++)for(int y=1;y<=12;y++){var p=new BlockPos(TartarusInteriorLayout.origin(floor)+x,64+y,TartarusInteriorLayout.GATE_Z);if(!l.m_8055_(p).m_60795_())l.m_7731_(p,Blocks.f_50016_.m_49966_(),2);}}
 public static void encounterFinished(UUID battle,boolean won){
  if(!won)return;var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;var l=server.m_129880_(DIMENSION);if(l==null)return;var s=data(l);
  for(var slot:s.slots.entrySet())if(slot.getValue().m_128461_("battle").equals(battle.toString())){int f=Integer.parseInt(slot.getKey().split(":")[0]);if(TartarusInteriorLayout.bossFloor(f)){s.defeated.add(f);openGate(l,f);s.m_77762_();}}
 }
 public static boolean ready(State s,int f){return s.built.contains(f)&&s.progress.current(f)&&(!TartarusInteriorLayout.transitionFloor(f)||s.transitions19.contains(f));}
 private static void prepareFloor(ServerLevel l,State s,int floor){
  if(ready(s,floor)||s.jobs.containsKey(floor))return;
  if(l.m_6907_().stream().anyMatch(p->floor(p)==floor&&BattleService.current(p)!=null))return;
  for(var p:List.copyOf(l.m_6907_()))if(floor(p)==floor){leave(p);s.pending.put(p.m_20148_(),new Pending(floor,false,p.m_9236_().m_46472_(),p.m_20185_(),p.m_20186_(),p.m_20189_()));}
  s.built.remove(floor);s.repaired.remove(floor);s.plans.remove(floor);
  // Removing a loaded actor may invoke removal listeners; collect before mutating maps.
  var ids=new ArrayList<UUID>();for(var map:List.of(s.slots,s.props))for(var it=map.entrySet().iterator();it.hasNext();){var e=it.next();if(e.getKey().startsWith(floor+":")){try{ids.add(UUID.fromString(e.getValue().m_128461_("entity")));}catch(IllegalArgumentException ignored){}it.remove();}}
  for(var id:ids){var e=l.m_8791_(id);if(e!=null)e.m_146870_();}
  s.jobs.put(floor,new Job(s.progress.seed(l.m_7328_(),floor),floor));s.m_77762_();
 }
 public static void cycle(ServerLevel overworld){
  var l=overworld.m_7654_().m_129880_(DIMENSION);if(l==null)return;var s=data(l);boolean was=s.progress.active;long before=s.progress.day;
  if(s.progress.observe(overworld.m_46468_())){s.plans.clear();s.jobs.entrySet().removeIf(e->!TartarusInteriorLayout.fixedFloor(e.getKey()));s.m_77762_();}
  if(was&&!s.progress.active)for(var p:l.m_6907_())p.getPersistentData().m_128379_("velvet_eclipse:tartarus_dawn",true);
  if(was!=s.progress.active||before!=s.progress.day)s.m_77762_();
 }
 @SubscribeEvent public static void retireOldShadows(net.minecraftforge.event.entity.EntityJoinLevelEvent event){
  var e=event.getEntity();if(!(event.getLevel() instanceof ServerLevel l)||!inside(e))return;
  if(dungeonShadow(e)||e instanceof TartarusPropEntity){var s=data(l);int f=floor(e);if(!s.progress.current(f)||!e.getPersistentData().m_128441_("tartarus_generation")||e.getPersistentData().m_128454_("tartarus_generation")!=s.progress.generation(f)||dungeonShadow(e)&&(TartarusInteriorLayout.shadowLimit(f)==0||guardian(e)!=TartarusInteriorLayout.bossFloor(f)))event.setCanceled(true);
   if(dungeonShadow(e)&&guardian(e)){var slot=s.slots.get(e.getPersistentData().m_128461_(SLOT));if(s.defeated.contains(f)||slot==null||!slot.m_128461_("battle").isEmpty()||!slot.m_128461_("entity").equals(e.m_20148_().toString()))event.setCanceled(true);}
   if(e instanceof TartarusPropEntity){var prop=s.props.get(e.getPersistentData().m_128461_("tartarus_prop"));if(prop==null||!prop.m_128461_("entity").equals(e.m_20148_().toString()))event.setCanceled(true);}}
 }
 public static int teleportToFloor(ServerPlayer p,int target){
  if(target<0||target>250||BattleService.current(p)!=null||com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)||p.m_20159_()){p.m_5661_(Component.m_237113_("Finalize a batalha/animação e levante-se antes de teleportar."),false);return 0;}
  var l=p.m_20194_().m_129880_(DIMENSION);if(l==null){p.m_5661_(Component.m_237113_("Reinicie o mundo para carregar o Tártaro."),false);return 0;}
  var s=data(l);prepareFloor(l,s,target);s.pending.put(p.m_20148_(),new Pending(target,false,p.m_9236_().m_46472_(),p.m_20185_(),p.m_20186_(),p.m_20189_()));p.m_5661_(Component.m_237113_("Preparando Tártaro • andar "+target+". Aguarde no local."),true);return 1;
 }
 @SubscribeEvent public static void removed(EntityLeaveLevelEvent event){
  var e=event.getEntity();if(!(event.getLevel() instanceof ServerLevel l)||!inside(e)||!(dungeonShadow(e)||e instanceof TartarusPropEntity))return;
  // Chunk unload is not a defeat: retain the persistent slot and never duplicate the mob.
  if(e.m_146911_()!=Entity.RemovalReason.KILLED&&e.m_146911_()!=Entity.RemovalReason.DISCARDED)return;
  var s=data(l);if(e instanceof TartarusPropEntity){String id=e.getPersistentData().m_128461_("tartarus_prop");var prop=s.props.get(id);if(prop!=null&&prop.m_128461_("entity").equals(e.m_20148_().toString())){s.props.remove(id);s.m_77762_();}return;}var slot=s.slots.get(e.getPersistentData().m_128461_(SLOT));if(slot!=null&&slot.m_128461_("entity").equals(e.m_20148_().toString())&&slot.m_128461_("battle").isEmpty()){slot.m_128473_("entity");s.m_77762_();}
 }
}
