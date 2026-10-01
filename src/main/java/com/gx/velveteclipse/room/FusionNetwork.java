package com.gx.velveteclipse.room;
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
public final class FusionNetwork {
 public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","fusion"),()->"3","3"::equals,"3"::equals);
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){
  CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
  CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  CHANNEL.registerMessage(2,ArchiveRequest.class,ArchiveRequest::encode,ArchiveRequest::decode,ArchiveRequest::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
  CHANNEL.registerMessage(3,ArchiveResponse.class,ArchiveResponse::encode,ArchiveResponse::decode,ArchiveResponse::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  CHANNEL.registerMessage(4,RoomEntry.class,RoomEntry::encode,RoomEntry::decode,RoomEntry::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
 }
 public static void request(String action,String a,String b,String token){CHANNEL.sendToServer(new Request(action,a,b,token));}
 public static void reply(ServerPlayer p,FusionSnapshot data){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Response(MenuNetwork.JSON.toJson(data)));}
 public static void archiveRequest(VelvetArchive.Request request){CHANNEL.sendToServer(new ArchiveRequest(MenuNetwork.JSON.toJson(request)));}
 public static void archiveReply(ServerPlayer p,VelvetArchive.View view){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new ArchiveResponse(MenuNetwork.JSON.toJson(view)));}
 public static void enter(ServerPlayer player,boolean seated){CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new RoomEntry(seated));}
 public record RoomEntry(boolean seated){
  static void encode(RoomEntry p,FriendlyByteBuf b){b.writeBoolean(p.seated);}
  static RoomEntry decode(FriendlyByteBuf b){return new RoomEntry(b.readBoolean());}
  static void handle(RoomEntry p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.room.VelvetEntranceScreen.enter(p.seated)));c.setPacketHandled(true);}
 }
 public record ArchiveRequest(String json){
  static void encode(ArchiveRequest p,FriendlyByteBuf b){b.m_130072_(p.json,8192);}
  static ArchiveRequest decode(FriendlyByteBuf b){return new ArchiveRequest(b.m_130136_(8192));}
  static void handle(ArchiveRequest p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();var player=c.getSender();if(player!=null)c.enqueueWork(()->{try{VelvetArchive.handle(player,MenuNetwork.JSON.fromJson(p.json,VelvetArchive.Request.class));}catch(com.google.gson.JsonParseException|IllegalArgumentException ignored){}});c.setPacketHandled(true);}
 }
 public record ArchiveResponse(String json){
  static void encode(ArchiveResponse p,FriendlyByteBuf b){b.m_130087_(com.gx.velveteclipse.core.fusion.FusionPayload.encode(p.json));}
  static ArchiveResponse decode(FriendlyByteBuf b){return new ArchiveResponse(com.gx.velveteclipse.core.fusion.FusionPayload.decode(b.m_130101_(com.gx.velveteclipse.core.fusion.FusionPayload.WIRE_LIMIT)));}
  static void handle(ArchiveResponse p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.menu.VelvetArchiveScreen.accept(p.json)));c.setPacketHandled(true);}
 }
 public record Request(String action,String first,String second,String token){
  static void encode(Request p,FriendlyByteBuf b){b.m_130072_(p.action,16);b.m_130072_(p.first,64);b.m_130072_(p.second,64);b.m_130072_(p.token,36);}
  static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(16),b.m_130136_(64),b.m_130136_(64),b.m_130136_(36));}
  static void handle(Request p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();var player=c.getSender();if(player!=null)c.enqueueWork(()->FusionRoomService.handle(player,p));c.setPacketHandled(true);}
 }
 public record Response(String json){
  static void encode(Response p,FriendlyByteBuf b){b.m_130072_(p.json,131072);}
  static Response decode(FriendlyByteBuf b){return new Response(b.m_130136_(131072));}
  static void handle(Response p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.menu.FusionScreen.accept(p.json)));c.setPacketHandled(true);}
 }
}
