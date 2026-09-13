package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
public record BattleLightningS2C(List<Strike> strikes) {
 public record Strike(double x,double y,double z,long seed){}
 public static void encode(BattleLightningS2C p,FriendlyByteBuf b){b.writeInt(p.strikes.size());for(var s:p.strikes){b.writeDouble(s.x);b.writeDouble(s.y);b.writeDouble(s.z);b.writeLong(s.seed);}}
 public static BattleLightningS2C decode(FriendlyByteBuf b){int n=b.readInt();if(n<0||n>5)throw new IllegalArgumentException("Invalid strikes");var list=new ArrayList<Strike>();for(int i=0;i<n;i++)list.add(new Strike(b.readDouble(),b.readDouble(),b.readDouble(),b.readLong()));return new BattleLightningS2C(List.copyOf(list));}
 public static void handle(BattleLightningS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.LightningStrikeRenderer.accept(p)));c.setPacketHandled(true);}
}
