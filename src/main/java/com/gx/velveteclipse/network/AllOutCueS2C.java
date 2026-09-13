package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public record AllOutCueS2C(boolean active,int leader,List<Integer> fighters,boolean victory) {
 public static void encode(AllOutCueS2C p,FriendlyByteBuf b){b.writeBoolean(p.active);b.writeInt(p.leader);b.writeInt(p.fighters.size());for(int id:p.fighters)b.writeInt(id);b.writeBoolean(p.victory);}
 public static AllOutCueS2C decode(FriendlyByteBuf b){boolean active=b.readBoolean();int leader=b.readInt(),n=b.readInt();if(n<0||n>256)throw new IllegalArgumentException("Invalid lineup");var ids=new ArrayList<Integer>();for(int i=0;i<n;i++)ids.add(b.readInt());return new AllOutCueS2C(active,leader,List.copyOf(ids),b.readBoolean());}
 public static void handle(AllOutCueS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.AllOutPresentation.accept(p)));c.setPacketHandled(true);}
}
