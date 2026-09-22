package com.gx.velveteclipse.equipment;
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
public final class EquipmentNetwork {
 public static final Gson JSON=new Gson();
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","equipment"),()->"3","3"::equals,"3"::equals);
 public record Snapshot(EquipmentLoadout bag,long yen,String view,String message,int npc,boolean merchant,String token,int shopTier){public Snapshot(EquipmentLoadout bag,long yen,String view,String message,int npc,boolean merchant,String token){this(bag,yen,view,message,npc,merchant,token,0);}}
 @SubscribeEvent public static void setup(FMLCommonSetupEvent event){CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void request(String action,String id,String token){CHANNEL.sendToServer(new Request(action,id,token));}
 public static void reply(ServerPlayer p,Snapshot d){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Response(JSON.toJson(d)));}
 public record Request(String action,String id,String token){
  static void encode(Request p,FriendlyByteBuf b){b.m_130072_(p.action,16);b.m_130072_(p.id,96);b.m_130072_(p.token,40);}
  static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(16),b.m_130136_(96),b.m_130136_(40));}
  static void handle(Request p,Supplier<NetworkEvent.Context> s){var c=s.get();if(c.getSender()!=null)c.enqueueWork(()->EquipmentService.handle(c.getSender(),p));c.setPacketHandled(true);}
 }
 public record Response(String json){
  static void encode(Response p,FriendlyByteBuf b){b.m_130072_(p.json,65536);}
  static Response decode(FriendlyByteBuf b){return new Response(b.m_130136_(65536));}
  static void handle(Response p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.equipment.EquipmentScreen.accept(p.json)));c.setPacketHandled(true);}
 }
}
