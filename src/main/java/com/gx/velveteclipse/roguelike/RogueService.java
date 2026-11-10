package com.gx.velveteclipse.roguelike;
import com.gx.velveteclipse.forge.BattleService;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.*;
import static com.gx.velveteclipse.localization.ModText.component;

@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class RogueService {
 private static final String KEY="velvet_eclipse:roguelike_v1";
 private static final ArcanaConfirmation OFFERS=new ArcanaConfirmation();
 private static final Map<UUID,Long> REQUESTS=new HashMap<>();
 private static final Random LOOT=new Random();
 private static final com.google.gson.Gson JSON=new com.google.gson.Gson();
 public static RogueProfile load(Object p){try{var d=JSON.fromJson(RuntimeBridge.tagGetString(RuntimeBridge.persistentData(p),KEY),RogueProfile.class);return d==null?new RogueProfile():d.normalized();}catch(RuntimeException e){return new RogueProfile();}}
 public static void save(Object p,RogueProfile data){RuntimeBridge.tagPutString(RuntimeBridge.persistentData(p),KEY,JSON.toJson(data.normalized()));}
 public static RogueBonuses bonuses(ServerPlayer p){return load(p).bonuses(RogueRules.enabled(p.m_20194_()));}
 public static Map<String,Integer> fusionRanks(ServerPlayer p){return RogueRules.enabled(p.m_20194_())?Map.copyOf(load(p).ranks):Map.of();}
 public static boolean owner(ServerPlayer p){
  var s=p.m_20194_();var integrated=s.m_236731_();
  if(integrated!=null)return RoguePermissions.owner(p.m_20148_(),integrated.getId(),"");
  String id=RogueOwner.get(s).uuid;if(id.isBlank())id=System.getProperty("velvet.roguelikeOwner",System.getProperty("velvet.cityOwner",""));
  return RoguePermissions.owner(p.m_20148_(),null,id);
 }
 private static boolean console(CommandSourceStack s){return s.f_81288_ instanceof MinecraftServer||s.f_81288_ instanceof net.minecraft.server.rcon.RconConsoleSource;}
 private static boolean blocked(ServerPlayer p){return !p.m_6084_()||BattleService.current(p)!=null||com.gx.velveteclipse.raid.RaidService.member(p)||com.gx.velveteclipse.awakening.AwakeningService.active(p)||com.gx.velveteclipse.exploration.ExplorationService.preparing(p);}
 private static boolean worldBusy(MinecraftServer s){return s.m_6846_().m_11314_().stream().anyMatch(RogueService::blocked);}
 public static void sync(ServerPlayer p,boolean open,String message){var o=OFFERS.peek(p.m_20148_(),p.m_9236_().m_46467_());reply(p,open,message,o==null?"":o.card(),o==null?"":o.token());}
 private static void reply(ServerPlayer p,boolean open,String message,String card,String token){var d=load(p);RogueNetwork.reply(p,new RogueNetwork.State(open,RogueRules.enabled(p.m_20194_()),owner(p),!blocked(p),d.focus,List.copyOf(d.collected),List.copyOf(d.active),message,card,token,d.ranks));}
 public static void broadcast(MinecraftServer s){OFFERS.clearAll();for(var p:s.m_6846_().m_11314_())sync(p,false,"");}
 public static void offer(ServerPlayer p,InteractionHand hand){
  if(blocked(p)||!RogueRules.enabled(p.m_20194_())){p.m_5661_(component("Enable Roguelike mode and leave combat before using an Arcana Buff."),true);return;}
  String id=ArcanaItems.id(p.m_21120_(hand));if(id.isEmpty())return;
  var d=load(p);d.collect(id);save(p,d);
  if(!d.canUse(id)){OFFERS.clear(p.m_20148_());sync(p,true,ArcanaUpCatalog.offered(id)==null?"This Arcana is already active. Duplicates do not stack.":"This Arcana is already rank 10. No card was consumed.");return;}
  int slot=hand==InteractionHand.OFF_HAND?40:p.m_150109_().f_35977_;
  var o=OFFERS.open(p.m_20148_(),id,slot,p.m_9236_().m_46467_());reply(p,true,"",id,o.token());
 }
 public static void handle(ServerPlayer p,RogueNetwork.Request r){
  long now=p.m_9236_().m_46467_();UUID id=p.m_20148_();long last=REQUESTS.getOrDefault(id,Long.MIN_VALUE/2);
  if(now-last<2)return;REQUESTS.put(id,now);
  switch(r.action()){
   case "open"->{OFFERS.clear(id);sync(p,true,"");}
   case "cancel"->{OFFERS.clear(id);sync(p,false,"");}
   case "toggle"->{
    if(!owner(p)||worldBusy(p.m_20194_())||!Set.of("true","false").contains(r.value())){sync(p,false,"Only the world owner may change this rule, outside battles and raids.");return;}
    OFFERS.clear(id);p.m_20194_().m_129900_().m_46170_(RogueRules.ENABLED).m_46246_(Boolean.parseBoolean(r.value()),p.m_20194_());sync(p,false,"World rule saved. Your cards are preserved while the mode is off.");
   }
   case "focus"->{
    var focus=RogueCatalog.Focus.parse(r.value());
    if(blocked(p)||!RogueRules.enabled(p.m_20194_())||focus==RogueCatalog.Focus.NONE||!focus.id().equals(r.value())){sync(p,false,"Choose a class outside combat while Roguelike mode is enabled.");return;}
    var d=load(p);d.focus=focus.id();save(p,d);sync(p,false,"Class focus saved for your next battle.");
   }
   case "confirm"->{
    var offer=OFFERS.take(id,r.value(),now);
    if(offer==null||blocked(p)||!RogueRules.enabled(p.m_20194_())){sync(p,false,"Confirmation expired or unavailable. Use the card again.");return;}
    var stack=p.m_150109_().m_8020_(offer.slot());var d=load(p);
    if(!d.activateFromItem(offer.card(),ArcanaItems.id(stack),RogueRules.enabled(p.m_20194_()),!blocked(p))){sync(p,false,ArcanaUpCatalog.offered(offer.card())==null?"The card moved or this buff is already active. Nothing was consumed.":"The card moved or this Arcana is already rank 10. Nothing was consumed.");return;}
    stack.m_41774_(1);save(p,d);p.m_150109_().m_6596_();p.f_36096_.m_38946_();var up=ArcanaUpCatalog.offered(offer.card());sync(p,false,up==null?"Arcana activated. One card was consumed.":"Arcana rank increased: "+up.name()+" • "+d.rank(up.id())+" / 10.");
   }
  }
 }
 public static void treasure(ServerPlayer p){
  if(!RogueRules.enabled(p.m_20194_()))return;String id=RogueCatalog.rollTreasure(LOOT);if(id.isEmpty())return;
  var item=new ItemStack(ArcanaItems.entry(id).get());
  if(!p.m_150109_().m_36054_(item))p.m_36176_(item,false);
  var d=load(p);d.collect(id);save(p,d);sync(p,false,"Arcana card found. Use the item to review and confirm.");
 }
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p,false,"");}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)sync(p,false,"");}
 @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone e){save(e.getEntity(),load(e.getOriginal()));OFFERS.clear(e.getEntity().m_20148_());}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){OFFERS.clear(e.getEntity().m_20148_());REQUESTS.remove(e.getEntity().m_20148_());}
 @SubscribeEvent public static void inventory(TickEvent.PlayerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p)||p.f_19797_%40!=0)return;
  var d=load(p);boolean changed=false;
  for(int i=0;i<p.m_150109_().m_6643_();i++){String id=ArcanaItems.id(p.m_150109_().m_8020_(i));if(!id.isEmpty())changed|=d.collect(id);}
  if(changed){save(p,d);sync(p,false,"");}
 }
 @SubscribeEvent public static void commands(RegisterCommandsEvent e){
  e.getDispatcher().register(Commands.m_82127_("velvetrogue").then(Commands.m_82127_("owner").requires(RogueService::console).then(Commands.m_82129_("uuid",StringArgumentType.word()).executes(c->{
   try{String id=UUID.fromString(StringArgumentType.getString(c,"uuid")).toString();var s=RogueOwner.get(c.getSource().m_81377_());s.uuid=id;s.m_77762_();broadcast(c.getSource().m_81377_());c.getSource().m_288197_(() -> component("Roguelike owner saved: "+id),false);return 1;}catch(IllegalArgumentException ex){c.getSource().m_81352_(component("Enter a valid player UUID."));return 0;}
  }))));
 }
 /** Protect direct and /execute-wrapped gamerule writes, including OPs who are not owners. */
 @SubscribeEvent public static void command(CommandEvent e){
  if(!RoguePermissions.writesRule(e.getParseResults().getContext()))return;var source=e.getParseResults().getContext().getSource();
  boolean allowed=console(source)||source.m_81373_() instanceof ServerPlayer p&&owner(p);
  if(!allowed||worldBusy(source.m_81377_())){e.setCanceled(true);source.m_81352_(component("Only the world owner may change this rule, outside battles and raids."));}
 }
 private RogueService(){}
}
