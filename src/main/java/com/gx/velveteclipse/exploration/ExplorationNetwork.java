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
    private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","exploration"),()->"5","5"::equals,"5"::equals);
    public static final int TOGGLE=0, ATTACK=1;
    @SubscribeEvent public static void setup(FMLCommonSetupEvent e){
        CHANNEL.registerMessage(4,Entry.class,Entry::encode,Entry::decode,Entry::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(3,Swing.class,Swing::encode,Swing::decode,Swing::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(2,Shot.class,Shot::encode,Shot::decode,Shot::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1,State.class,State::encode,State::decode,State::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void request(int action){CHANNEL.sendToServer(new Request(action));}
    public static void reply(ServerPlayer p,ExplorationRules.Mode mode,int tick,int sequence,int target,float yaw){
        reply(p,mode,tick,sequence,target,yaw,p.m_20182_(),p.m_20182_());
    }
    public static void reply(ServerPlayer p,ExplorationRules.Mode mode,int tick,int sequence,int target,float yaw,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end){
        reply(p,mode,tick,sequence,target,yaw,start,end,com.gx.velveteclipse.core.battle.EncounterOpening.NEUTRAL);
    }
    public static void reply(ServerPlayer p,ExplorationRules.Mode mode,int tick,int sequence,int target,float yaw,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end,com.gx.velveteclipse.core.battle.EncounterOpening opening){
        CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new State(mode.ordinal(),tick,sequence,target,yaw,start.f_82479_,start.f_82480_,start.f_82481_,end.f_82479_,end.f_82480_,end.f_82481_,opening==null?0:opening.ordinal()));
    }
    public static void entry(ServerPlayer p,java.util.UUID session,com.gx.velveteclipse.core.battle.EncounterOpening opening,int elapsed){
        CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Entry(session,opening.ordinal(),elapsed));
    }
    public record Entry(java.util.UUID session,int opening,int elapsed){
        static void encode(Entry p,FriendlyByteBuf b){b.writeLong(p.session.getMostSignificantBits());b.writeLong(p.session.getLeastSignificantBits());b.writeByte(p.opening);b.writeInt(p.elapsed);}
        static Entry decode(FriendlyByteBuf b){return new Entry(new java.util.UUID(b.readLong(),b.readLong()),b.readUnsignedByte(),b.readInt());}
        static void handle(Entry p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.exploration.EntryTransition.accept(p)));c.setPacketHandled(true);}
    }
    public static void shot(ServerPlayer p,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end,int ticks){var shot=new Shot(start.f_82479_,start.f_82480_,start.f_82481_,end.f_82479_,end.f_82480_,end.f_82481_,ticks,p.m_19879_());for(var viewer:p.m_20194_().m_6846_().m_11314_())if(viewer.m_9236_()==p.m_9236_()&&viewer.m_20280_(p)<4096)CHANNEL.send(PacketDistributor.PLAYER.with(()->viewer),shot);}
    public static void swing(ServerPlayer p,String family,float yaw){var packet=new Swing(p.m_19879_(),family,yaw);for(var viewer:p.m_20194_().m_6846_().m_11314_())if(viewer.m_9236_()==p.m_9236_()&&viewer.m_20280_(p)<4096)CHANNEL.send(PacketDistributor.PLAYER.with(()->viewer),packet);}
    public record Swing(int entity,String family,float yaw){
      static void encode(Swing p,FriendlyByteBuf b){b.writeInt(p.entity);b.m_130070_(p.family);b.writeFloat(p.yaw);}
      static Swing decode(FriendlyByteBuf b){return new Swing(b.readInt(),b.m_130277_(),b.readFloat());}
      static void handle(Swing p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.presentation.RemoteWeaponSwing.accept(p)));c.setPacketHandled(true);}
    }
    public record Shot(double sx,double sy,double sz,double ex,double ey,double ez,int ticks,int actor){
      public Shot(double sx,double sy,double sz,double ex,double ey,double ez,int ticks){this(sx,sy,sz,ex,ey,ez,ticks,-1);}
      static void encode(Shot s,FriendlyByteBuf b){b.writeDouble(s.sx);b.writeDouble(s.sy);b.writeDouble(s.sz);b.writeDouble(s.ex);b.writeDouble(s.ey);b.writeDouble(s.ez);b.writeInt(s.ticks);b.writeInt(s.actor);}
      static Shot decode(FriendlyByteBuf b){return new Shot(b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readInt(),b.readInt());}
      static void handle(Shot s,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.exploration.ArrowFlight.accept(s)));c.setPacketHandled(true);}
    }
    public record Request(int action){
        static void encode(Request p,FriendlyByteBuf b){b.writeByte(p.action);}
        static Request decode(FriendlyByteBuf b){return new Request(b.readUnsignedByte());}
        static void handle(Request p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();var player=c.getSender();if(player!=null)c.enqueueWork(()->ExplorationService.request(player,p.action));c.setPacketHandled(true);}
    }
    public record State(int mode,int tick,int sequence,int target,float yaw,double sx,double sy,double sz,double ex,double ey,double ez,int opening){
        static void encode(State p,FriendlyByteBuf b){b.writeByte(p.mode);b.writeInt(p.tick);b.writeInt(p.sequence);b.writeInt(p.target);b.writeFloat(p.yaw);b.writeDouble(p.sx);b.writeDouble(p.sy);b.writeDouble(p.sz);b.writeDouble(p.ex);b.writeDouble(p.ey);b.writeDouble(p.ez);b.writeByte(p.opening);}
        static State decode(FriendlyByteBuf b){return new State(b.readUnsignedByte(),b.readInt(),b.readInt(),b.readInt(),b.readFloat(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readUnsignedByte());}
        static void handle(State p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.exploration.ExplorationClient.accept(p)));c.setPacketHandled(true);}
    }
}
