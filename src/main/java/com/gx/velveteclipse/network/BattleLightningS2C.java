package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
public record BattleLightningS2C(List<Strike> strikes) {
 public record Strike(double x,double y,double z,long seed,int entityId,UUID entityUuid){
  public Strike(double x,double y,double z,long seed){this(x,y,z,seed,-1,null);}
  public Strike{if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Invalid effect position");}
  public static Strike at(net.minecraft.world.entity.Entity e){return new Strike(e.m_20185_(),e.m_20186_(),e.m_20189_(),e.m_19879_()*31L,e.m_19879_(),e.m_20148_());}
 }
 public static void encode(BattleLightningS2C p,FriendlyByteBuf b){b.writeInt(p.strikes.size());for(var s:p.strikes){b.writeDouble(s.x);b.writeDouble(s.y);b.writeDouble(s.z);b.writeLong(s.seed);b.writeInt(s.entityId);b.writeBoolean(s.entityUuid!=null);if(s.entityUuid!=null){b.writeLong(s.entityUuid.getMostSignificantBits());b.writeLong(s.entityUuid.getLeastSignificantBits());}}}
 public static BattleLightningS2C decode(FriendlyByteBuf b){int n=b.readInt();if(n<0||n>5)throw new IllegalArgumentException("Invalid strikes");var list=new ArrayList<Strike>();for(int i=0;i<n;i++){double x=b.readDouble(),y=b.readDouble(),z=b.readDouble();long seed=b.readLong();int id=b.readInt();UUID uuid=b.readBoolean()?new UUID(b.readLong(),b.readLong()):null;list.add(new Strike(x,y,z,seed,id,uuid));}return new BattleLightningS2C(List.copyOf(list));}
 public static void handle(BattleLightningS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.LightningStrikeRenderer.accept(p)));c.setPacketHandled(true);}
}
