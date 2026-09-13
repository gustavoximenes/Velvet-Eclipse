package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public record BattleRosterS2C(List<Target> targets) {
 public record Target(UUID id,int entityId,String name,boolean ally,int hp,int maxHp,String persona,String known,boolean turn){}
 public BattleRosterS2C {targets=List.copyOf(targets);}
 public static void encode(BattleRosterS2C p,FriendlyByteBuf b){b.writeInt(p.targets.size());for(var t:p.targets){b.m_130077_(t.id);b.writeInt(t.entityId);b.m_130070_(t.name);b.writeBoolean(t.ally);b.writeInt(t.hp);b.writeInt(t.maxHp);b.m_130070_(t.persona);b.m_130070_(t.known);b.writeBoolean(t.turn);}}
 public static BattleRosterS2C decode(FriendlyByteBuf b){int n=b.readInt();if(n<0||n>256)throw new IllegalArgumentException("Invalid roster size");var list=new ArrayList<Target>();for(int i=0;i<n;i++)list.add(new Target(b.m_130259_(),b.readInt(),b.m_130277_(),b.readBoolean(),b.readInt(),b.readInt(),b.m_130277_(),b.m_130277_(),b.readBoolean()));return new BattleRosterS2C(list);}
 public static void handle(BattleRosterS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.BattleTargetHud.accept(p)));c.setPacketHandled(true);}
}
