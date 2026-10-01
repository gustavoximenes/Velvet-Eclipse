package com.gx.velveteclipse.room;

import com.gx.velveteclipse.core.fusion.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.forge.*;
import com.gx.velveteclipse.menu.*;
import com.gx.velveteclipse.party.PartyService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import java.util.*;

/** All decisions, prices and ownership are authoritative server state. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class VelvetArchive {
 public record Request(String action,String result,List<PartyFusion.Source> sources,List<String> skills,String token,String recipient){}
 public record Stock(String owner,String ownerName,String id,String name,int level,boolean offered){}
 public record Book(String id,String name,int level,long price,boolean owned,MenuSnapshot.Persona persona){}
 public record Person(String id,String name){}
 public record Recipe(String id,String name,int level,List<String> ingredients){}
 public record View(boolean open,String self,int level,long yen,List<Stock> stock,List<Book> book,List<Person> party,List<Recipe> recipes,MenuSnapshot.Persona result,List<SkillDefinition> skills,List<PartyFusion.Source> sources,String special,String token,boolean allowed,String message,int capacity,int ownedCount,List<FusionSearch.Option> results,int resultCount,int resultOffset,boolean fused,String sequence){}
 private record Offer(UUID owner,UUID recipient,String persona,String fingerprint,long expiry){}
 private record Ticket(String token,List<PartyFusion.Source> sources,String special,Map<UUID,String> fingerprints,long expiry){}
 private static final Map<String,Offer> OFFERS=new HashMap<>();
 private static final Map<UUID,Ticket> TICKETS=new HashMap<>();
 private static final Map<UUID,Long> GATE=new HashMap<>();
 private record Search(List<FusionSearch.Option> options,int offset){}
 private static final Map<UUID,Search> SEARCH=new HashMap<>();
 private static long now(){return System.currentTimeMillis();}
 private static boolean available(ServerPlayer p){return p!=null&&FusionRoomService.available(p);}
 private static String key(UUID owner,String persona){return owner+"/"+persona;}
 private static Map<UUID,ServerPlayer> nearby(ServerPlayer p){var out=new LinkedHashMap<UUID,ServerPlayer>();for(var member:PartyService.members(p))if(available(member))out.put(member.m_20148_(),member);return out;}
 private static String sourceFingerprint(PlayerPersonaData d){return FusionTransaction.fingerprint(d)+"/"+d.personaCapacity();}
 private static boolean consent(ServerPlayer p,PartyFusion.Source s,Map<UUID,ServerPlayer> players,Map<UUID,PlayerPersonaData> data){
  if(!players.containsKey(s.owner()))return false;if(s.owner().equals(p.m_20148_()))return true;
  var offer=OFFERS.get(key(s.owner(),s.persona()));return offer!=null&&offer.recipient().equals(p.m_20148_())&&offer.expiry()>now()&&offer.fingerprint().equals(sourceFingerprint(data.get(s.owner())));
 }
 private static Map<UUID,PlayerPersonaData> data(Map<UUID,ServerPlayer> players){var map=new LinkedHashMap<UUID,PlayerPersonaData>();players.forEach((id,p)->map.put(id,PlayerPersonaData.load(p)));return map;}
 public static void open(ServerPlayer p){if(available(p)){TICKETS.remove(p.m_20148_());view(p,true,null,"Escolha uma fusão ou consulte seu Compendium.");}}
 public static void handle(ServerPlayer p,Request r){
  if(r==null||r.action()==null||r.action().length()>20)return;
  if(r.action().equals("close")||r.action().equals("stand")){TICKETS.remove(p.m_20148_());SEARCH.remove(p.m_20148_());if(r.action().equals("stand")&&VelvetRoomService.inside(p)&&p.m_20202_() instanceof VelvetSeatEntity)p.m_8127_();return;}
  if(!available(p))return;
  if(now()-GATE.getOrDefault(p.m_20148_(),0L)<100)return;GATE.put(p.m_20148_(),now());
  if(r.sources()==null||r.sources().size()>6||r.skills()==null||r.skills().size()>2||r.result()==null||r.token()==null||r.recipient()==null)return;
  if(r.result().length()>100||r.token().length()>100||r.recipient().length()>100||r.skills().stream().anyMatch(s->s==null||s.length()>100)||r.sources().stream().anyMatch(s->s==null||s.owner()==null||s.persona()==null||s.persona().length()>100))return;
  var players=nearby(p);var data=data(players);var self=p.m_20148_();var own=data.get(self);if(own==null)return;
  OFFERS.values().removeIf(o->o.expiry()<now());String message="";PartyFusion.Preview preview=null;
  switch(r.action()){
   case "open"->{open(p);return;}
   case "refresh","cancel"->{TICKETS.remove(self);SEARCH.remove(self);}
   case "search"->{
    TICKETS.remove(self);int offset;try{offset=Math.max(0,Integer.parseInt(r.result()));}catch(NumberFormatException e){offset=0;}
    var allowed=new ArrayList<PartyFusion.Source>();for(var entry:data.entrySet())for(String id:entry.getValue().rosterIds()){var source=new PartyFusion.Source(entry.getKey(),id);if(consent(p,source,players,data))allowed.add(source);}
    var options=FusionSearch.available(data,self,allowed);SEARCH.put(self,new Search(options,Math.min(offset,Math.max(0,(((int)options.stream().map(FusionSearch.Option::id).distinct().count()-1)/16)*16))));
    message="Receitas disponíveis para seu nível e suas contribuições autorizadas.";
   }
   case "summon"->{TICKETS.remove(self);if(own.summon(r.result())){own.save(p);BattleService.sync(p,"Persona invocada do Compendium.");message="Invocação concluída.";}else message="Invocação recusada: confira seu saldo, espaço e se já possui esta Persona.";}
   case "offer"->{
    TICKETS.remove(self);UUID recipient;try{recipient=UUID.fromString(r.recipient());}catch(IllegalArgumentException e){return;}
    if(recipient.equals(self)||!players.containsKey(recipient)||own.instance(r.result())==null||own.rosterIds().size()<2){message="Escolha um integrante na Velvet Room. Você deve manter pelo menos uma Persona.";break;}
    OFFERS.put(key(self,r.result()),new Offer(self,recipient,r.result(),sourceFingerprint(own),now()+180000));message="Contribuição autorizada por 3 minutos. A Persona só será consumida se a fusão for confirmada.";
    players.get(recipient).m_5661_(Component.m_237113_(p.m_7755_().getString()+" ofereceu "+r.result()+" para sua fusão. Atualize a lista com R."),false);
   }
   case "withdraw"->{OFFERS.remove(key(self,r.result()));message="Contribuição retirada.";}
   case "preview"->{
    TICKETS.remove(self);if(r.sources().stream().anyMatch(Objects::isNull)||!r.sources().stream().allMatch(s->consent(p,s,players,data))){message="Contribuição indisponível, expirada ou sem autorização.";break;}
    preview=PartyFusion.preview(data,self,r.sources(),r.result(),r.skills());if(preview==null){message="Combinação ou herança inválida.";break;}
    var fingerprints=new LinkedHashMap<UUID,String>();fingerprints.put(self,sourceFingerprint(own));r.sources().forEach(s->fingerprints.put(s.owner(),sourceFingerprint(data.get(s.owner()))));
    TICKETS.put(self,new Ticket(UUID.randomUUID().toString(),List.copyOf(r.sources()),r.result(),Map.copyOf(fingerprints),now()+180000));message=preview.message();
   }
   case "confirm"->{
    var t=TICKETS.remove(self);
    if(t==null||!t.token().equals(r.token())||t.expiry()<now()||!t.fingerprints().entrySet().stream().allMatch(e->data.containsKey(e.getKey())&&sourceFingerprint(data.get(e.getKey())).equals(e.getValue()))||!t.sources().stream().allMatch(s->consent(p,s,players,data))){message="A prévia expirou ou alguém alterou a contribuição. Faça uma nova prévia.";break;}
    var committed=PartyFusion.preview(data,self,t.sources(),t.special(),r.skills());
    if(!PartyFusion.commit(data,self,t.sources(),t.special(),r.skills())){message="Fusão recusada. Confira nível, capacidade e até duas habilidades válidas.";break;}
    var affected=new LinkedHashSet<UUID>();affected.add(self);t.sources().forEach(s->{affected.add(s.owner());OFFERS.remove(key(s.owner(),s.persona()));});
    for(var id:affected){data.get(id).save(players.get(id));BattleService.sync(players.get(id),"Fusão concluída.");if(!id.equals(self))players.get(id).m_5661_(Component.m_237113_("Sua contribuição foi usada na fusão de "+p.m_7755_().getString()+". Ela continua registrada no Compendium."),false);}
    message="Fusão concluída. A nova Persona foi registrada no Compendium.";
    SEARCH.remove(self);view(p,false,committed,message,true,t.sources(),t.token());return;
   }
   default->{return;}
  }
  view(p,false,preview,message);
 }
 private static void view(ServerPlayer p,boolean open,PartyFusion.Preview preview,String message){view(p,open,preview,message,false,List.of(),"");}
 private static void view(ServerPlayer p,boolean open,PartyFusion.Preview preview,String message,boolean fused,List<PartyFusion.Source> consumed,String sequence){
  var players=nearby(p);var data=data(players);var self=p.m_20148_();var own=data.get(self);if(own==null)return;var stock=new ArrayList<Stock>();
  for(var e:data.entrySet())for(var persona:e.getValue().rosterInstances()){
   var source=new PartyFusion.Source(e.getKey(),persona.definitionId());boolean isSelf=e.getKey().equals(self);if(!isSelf&&!consent(p,source,players,data))continue;
   stock.add(new Stock(e.getKey().toString(),players.get(e.getKey()).m_7755_().getString(),persona.definitionId(),PersonaRegistry.persona(persona.definitionId()).orElseThrow().displayName(),persona.level(),OFFERS.containsKey(key(e.getKey(),persona.definitionId()))));
  }
  var book=own.compendiumInstances().stream().sorted(Comparator.comparingInt(PersonaInstance::level).thenComparing(PersonaInstance::definitionId)).map(i->new Book(i.definitionId(),PersonaRegistry.persona(i.definitionId()).orElseThrow().displayName(),i.level(),PlayerPersonaData.summonPrice(i),own.rewardInstance(i.definitionId())!=null,MenuService.describe(i))).toList();
  var recipes=ReloadCatalogue.special().entrySet().stream().map(e->{var d=PersonaRegistry.persona(e.getKey()).orElseThrow();return new Recipe(d.id(),d.displayName(),d.baseLevel(),e.getValue());}).sorted(Comparator.comparingInt(Recipe::level)).toList();
  var ticket=TICKETS.get(self);var skills=preview==null?List.<SkillDefinition>of():preview.inheritable().stream().map(id->PersonaRegistry.skill(id).orElseThrow()).toList();
  var search=SEARCH.get(self);var keys=search==null?List.<String>of():search.options().stream().map(FusionSearch.Option::id).distinct().toList();var visible=search==null?Set.<String>of():new HashSet<>(keys.subList(search.offset(),Math.min(keys.size(),search.offset()+16)));var results=search==null?List.<FusionSearch.Option>of():search.options().stream().filter(o->visible.contains(o.id())).toList();
  var view=new View(open,self.toString(),own.playerLevel(),own.yen(),stock,book,players.values().stream().filter(x->x!=p).map(x->new Person(x.m_20148_().toString(),x.m_7755_().getString())).toList(),recipes,preview==null?null:MenuService.describe(preview.result()),skills,fused?consumed:ticket==null?List.of():ticket.sources(),ticket==null?"":ticket.special(),ticket==null?"":ticket.token(),preview!=null&&preview.allowed(),message,own.personaCapacity(),own.rosterIds().size(),results,keys.size(),search==null?0:search.offset(),fused,sequence);
  FusionNetwork.archiveReply(p,view);
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().m_20148_();TICKETS.remove(id);GATE.remove(id);SEARCH.remove(id);OFFERS.values().removeIf(o->o.owner().equals(id)||o.recipient().equals(id));}
 @SubscribeEvent public static void stop(ServerStoppedEvent e){OFFERS.clear();TICKETS.clear();GATE.clear();SEARCH.clear();}
}
