package com.gx.velveteclipse.awakening;

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
public final class AwakeningNetwork {
    private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","awakening"),()->"1","1"::equals,"1"::equals);
    @SubscribeEvent public static void setup(FMLCommonSetupEvent e){
        CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1,State.class,State::encode,State::decode,State::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void request(String token,String action,String persona){CHANNEL.sendToServer(new Request(token,action,persona));}
    public static void send(ServerPlayer p,String token,String phase,String persona){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new State(token,phase,persona));}
    public record Request(String token,String action,String persona){
        static void encode(Request p,FriendlyByteBuf b){b.m_130070_(p.token);b.m_130070_(p.action);b.m_130070_(p.persona);}
        static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(40),b.m_130136_(16),b.m_130136_(16));}
        static void handle(Request p,Supplier<NetworkEvent.Context> s){var c=s.get();var player=c.getSender();if(player!=null)c.enqueueWork(()->AwakeningService.request(player,p));c.setPacketHandled(true);}
    }
    public record State(String token,String phase,String persona){
        static void encode(State p,FriendlyByteBuf b){b.m_130070_(p.token);b.m_130070_(p.phase);b.m_130070_(p.persona);}
        static State decode(FriendlyByteBuf b){return new State(b.m_130136_(40),b.m_130136_(16),b.m_130136_(16));}
        static void handle(State p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.awakening.AwakeningScreen.accept(p)));c.setPacketHandled(true);}
    }
}
