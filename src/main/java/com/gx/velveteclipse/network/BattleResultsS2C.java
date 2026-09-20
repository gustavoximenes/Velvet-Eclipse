package com.gx.velveteclipse.network;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import java.util.function.Supplier;

/** Rewards have already been committed once; acknowledgement only releases the player. */
public record BattleResultsS2C(String battle,boolean victory,int exp,int beforeLevel,int level,int experience,int next,
                              String persona,int personaLevel,String items,boolean shuffle) {
 public static void encode(BattleResultsS2C p,FriendlyByteBuf b){b.m_130072_(p.battle,36);b.writeBoolean(p.victory);b.writeInt(p.exp);b.writeInt(p.beforeLevel);b.writeInt(p.level);b.writeInt(p.experience);b.writeInt(p.next);b.m_130072_(p.persona,100);b.writeInt(p.personaLevel);b.m_130072_(p.items,200);b.writeBoolean(p.shuffle);}
 public static BattleResultsS2C decode(FriendlyByteBuf b){return new BattleResultsS2C(b.m_130136_(36),b.readBoolean(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.readInt(),b.m_130136_(100),b.readInt(),b.m_130136_(200),b.readBoolean());}
 public static void handle(BattleResultsS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.BattleResultsClient.accept(p)));c.setPacketHandled(true);}
}
