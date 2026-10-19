package com.gx.velveteclipse.menu;
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
import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class MenuNetwork {
    public static final Gson JSON=new Gson();
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("velvet_eclipse","menu"),()->"7","7"::equals,"7"::equals);
    @SubscribeEvent public static void setup(FMLCommonSetupEvent event){
        CHANNEL.registerMessage(0,Request.class,Request::encode,Request::decode,Request::handle,Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(1,Response.class,Response::encode,Response::decode,Response::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    public static void request(String action,String argument){CHANNEL.sendToServer(new Request(action,argument));}
    public static void reply(ServerPlayer player,MenuSnapshot snapshot){CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Response(JSON.toJson(snapshot)));}
    public record Request(String action,String argument){
        public static void encode(Request p,FriendlyByteBuf b){b.m_130072_(p.action,16);b.m_130072_(p.argument,160);}
        public static Request decode(FriendlyByteBuf b){return new Request(b.m_130136_(16),b.m_130136_(160));}
        public static void handle(Request p,Supplier<NetworkEvent.Context> supplier){
            var c=supplier.get();var player=c.getSender();
            if(player!=null)c.enqueueWork(()->MenuService.handle(player,p.action,p.argument));
            c.setPacketHandled(true);
        }
    }
    public record Response(String json){
        public static void encode(Response p,FriendlyByteBuf b){b.m_130072_(p.json,131072);}
        public static Response decode(FriendlyByteBuf b){return new Response(b.m_130136_(131072));}
        public static void handle(Response p,Supplier<NetworkEvent.Context> supplier){
            var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->
                com.gx.velveteclipse.client.menu.PersonaMenuScreen.accept(p.json)));
            c.setPacketHandled(true);
        }
    }
}
