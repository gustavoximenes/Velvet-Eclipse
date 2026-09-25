package com.gx.velveteclipse.raid;
import com.gx.velveteclipse.darkhour.*;import com.gx.velveteclipse.forge.*;import com.gx.velveteclipse.core.battle.*;import com.gx.velveteclipse.core.persona.*;import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.core.*;import net.minecraft.core.registries.Registries;import net.minecraft.resources.*;import net.minecraft.nbt.*;import net.minecraft.network.chat.Component;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.level.*;import net.minecraft.world.level.block.Blocks;import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.TickEvent;import net.minecraftforge.event.entity.player.PlayerEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.fml.common.Mod;
import java.util.*;
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class RaidService {
 public static final ResourceKey<Level> DIMENSION=ResourceKey.m_135785_(Registries.f_256858_,new ResourceLocation("velvet_eclipse","monorail"));
 private static final String RETURN="velvet_eclipse:raid_return";
 private static final Map<UUID,Run> MEMBERS=new HashMap<>();private static final List<Run> RUNS=new ArrayList<>();
 private static final List<TrainLayout.Cell> PLAN=TrainLayout.build();private static final Random RANDOM=new Random();private static int ticks;
 private static final TicketType<Integer> TICKET=TicketType.m_9465_("velvet_train",Comparator.<Integer>naturalOrder(),100);
 public static final class Settings extends SavedData{
  int capacity=PriestessRules.DEFAULT_CAPACITY;
  static Settings load(CompoundTag t){var s=new Settings();s.capacity=Math.max(1,Math.min(PriestessRules.MAX_CAPACITY,t.m_128451_("capacity")));return s;}
  @Override public CompoundTag m_7176_(CompoundTag t){t.m_128405_("capacity",capacity);return t;}
 }
 private static Settings settings(net.minecraft.server.MinecraftServer server){return server.m_129783_().m_8895_().m_164861_(Settings::load,Settings::new,"velvet_raid_settings");}
 private static final class Run{
  final LinkedHashMap<UUID,String> names=new LinkedHashMap<>();final int slot,capacity;final Set<UUID> acknowledged=new HashSet<>();
  int built,stage,dialogue,revision=1;String mode="lobby",message="";long pulseUntil;UUID battle;Entity actor;PriestessRules rules;
  Run(int slot,int capacity){this.slot=slot;this.capacity=capacity;}
  int x(){return slot*256;}UUID leader(){return names.keySet().iterator().next();}
 }
 public static boolean inside(Object p){return p instanceof Entity e&&e.m_9236_().m_46472_().equals(DIMENSION);}
 public static boolean member(Object p){return p instanceof Entity e&&MEMBERS.containsKey(e.m_20148_());}
 public static boolean battle(UUID id){return RUNS.stream().anyMatch(r->id.equals(r.battle));}
 public static boolean animating(UUID id){return RUNS.stream().anyMatch(r->id.equals(r.battle)&&r.pulseUntil>now());}
 private static long now(){return System.nanoTime()/1_000_000;}
 private static ServerPlayer player(UUID id){var s=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();return s==null?null:s.m_6846_().m_11259_(id);}
 private static List<ServerPlayer> players(Run r){return r.names.keySet().stream().map(RaidService::player).filter(Objects::nonNull).toList();}
 private static ServerLevel world(){var s=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();return s==null?null:s.m_129880_(DIMENSION);}
 private static void say(ServerPlayer p,String text){p.m_5661_(Component.m_237113_(text),false);}
 public static void open(ServerPlayer p){
  if(!com.gx.velveteclipse.awakening.AwakeningService.canBattle(p)){say(p,"Desperte sua Persona com a Evoker antes de entrar na raid.");return;}
  if(!p.m_6084_()||BattleService.current(p)!=null||TartarusDungeonService.floor(p)!=25)return;
  var old=MEMBERS.get(p.m_20148_());if(old!=null){send(old,p);return;}
  var r=RUNS.stream().filter(a->a.mode.equals("lobby")).findFirst().orElse(null);
  if(r==null){int slot=0;var used=RUNS.stream().map(a->a.slot).toList();while(used.contains(slot))slot++;r=new Run(slot,settings(p.m_20194_()).capacity);RUNS.add(r);}
  if(r.names.size()>=r.capacity){say(p,"A raid está cheia ("+r.capacity+" jogadores).");return;}
  r.names.put(p.m_20148_(),p.m_7755_().getString());MEMBERS.put(p.m_20148_(),r);broadcast(r);
 }
 private static boolean atDoor(ServerPlayer p){return TartarusDungeonService.floor(p)==25&&p.m_20275_(TartarusInteriorLayout.origin(25)+21.5,65,256.5)<=64&&BattleService.current(p)==null;}
 public static void request(ServerPlayer p,RaidNetwork.Request q){
  var r=MEMBERS.get(p.m_20148_());if(r==null)return;
  if(q.action().equals("leave")){if(r.mode.equals("lobby")){r.names.remove(p.m_20148_());MEMBERS.remove(p.m_20148_());RaidNetwork.send(p,RaidNetwork.View.empty());if(r.names.isEmpty())RUNS.remove(r);else broadcast(r);}else fail(r,"A equipe abandonou a operação.");return;}
  if(q.action().equals("start")&&r.mode.equals("lobby")&&r.leader().equals(p.m_20148_())){
   if(players(r).size()!=r.names.size()||players(r).stream().anyMatch(a->!atDoor(a)||!a.m_6084_()||PlayerPersonaData.load(a).hp()<=0)){say(p,"Todos devem estar vivos e próximos da porta Monad.");return;}
   if(world()==null){say(p,"Reabra o mundo para carregar o trem.");return;}
   r.mode="building";r.revision++;broadcast(r);return;
  }
  if(q.action().equals("next")&&r.mode.equals("dialogue")&&q.revision()==r.revision&&inside(p)){
   r.acknowledged.add(p.m_20148_());if(r.acknowledged.containsAll(r.names.keySet())){r.acknowledged.clear();r.dialogue++;r.revision++;if(r.dialogue>=r.names.size()+2){r.mode="walking";spawn(r);}broadcast(r);}
  }
 }
 private static String line(Run r){
  int n=r.names.size();if(r.dialogue==0)return "Fiquem juntos. Vamos atravessar o trem e descobrir o que está controlando tudo isso.";
  if(r.dialogue<n)return switch(r.dialogue%4){case 1->"Os vagões estão vazios... mas alguma coisa se moveu à frente.";case 2->"As portas se fecharam. Se isto é uma armadilha, vamos abrir caminho.";case 3->"Há duas presenças no corredor. Primeiro cuidamos delas, depois seguimos.";default->"Estou pronto. Vou proteger a retaguarda enquanto avançamos.";};
  return r.dialogue==n?"O trem está acelerando. A presença mais forte está no último vagão.":"Ao enfrentá-la teremos cinco minutos. Observem a fraqueza: ela pode mudar durante a luta.";
 }
 private static void send(Run r,ServerPlayer p){String text=r.mode.equals("dialogue")?line(r):r.message;RaidNetwork.send(p,new RaidNetwork.View(r.mode,List.copyOf(r.names.values()),r.capacity,r.leader().equals(p.m_20148_()),r.revision,r.dialogue%r.names.size(),text,r.rules==null?0:(int)r.rules.remaining(now()),r.rules==null?"":BattleService.knownRaidWeakness(p),r.stage));}
 private static void broadcast(Run r){for(var p:players(r))send(r,p);}
 private static void enter(Run r){int i=0;for(var p:players(r)){
  var t=new CompoundTag();t.m_128347_("x",p.m_20185_());t.m_128347_("y",p.m_20186_());t.m_128347_("z",p.m_20189_());p.getPersistentData().m_128365_(RETURN,t);
  p.m_6915_();p.m_8999_(world(),r.x()+(i%3-1)*1.5+.5,65.05,5+(i/3)*2,0,0);p.f_19789_=0;i++;
 }r.mode="dialogue";r.revision++;broadcast(r);}
 private static void spawn(Run r){
  var l=world();if(l==null)return;if(r.actor!=null)r.actor.m_146870_();
  Entity e=r.stage==2?RaidRegistry.PRIESTESS.get().m_20615_(l):ShadowRegistry.shadowType().m_20615_(l);if(e==null){fail(r,"Não foi possível preparar o encontro.");return;}
  e.m_6034_(r.x()+.5,65.05,TrainLayout.encounterZ(r.stage)+.5);RuntimeBridge.setEntityRotation(e,180,0);e.m_20331_(true);e.m_20242_(true);
  if(e instanceof Mob m){m.m_21557_(true);m.m_21530_();}
  e.m_6593_(Component.m_237113_(r.stage==2?"Sacerdotisa":r.stage==0?"Guardião do Trem • Sentinela":"Guardião do Trem • Condutor"));e.getPersistentData().m_128379_("raid_actor",true);l.m_7967_(e);r.actor=e;
 }
 private static void startBattle(Run r){
  var party=players(r);if(party.isEmpty())return;
  for(int i=0;i<party.size();i++){var p=party.get(i);p.m_8999_(world(),r.x()+.5+(i%3-1)*2.1,65.05,TrainLayout.encounterZ(r.stage)-7-(i/3)*2.1,0,0);}
  var s=BattleService.startRaid(party,r.stage,r.actor);if(s==null){fail(r,"O encontro foi interrompido.");return;}
  r.battle=s.id();r.mode="battle";if(r.stage==2)r.rules=new PriestessRules(now());broadcast(r);
 }
 /** Called at resolved damage, before rewards: victory wins a tie with a phase penalty. */
 public static boolean allowAction(UUID id){for(var r:List.copyOf(RUNS))if(id.equals(r.battle)&&r.rules!=null&&r.mode.equals("battle")&&r.rules.remaining(now())==0){fail(r,"TEMPO ESGOTADO! O trem saiu de controle.",true);return false;}return true;}
 public static void afterAction(BattleSession s){
  for(var r:List.copyOf(RUNS))if(s.id().equals(r.battle)&&r.rules!=null){
   var boss=s.participants().stream().filter(c->c.side()==BattleSide.ENEMIES).findFirst().orElseThrow();
   if(!boss.alive()){r.rules.stop(now());r.mode="victory";r.message="A Sacerdotisa foi derrotada!";broadcast(r);return;}
   int loss=r.rules.update(boss.hp(),boss.maxHp(),RANDOM);boss.setRaidBoss(r.rules.weakness());
   if(loss>0){BattleService.resetRaidKnowledge(s.id());r.revision++;r.message="ACELERAÇÃO! −"+loss+"s";broadcast(r);}
  }
 }
 public static boolean iceTurn(UUID id){for(var r:RUNS)if(id.equals(r.battle)&&r.rules!=null){boolean ice=r.rules.iceTurn();r.pulseUntil=now()+2800;if(r.actor!=null)r.actor.m_146926_(-45);return ice;}return false;}
 public static void finished(UUID id,boolean won){for(var r:List.copyOf(RUNS))if(id.equals(r.battle)){if(r.rules!=null)r.rules.stop(now());r.mode=won?"results":"failed";r.message=won?"Encontro vencido. Confirme os resultados para continuar.":"A operação fracassou.";broadcast(r);}}
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;ticks++;
  for(var r:List.copyOf(RUNS)){
   if(r.names.isEmpty()){RUNS.remove(r);continue;}
   if(r.mode.equals("lobby")){if(ticks%20==0)for(var p:players(r))if(!atDoor(p))request(p,new RaidNetwork.Request("leave",r.revision));continue;}
   var l=world();if(l==null)continue;
   for(int z=0;z<=128;z+=16)l.m_7726_().m_8387_(TICKET,new ChunkPos(new BlockPos(r.x(),65,z)),2,r.slot);
   if(r.mode.equals("building")){
    if(players(r).size()!=r.names.size()||players(r).stream().anyMatch(p->!atDoor(p))){fail(r,"Embarque cancelado: mantenham-se próximos da porta.");continue;}
    int budget=2500;while(r.built<PLAN.size()&&budget-->0){var c=PLAN.get(r.built++);var state=c.material().equals("air")?Blocks.f_50016_.m_49966_():RaidRegistry.BLOCKS.get(c.material()).get().m_49966_();l.m_7731_(new BlockPos(r.x()+c.x(),64+c.y(),c.z()),state,2);}
    if(r.built==PLAN.size())enter(r);continue;
   }
   if(r.mode.equals("failed")){fail(r,"A equipe foi derrotada. A próxima tentativa começa do início.",true);continue;}
   if(r.actor!=null&&now()>r.pulseUntil)r.actor.m_146926_(0);
   if(r.rules!=null&&r.mode.equals("battle")&&r.rules.remaining(now())==0){fail(r,"TEMPO ESGOTADO! O trem saiu de controle.",true);continue;}
   if(r.mode.equals("results")&&players(r).stream().allMatch(p->BattleService.current(p)==null)){
    if(r.stage==2){finish(r);continue;}
    int gate=TrainLayout.gateZ(r.stage);for(int x=-5;x<=5;x++)for(int y=1;y<7;y++)l.m_7731_(new BlockPos(r.x()+x,64+y,gate),Blocks.f_50016_.m_49966_(),2);
    for(var p:players(r)){var d=PlayerPersonaData.load(p);if(d.hp()==0){d.setResources(1,d.sp());d.save(p);}}
    r.stage++;r.battle=null;r.mode="walking";r.message="Passagem liberada. Sigam para o próximo vagão.";spawn(r);broadcast(r);
   }
   boolean gone=false;for(var p:players(r)){
    if(!inside(p)||!p.m_6084_()){gone=true;break;}
    if(r.mode.equals("dialogue")){int index=new ArrayList<>(r.names.keySet()).indexOf(p.m_20148_());RuntimeBridge.freezePlayer(p,new double[]{r.x()+(index%3-1)*1.5+.5,65.05,5+(index/3)*2});}
    if(r.mode.equals("walking")&&(p.m_20186_()<64||p.m_20186_()>70||Math.abs(p.m_20185_()-r.x())>7||p.m_20189_()>TrainLayout.encounterZ(r.stage)+3))p.m_8999_(l,r.x()+.5,65.05,TrainLayout.encounterZ(r.stage)-10,0,0);
   }
   if(gone){fail(r,"Um integrante deixou o trem. A operação foi encerrada.");continue;}
   if(r.mode.equals("walking")&&players(r).stream().anyMatch(p->p.m_20189_()>TrainLayout.encounterZ(r.stage)-10))startBattle(r);
   if(ticks%10==0)broadcast(r);
  }
 }
 private static void returnPlayer(ServerPlayer p){
  var t=p.getPersistentData().m_128469_(RETURN);var l=p.m_20194_().m_129880_(TartarusDungeonService.DIMENSION);
  if(l==null)l=p.m_20194_().m_129783_();double x=t.m_128441_("x")?t.m_128459_("x"):TartarusInteriorLayout.origin(25)+15.5;
  p.m_8999_(l,x,t.m_128441_("y")?t.m_128459_("y"):65.05,t.m_128441_("z")?t.m_128459_("z"):256.5,90,0);p.f_19789_=0;p.getPersistentData().m_128473_(RETURN);
 }
 public static void endDarkHour(){for(var r:List.copyOf(RUNS)){for(var p:players(r))BattleService.abortEncounter(p);fail(r,"A Dark Hour terminou. A operaÃ§Ã£o foi encerrada.");}}
 private static void cleanup(Run r){if(r.actor!=null)r.actor.m_146870_();for(UUID id:r.names.keySet())MEMBERS.remove(id);RUNS.remove(r);}
 private static void fail(Run r,String message){fail(r,message,false);}
 private static void fail(Run r,String message,boolean fatal){
  var party=players(r);boolean embarked=!r.mode.equals("building")&&!r.mode.equals("lobby");cleanup(r);
  for(var p:party){BattleService.stop(p);if(inside(p))returnPlayer(p);RaidNetwork.send(p,RaidNetwork.View.empty());say(p,message);if(embarked&&fatal){p.getPersistentData().m_128379_("velvet_raid_defeated",true);var d=PlayerPersonaData.load(p);d.setResources(0,d.sp());d.save(p);p.m_6074_();}else{var d=PlayerPersonaData.load(p);if(d.hp()==0){d.setResources(1,d.sp());d.save(p);}BattleService.sync(p,message);}}
 }
 private static void finish(Run r){var party=players(r);cleanup(r);for(var p:party){var d=PlayerPersonaData.load(p);if(d.hp()==0){d.setResources(1,d.sp());d.save(p);}returnPlayer(p);RaidNetwork.send(p,RaidNetwork.View.empty());BattleService.sync(p,"OPERAÇÃO CONCLUÍDA • O trem foi detido.");say(p,"OPERAÇÃO CONCLUÍDA • O trem foi detido.");}}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var r=MEMBERS.get(e.getEntity().m_20148_());if(r!=null){if(r.mode.equals("lobby")&&e.getEntity() instanceof ServerPlayer p)request(p,new RaidNetwork.Request("leave",r.revision));else fail(r,"A conexão de um integrante foi perdida. Reinicie a operação.");}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p&&(inside(p)||p.getPersistentData().m_128441_(RETURN))){returnPlayer(p);RaidNetwork.send(p,RaidNetwork.View.empty());}}
 @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.LOWEST) public static void clonePlayer(PlayerEvent.Clone e){if(e.getOriginal().getPersistentData().m_128471_("velvet_raid_defeated")){var d=PlayerPersonaData.load(e.getOriginal());d.restore();d.save(e.getEntity());e.getEntity().getPersistentData().m_128473_("velvet_raid_defeated");e.getEntity().getPersistentData().m_128379_("velvet_raid_respawn",true);}}
 @SubscribeEvent public static void loadedActor(net.minecraftforge.event.entity.EntityJoinLevelEvent e){if(!e.getLevel().f_46443_&&e.loadedFromDisk()&&inside(e.getEntity())&&e.getEntity().getPersistentData().m_128471_("raid_actor"))e.setCanceled(true);}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getPersistentData().m_128471_("velvet_raid_respawn")){p.getPersistentData().m_128473_("velvet_raid_respawn");BattleService.sync(p,"A próxima tentativa começa do início.");}}
 @SubscribeEvent public static void stopping(net.minecraftforge.event.server.ServerStoppingEvent e){for(var r:List.copyOf(RUNS))fail(r,"Servidor encerrado. A operação será reiniciada na próxima entrada.");}
 @SubscribeEvent public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent e){MEMBERS.clear();RUNS.clear();ticks=0;}
 @SubscribeEvent public static void commands(net.minecraftforge.event.RegisterCommandsEvent e){e.getDispatcher().register(net.minecraft.commands.Commands.m_82127_("velvetraid").requires(s->s.m_6761_(2)).then(net.minecraft.commands.Commands.m_82127_("maxplayers").then(net.minecraft.commands.Commands.m_82129_("quantidade",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,PriestessRules.MAX_CAPACITY)).executes(c->{var s=settings(c.getSource().m_81377_());s.capacity=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"quantidade");s.m_77762_();c.getSource().m_288197_(() -> Component.m_237113_("Limite das próximas raids: "+s.capacity),true);return s.capacity;}))));}
}
