package com.gx.velveteclipse.roguelike;
import com.google.gson.Gson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.*;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RogueNetwork {
 public static final Gson JSON=new Gson();
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","roguelike"),()->"2","2"::equals,"2"::equals);
 public record State(boolean open,boolean enabled,boolean owner,boolean editable,String focus,List<String> collected,List<String> active,String message,String card,String token,Map<String,Integer> ranks){
  public State{ranks=Map.copyOf(ArcanaUpCatalog.clean(ranks));}
  public State(boolean open,boolean enabled,boolean owner,boolean editable,String focus,List<String> collected,List<String> active,String message,String card,String token){this(open,enabled,owner,editable,focus,collected,active,message,card,token,Map.of());}
  public int rank(String id){return ranks.getOrDefault(id,0);}
  public static State empty(){return new State(false,false,false,false,"none",List.of(),List.of(),"Synchronizing…","","");}
  public RogueBonuses bonuses(){return enabled?new RogueBonuses(RogueCatalog.Focus.parse(focus),Set.copyOf(active)):RogueBonuses.NONE;}
 }
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){
  CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
  CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
 }
 public static void request(String action,String value){CHANNEL.sendToServer(new Request(action,value));}
 public static void reply(ServerPlayer player,State state){CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Response(JSON.toJson(state)));}
 public record Request(String action,String value){
  public static void encode(Request p,FriendlyByteBuf b){b.m_130072_(p.action,16);b.m_130072_(p.value,80);}
  public static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(16),b.m_130136_(80));}
  public static void handle(Request p,Supplier<NetworkEvent.Context> s){var c=s.get();if(c.getSender()!=null)c.enqueueWork(()->RogueService.handle(c.getSender(),p));c.setPacketHandled(true);}
 }
 public record Response(String json){
  public static void encode(Response p,FriendlyByteBuf b){b.m_130072_(p.json,16384);}
  public static Response decode(FriendlyByteBuf b){return new Response(b.m_130136_(16384));}
  public static void handle(Response p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.menu.RogueScreen.accept(p.json)));c.setPacketHandled(true);}
 }
}
