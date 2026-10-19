package com.gx.velveteclipse.city;
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
import java.util.*;import java.util.function.Supplier;
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class CityNetwork {
 public static final Gson JSON=new Gson();
 public static final int MAX_VIEW_CHARS=262144;
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","city"),()->"8","8"::equals,"8"::equals);
 public record Row(String id,String name,String detail,int price,String group,int owned,String metric,int from,int to){
  public Row(String id,String name,String detail,int price){this(id,name,detail,price,"",0,"",0,0);}
 }
 public record View(String page,String title,String token,String message,long yen,List<Row> rows,boolean enabled,boolean ready){}
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void request(String action,String id,String token){CHANNEL.sendToServer(new Request(action,id,token));}
 public static void send(ServerPlayer p,View v){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Response(JSON.toJson(v)));}
 public record Request(String action,String id,String token){static void encode(Request p,FriendlyByteBuf b){b.m_130072_(p.action,20);b.m_130072_(p.id,96);b.m_130072_(p.token,40);}static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(20),b.m_130136_(96),b.m_130136_(40));}static void handle(Request p,Supplier<NetworkEvent.Context>s){var c=s.get();if(c.getSender()!=null)c.enqueueWork(()->CityService.handle(c.getSender(),p));c.setPacketHandled(true);}}
 public record Response(String json){static void encode(Response p,FriendlyByteBuf b){b.m_130072_(p.json,MAX_VIEW_CHARS);}static Response decode(FriendlyByteBuf b){return new Response(b.m_130136_(MAX_VIEW_CHARS));}static void handle(Response p,Supplier<NetworkEvent.Context>s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.city.CityScreen.accept(p.json)));c.setPacketHandled(true);}}
}
