package com.gx.velveteclipse.darkhour;
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
public final class TartarusTerminalNetwork {
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","terminals"),()->"1","1"::equals,"1"::equals);
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void request(int entity,int floor){CHANNEL.sendToServer(new Request(entity,floor));}
 public static void reply(ServerPlayer p,List<Integer> floors){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Response(TartarusDungeonService.floor(p),floors));}
 public record Request(int entity,int floor){
  public static void encode(Request r,FriendlyByteBuf b){b.m_130130_(r.entity);b.m_130130_(r.floor);}
  public static Request decode(FriendlyByteBuf b){return new Request(b.m_130242_(),b.m_130242_());}
  static void handle(Request r,Supplier<NetworkEvent.Context> s){var c=s.get();var p=c.getSender();if(p!=null)c.enqueueWork(()->TartarusTerminals.request(p,r));c.setPacketHandled(true);}
 }
 public record Response(int current,List<Integer> floors){
  public static void encode(Response r,FriendlyByteBuf b){b.m_130130_(r.current);b.m_130130_(r.floors.size());for(int f:r.floors)b.m_130130_(f);}
  public static Response decode(FriendlyByteBuf b){int f=b.m_130242_(),n=b.m_130242_();if(n<0||n>40)throw new IllegalArgumentException("Terminal count");var floors=new ArrayList<Integer>();for(int i=0;i<n;i++)floors.add(b.m_130242_());return new Response(f,List.copyOf(floors));}
  static void handle(Response r,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.tartarus.TerminalClient.open(r)));c.setPacketHandled(true);}
 }
}
