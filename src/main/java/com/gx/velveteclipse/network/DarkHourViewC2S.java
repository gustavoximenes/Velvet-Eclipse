package com.gx.velveteclipse.network;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
/** Client settings hint only; server view distance remains the upper bound. */
public record DarkHourViewC2S(int chunks){
 public static void encode(DarkHourViewC2S p,FriendlyByteBuf b){b.writeInt(p.chunks);}
 public static DarkHourViewC2S decode(FriendlyByteBuf b){int n=b.readInt();if(n<2||n>64)throw new IllegalArgumentException("Invalid render distance");return new DarkHourViewC2S(n);}
 public static void handle(DarkHourViewC2S p,Supplier<NetworkEvent.Context> s){var c=s.get();var player=c.getSender();c.enqueueWork(()->{if(player!=null){var tag=player.getPersistentData();long now=player.m_9236_().m_46467_(),last=tag.m_128454_("velvet_eclipse:view_tick");if(tag.m_128441_("velvet_eclipse:view_chunks")&&now>=last&&now-last<20)return;tag.m_128405_("velvet_eclipse:view_chunks",p.chunks);tag.m_128356_("velvet_eclipse:view_tick",now);}});c.setPacketHandled(true);}
}
