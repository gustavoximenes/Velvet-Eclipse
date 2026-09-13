package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public record BattleTacticsS2C(UUID actor,boolean oneMore,int event,String kind,boolean allOut,List<UUID> passTargets,List<Down> downed) {
 public record Down(int entityId,boolean dizzy){}
 public static BattleTacticsS2C empty(){return new BattleTacticsS2C(null,false,0,"",false,List.of(),List.of());}
 public static void encode(BattleTacticsS2C p,FriendlyByteBuf b){b.writeBoolean(p.actor!=null);if(p.actor!=null)b.m_130077_(p.actor);b.writeBoolean(p.oneMore);b.writeInt(p.event);b.m_130070_(p.kind);b.writeBoolean(p.allOut);b.writeInt(p.passTargets.size());for(var id:p.passTargets)b.m_130077_(id);b.writeInt(p.downed.size());for(var d:p.downed){b.writeInt(d.entityId);b.writeBoolean(d.dizzy);}}
 public static BattleTacticsS2C decode(FriendlyByteBuf b){UUID actor=b.readBoolean()?b.m_130259_():null;boolean more=b.readBoolean();int event=b.readInt();String kind=b.m_130277_();boolean all=b.readBoolean();int n=b.readInt();if(n<0||n>256)throw new IllegalArgumentException("Invalid party");var targets=new ArrayList<UUID>();for(int i=0;i<n;i++)targets.add(b.m_130259_());n=b.readInt();if(n<0||n>256)throw new IllegalArgumentException("Invalid downed list");var downs=new ArrayList<Down>();for(int i=0;i<n;i++)downs.add(new Down(b.readInt(),b.readBoolean()));return new BattleTacticsS2C(actor,more,event,kind,all,List.copyOf(targets),List.copyOf(downs));}
 public static void handle(BattleTacticsS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.BattleTacticsClient.accept(p)));c.setPacketHandled(true);}
}
