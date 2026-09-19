package com.gx.velveteclipse.network;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
public record DarkHourS2C(int x,int y,int z,int height,int revealed,boolean active){
 public static void encode(DarkHourS2C p,FriendlyByteBuf b){b.writeInt(p.x);b.writeInt(p.y);b.writeInt(p.z);b.writeInt(p.height);b.writeInt(p.revealed);b.writeBoolean(p.active);}
 public static DarkHourS2C decode(FriendlyByteBuf b){return new DarkHourS2C(b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readBoolean());}
 public static void handle(DarkHourS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.darkhour.DarkHourClient.accept(p)));c.setPacketHandled(true);}
}
