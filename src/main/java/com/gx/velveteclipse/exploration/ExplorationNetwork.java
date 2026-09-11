package com.gx.velveteclipse.exploration;

import java.util.Optional;
import java.util.function.Supplier;
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

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ExplorationNetwork {
    private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","exploration"),()->"1","1"::equals,"1"::equals);
    public static final int TOGGLE=0, ATTACK=1;
    @SubscribeEvent public static void setup(FMLCommonSetupEvent e){
        CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1,State.class,State::encode,State::decode,State::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void request(int action){CHANNEL.sendToServer(new Request(action));}
    public static void reply(ServerPlayer p,ExplorationRules.Mode mode,int tick,int sequence,int target,float yaw){
        reply(p,mode,tick,sequence,target,yaw,p.m_20182_(),p.m_20182_());
    }
    public static void reply(ServerPlayer p,ExplorationRules.Mode mode,int tick,int sequence,int target,float yaw,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end){
        CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new State(mode.ordinal(),tick,sequence,target,yaw,start.f_82479_,start.f_82480_,start.f_82481_,end.f_82479_,end.f_82480_,end.f_82481_));
    }
    public record Request(int action){
        static void encode(Request p,FriendlyByteBuf b){b.writeByte(p.action);}
        static Request decode(FriendlyByteBuf b){return new Request(b.readUnsignedByte());}
        static void handle(Request p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();var player=c.getSender();if(player!=null)c.enqueueWork(()->ExplorationService.request(player,p.action));c.setPacketHandled(true);}
    }
    public record State(int mode,int tick,int sequence,int target,float yaw,double sx,double sy,double sz,double ex,double ey,double ez){
        static void encode(State p,FriendlyByteBuf b){b.writeByte(p.mode);b.writeInt(p.tick);b.writeInt(p.sequence);b.writeInt(p.target);b.writeFloat(p.yaw);b.writeDouble(p.sx);b.writeDouble(p.sy);b.writeDouble(p.sz);b.writeDouble(p.ex);b.writeDouble(p.ey);b.writeDouble(p.ez);}
        static State decode(FriendlyByteBuf b){return new State(b.readUnsignedByte(),b.readInt(),b.readInt(),b.readInt(),b.readFloat(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble());}
        static void handle(State p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.exploration.ExplorationClient.accept(p)));c.setPacketHandled(true);}
    }
}
