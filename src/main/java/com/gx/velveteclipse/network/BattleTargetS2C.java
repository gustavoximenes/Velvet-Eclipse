package com.gx.velveteclipse.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;
public record BattleTargetS2C(int entityId,String enemyType,String known){
 public static void encode(BattleTargetS2C p,FriendlyByteBuf b){b.writeInt(p.entityId);b.m_130070_(p.enemyType);b.m_130070_(p.known);}
 public static BattleTargetS2C decode(FriendlyByteBuf b){return new BattleTargetS2C(b.readInt(),b.m_130277_(),b.m_130277_());}
 public static void handle(BattleTargetS2C p,Supplier<NetworkEvent.Context> supplier){var c=supplier.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.BattleTargetHud.accept(p)));c.setPacketHandled(true);}
}
