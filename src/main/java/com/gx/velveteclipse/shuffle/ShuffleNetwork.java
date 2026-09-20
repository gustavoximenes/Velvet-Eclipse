package com.gx.velveteclipse.shuffle;
import com.gx.velveteclipse.menu.MenuNetwork;
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
import java.util.Optional;
import java.util.function.Supplier;
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ShuffleNetwork {
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","shuffle"),()->"1","1"::equals,"1"::equals);
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void request(String action,String token,String choice){CHANNEL.sendToServer(new Request(action,token,choice));}
 public static void reply(ServerPlayer p,ShuffleSnapshot data){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Response(MenuNetwork.JSON.toJson(data)));}
 public record Request(String action,String token,String choice){
  static void encode(Request p,FriendlyByteBuf b){b.m_130072_(p.action,16);b.m_130072_(p.token,36);b.m_130072_(p.choice,64);}
  static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(16),b.m_130136_(36),b.m_130136_(64));}
  static void handle(Request p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();var player=c.getSender();if(player!=null)c.enqueueWork(()->ShuffleService.handle(player,p));c.setPacketHandled(true);}
 }
 public record Response(String json){
  static void encode(Response p,FriendlyByteBuf b){b.m_130072_(p.json,131072);}
  static Response decode(FriendlyByteBuf b){return new Response(b.m_130136_(131072));}
  static void handle(Response p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.shuffle.ShuffleClient.accept(p.json)));c.setPacketHandled(true);}
 }
}
