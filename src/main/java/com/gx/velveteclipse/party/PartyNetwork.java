package com.gx.velveteclipse.party;
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
public final class PartyNetwork {
 public static final Gson JSON=new Gson();
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","party"),()->"1","1"::equals,"1"::equals);
 public record Person(String id,String name,boolean online){}
 public record Asset(String id,String name,int count){}
 public record View(boolean open,String self,String leader,List<Person> members,List<Person> online,List<Asset> items,long yen,String inviter,String message,String token){}
 public record Request(String action,String target,String item,long amount,String token){
  static void encode(Request p,FriendlyByteBuf b){b.m_130072_(p.action,16);b.m_130072_(p.target,40);b.m_130072_(p.item,120);b.writeLong(p.amount);b.m_130072_(p.token,40);}
  static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(16),b.m_130136_(40),b.m_130136_(120),b.readLong(),b.m_130136_(40));}
  static void handle(Request p,Supplier<NetworkEvent.Context> s){var c=s.get();if(c.getSender()!=null)c.enqueueWork(()->PartyService.handle(c.getSender(),p));c.setPacketHandled(true);}
 }
 public record Response(String json){
  static void encode(Response p,FriendlyByteBuf b){b.m_130072_(p.json,131072);}
  static Response decode(FriendlyByteBuf b){return new Response(b.m_130136_(131072));}
  static void handle(Response p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.party.PartyScreen.accept(p.json)));c.setPacketHandled(true);}
 }
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void request(String action,String target,String item,long amount,String token){CHANNEL.sendToServer(new Request(action,target,item,amount,token));}
 public static void send(ServerPlayer p,View v){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Response(JSON.toJson(v)));}
}
