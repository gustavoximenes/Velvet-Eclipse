package com.gx.velveteclipse.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;
import java.util.UUID;

/** A spectator-safe shot cue. Never changes position, resources or turn ownership. */
public record BattleDirectionS2C(UUID actor,int actorEntity,int targetEntity,String kind,String skill,String weapon,double x,double y,double z,double tx,double ty,double tz,int duration){
 public BattleDirectionS2C{if(duration<0||duration>160||!Double.isFinite(x+y+z+tx+ty+tz))throw new IllegalArgumentException("Invalid direction cue");}
 public static void encode(BattleDirectionS2C p,FriendlyByteBuf b){b.m_130077_(p.actor);b.writeInt(p.actorEntity);b.writeInt(p.targetEntity);b.m_130070_(p.kind);b.m_130070_(p.skill);b.m_130070_(p.weapon);b.writeDouble(p.x);b.writeDouble(p.y);b.writeDouble(p.z);b.writeDouble(p.tx);b.writeDouble(p.ty);b.writeDouble(p.tz);b.writeInt(p.duration);}
 public static BattleDirectionS2C decode(FriendlyByteBuf b){return new BattleDirectionS2C(b.m_130259_(),b.readInt(),b.readInt(),b.m_130277_(),b.m_130277_(),b.m_130277_(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readDouble(),b.readInt());}
 public static void handle(BattleDirectionS2C p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.presentation.BattleDirector.accept(p)));c.setPacketHandled(true);}
}
