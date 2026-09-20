package com.gx.velveteclipse.darkhour;
import net.minecraft.network.FriendlyByteBuf;import net.minecraft.resources.ResourceLocation;import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;import net.minecraftforge.fml.DistExecutor;import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.*;import net.minecraftforge.network.simple.SimpleChannel;import java.util.*;import java.util.function.Supplier;
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class TartarusMapNetwork {
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","map_clock"),()->"1","1"::equals,"1"::equals);
 @SubscribeEvent public static void setup(FMLCommonSetupEvent e){CHANNEL.registerMessage(0,View.class,View::encode,View::decode,View::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));CHANNEL.registerMessage(1,Clock.class,Clock::encode,Clock::decode,Clock::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void send(ServerPlayer p,Object v){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),v);}
 public record Marker(int kind,float x,float z){}
 public record View(int floor,int minX,int minZ,int width,int height,byte[] tiles,List<Marker> markers){
  public static View empty(){return new View(-1,0,0,0,0,new byte[0],List.of());}
  public boolean tile(int x,int z){if(x<0||z<0||x>=width||z>=height)return false;int i=z*width+x;return i/8<tiles.length&&(tiles[i/8]&(1<<(i%8)))!=0;}
  public static void encode(View v,FriendlyByteBuf b){b.writeInt(v.floor);b.writeInt(v.minX);b.writeInt(v.minZ);b.writeInt(v.width);b.writeInt(v.height);b.writeInt(v.tiles.length);b.writeBytes(v.tiles);b.writeInt(v.markers.size());for(var m:v.markers){b.writeByte(m.kind);b.writeFloat(m.x);b.writeFloat(m.z);}}
  public static View decode(FriendlyByteBuf b){int f=b.readInt(),x=b.readInt(),z=b.readInt(),w=b.readInt(),h=b.readInt(),n=b.readInt();if(f< -1||f>250||w<0||h<0||w>128||h>128||n!=(w*h+7)/8)throw new IllegalArgumentException("Map bounds");byte[] tiles=new byte[n];b.readBytes(tiles);int count=b.readInt();if(count<0||count>80)throw new IllegalArgumentException("Map markers");var m=new ArrayList<Marker>();for(int i=0;i<count;i++)m.add(new Marker(b.readUnsignedByte(),b.readFloat(),b.readFloat()));return new View(f,x,z,w,h,tiles,List.copyOf(m));}
  static void handle(View v,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.tartarus.TartarusMapClient.accept(v)));c.setPacketHandled(true);}
 }
 public record Clock(int milliseconds){
  public static void encode(Clock v,FriendlyByteBuf b){b.writeInt(v.milliseconds);}
  public static Clock decode(FriendlyByteBuf b){return new Clock(Math.max(0,Math.min(DarkHourTimer.DURATION,b.readInt())));}
  static void handle(Clock v,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.tartarus.TartarusMapClient.clock(v.milliseconds)));c.setPacketHandled(true);}
 }
}
