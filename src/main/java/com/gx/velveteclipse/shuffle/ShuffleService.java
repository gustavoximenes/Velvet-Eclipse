package com.gx.velveteclipse.shuffle;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.menu.MenuService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class ShuffleService {
 private record Teaching(String token,String skill,InteractionHand hand,long expires){}
 private static final Map<UUID,Teaching> teaching=new HashMap<>();
 private static final Map<UUID,Long> requests=new HashMap<>();
 private static boolean available(ServerPlayer p){return p.m_6084_()&&BattleService.current(p)==null&&!com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p);}
 public static void credit(Object object,int floor){credit(object,floor,false);}
 public static void credit(Object object,int floor,boolean rare){if(object instanceof ServerPlayer p){var d=ShuffleData.load(p);d.add(ShuffleRules.roll(floor,PlayerPersonaData.load(p).rosterIds(),new Random(),rare));d.save(p);status(p,"Shuffle Time disponível! Pressione P.");}}
 public static void status(ServerPlayer p,String message){var d=PlayerPersonaData.load(p);ShuffleNetwork.reply(p,new ShuffleSnapshot("status",ShuffleData.load(p).rounds().size(),d.yen(),"",0,List.of(),List.of(),"",message));}
 private static ShuffleSnapshot.Choice describe(ShuffleRules.Card c,PlayerPersonaData d){
  String name="",detail="";com.gx.velveteclipse.menu.MenuSnapshot.Persona persona=null;
  switch(c.kind()){
   case PERSONA->{persona=MenuService.describe(ShuffleRules.personaReward(c,d.rewardInstance(c.id())));name=persona.name();detail=(d.canReceivePersona(c.id())?"":"LIMITE ATINGIDO • ")+(d.rosterIds().contains(c.id())?"Fortalece sua Persona: ":"Receba a Persona ")+name+" • Lv "+persona.level();}
   case DARK_HOUR->{name="DARK HOUR+";detail="Acrescenta 5 minutos à Dark Hour para todo o servidor.";}
   case CUPS->{name="CUPS • COPAS";detail="Recupera "+c.amount()+"% do HP máximo.";}
   case COINS->{name="COINS • YEN";detail="Receba ¥ "+c.amount()+" na carteira.";}
   case WANDS->{name="WANDS • EXPERIÊNCIA";detail="Receba "+c.amount()+" EXP de jogador e da Persona equipada.";}
   case SWORDS->{name="SWORDS • SKILL CARD";detail="Receba Skill Card: "+PersonaRegistry.skill(c.id()).orElseThrow().displayName()+".";}
  }return new ShuffleSnapshot.Choice(c.kind(),c.id(),c.amount(),c.rank(),name,detail,persona);
 }
 private static void open(ServerPlayer p){var d=ShuffleData.load(p);var round=d.first();if(round==null){status(p,"Nenhum Shuffle Time disponível.");return;}var player=PlayerPersonaData.load(p);ShuffleNetwork.reply(p,new ShuffleSnapshot("shuffle",d.rounds().size(),player.yen(),round.token(),round.floor(),round.cards().stream().map(c->describe(c,player)).toList(),List.of(),"","Escolha uma carta. As outras serão descartadas."));}
 public static void openSkill(ServerPlayer p,InteractionHand hand,String skill){
  if(!available(p)){p.m_5661_(Component.m_237113_("Use Skill Cards fora de batalhas."),true);return;}
  var t=new Teaching(UUID.randomUUID().toString(),skill,hand,p.m_9236_().m_46467_()+1200);teaching.put(p.m_20148_(),t);var d=PlayerPersonaData.load(p);
  ShuffleNetwork.reply(p,new ShuffleSnapshot("skill",ShuffleData.load(p).rounds().size(),d.yen(),t.token(),0,List.of(),d.rosterInstances().stream().map(MenuService::describe).toList(),skill,"Selecione a Persona que aprenderá a habilidade."));
 }
 public static void handle(ServerPlayer p,ShuffleNetwork.Request r){
  var id=p.m_20148_();if(r.action().equals("close")){teaching.remove(id);return;}
  long now=p.m_9236_().m_46467_();Long previous=requests.put(id,now);if(previous!=null&&now-previous<3)return;
  if(!available(p)){status(p,"Aguarde o fim da batalha ou animação.");return;}
  if(r.action().equals("open")){open(p);return;}
  if(r.action().equals("teach")){teach(p,r,now);return;}
  if(!r.action().equals("choose"))return;
  var shuffle=ShuffleData.load(p);var round=shuffle.first();if(round==null||!round.token().equals(r.token())){status(p,"Essa seleção já foi utilizada.");return;}
  int index;try{index=Integer.parseInt(r.choice());}catch(NumberFormatException e){return;}if(index<0||index>=round.cards().size())return;
  if(round.cards().get(index).kind()==ShuffleRules.Kind.DARK_HOUR&&!com.gx.velveteclipse.darkhour.DarkHourDeadline.extend(p,300000)){message(p,"Guarde esta carta e use durante a próxima Dark Hour.");return;}
  var data=PlayerPersonaData.load(p);var result=ShuffleRewards.claim(shuffle,data,r.token(),index,skill->deliver(p,skill));
  if(result.card()==null){message(p,result.message());return;}var card=result.card();data.save(p);shuffle.save(p);BattleService.sync(p,"Recompensa recebida.");
  ShuffleNetwork.reply(p,new ShuffleSnapshot("claimed",shuffle.rounds().size(),data.yen(),round.token(),round.floor(),List.of(describe(card,data)),List.of(),"","Recompensa recebida!"));
 }
 private static boolean deliver(ServerPlayer p,String skill){
  var card=new ItemStack(SkillCardRegistry.CARDS.get(skill).get());var inventory=p.m_150109_();boolean space=inventory.m_36062_()>=0;
  for(int i=0;i<36&&!space;i++){var existing=inventory.m_8020_(i);space=ItemStack.m_150942_(existing,card)&&existing.m_41613_()<64;}
  // Vanilla creative inventory discards overflowing items while returning true.
  return space&&inventory.m_36054_(card);
 }
 private static void teach(ServerPlayer p,ShuffleNetwork.Request r,long now){
  var t=teaching.get(p.m_20148_());if(t==null||!t.token.equals(r.token())||now>t.expires){message(p,"Seleção expirada. Use o Skill Card novamente.");return;}
  var stack=p.m_21120_(t.hand);if(stack.m_41619_()||stack.m_41720_()!=SkillCardRegistry.CARDS.get(t.skill).get()){message(p,"Mantenha o Skill Card na mão para confirmar.");return;}
  var data=PlayerPersonaData.load(p);var persona=data.instance(r.choice());if(persona==null){message(p,"Selecione uma Persona que você possui.");return;}
  if(persona.learnedSkills().contains(t.skill)){message(p,"Essa Persona já conhece a habilidade. O cartão foi preservado.");return;}
  if(!ShuffleRewards.teach(data,persona.definitionId(),t.skill))return;data.save(p);stack.m_41774_(1);teaching.remove(p.m_20148_());BattleService.sync(p,"Habilidade aprendida.");
  ShuffleNetwork.reply(p,new ShuffleSnapshot("learned",ShuffleData.load(p).rounds().size(),data.yen(),t.token,0,List.of(),List.of(MenuService.describe(data.instance(persona.definitionId()))),t.skill,"Habilidade aprendida!"));
 }
 private static void message(ServerPlayer p,String message){var d=PlayerPersonaData.load(p);ShuffleNetwork.reply(p,new ShuffleSnapshot("message",ShuffleData.load(p).rounds().size(),d.yen(),"",0,List.of(),List.of(),"",message));}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent e){if(e.phase==TickEvent.Phase.END&&e.player instanceof ServerPlayer p&&p.f_19797_%100==0)status(p,"");}
 @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone e){ShuffleData.copy(e.getOriginal(),e.getEntity());}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){teaching.remove(e.getEntity().m_20148_());requests.remove(e.getEntity().m_20148_());}
}
