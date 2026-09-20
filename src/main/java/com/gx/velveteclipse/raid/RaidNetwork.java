package com.gx.velveteclipse.raid;
import net.minecraft.network.FriendlyByteBuf;import net.minecraft.resources.ResourceLocation;import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;import net.minecraftforge.fml.DistExecutor;import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.*;import net.minecraftforge.network.simple.SimpleChannel;import java.util.*;import java.util.function.Supplier;
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RaidNetwork {
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","raid"),()->"1","1"::equals,"1"::equals);
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));CHANNEL.registerMessage(1,View.class,View::encode,View::decode,View::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void request(String action,int revision){CHANNEL.sendToServer(new Request(action,revision));}
 public static void send(ServerPlayer p,View v){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),v);}
 public record Request(String action,int revision){
  public static void encode(Request r,FriendlyByteBuf b){b.m_130072_(r.action,16);b.writeInt(r.revision);}
  public static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(16),b.readInt());}
  static void handle(Request r,Supplier<NetworkEvent.Context> s){var c=s.get();if(c.getSender()!=null)c.enqueueWork(()->RaidService.request(c.getSender(),r));c.setPacketHandled(true);}
 }
 public record View(String mode,List<String> names,int capacity,boolean leader,int revision,int speaker,String line,int milliseconds,String weakness,int stage){
  public static View empty(){return new View("",List.of(),5,false,0,0,"",0,"",0);}
  public static void encode(View r,FriendlyByteBuf b){b.m_130072_(r.mode,16);b.writeInt(r.names.size());for(String n:r.names)b.m_130072_(n,40);b.writeInt(r.capacity);b.writeBoolean(r.leader);b.writeInt(r.revision);b.writeInt(r.speaker);b.m_130072_(r.line,400);b.writeInt(r.milliseconds);b.m_130072_(r.weakness,24);b.writeInt(r.stage);}
  public static View decode(FriendlyByteBuf b){String mode=b.m_130136_(16);int n=b.readInt();if(n<0||n>PriestessRules.MAX_CAPACITY)throw new IllegalArgumentException("Raid roster");var names=new ArrayList<String>();for(int i=0;i<n;i++)names.add(b.m_130136_(40));return new View(mode,List.copyOf(names),b.readInt(),b.readBoolean(),b.readInt(),b.readInt(),b.m_130136_(400),b.readInt(),b.m_130136_(24),b.readInt());}
  static void handle(View r,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.raid.RaidClient.accept(r)));c.setPacketHandled(true);}
 }
}
