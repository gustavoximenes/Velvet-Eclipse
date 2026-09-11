package com.gx.velveteclipse.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;
public record BattleCueS2C(String type,String skill,boolean weakness,double x,double y,double z,double tx,double ty,double tz){
 public static void encode(BattleCueS2C p,FriendlyByteBuf b){b.m_130070_(p.type);b.m_130070_(p.skill);b.writeBoolean(p.weakness);b.writeDouble(p.x);b.writeDouble(p.y);b.writeDouble(p.z);b.writeDouble(p.tx);b.writeDouble(p.ty);b.writeDouble(p.tz);}
 public static BattleCueS2C decode(FriendlyByteBuf b){return new BattleCueS2C(b.m_130277_(),b.m_130277_(),b.readBoolean(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble());}
 public static void handle(BattleCueS2C p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.PersonaCinematics.accept(p)));c.setPacketHandled(true);}
}
