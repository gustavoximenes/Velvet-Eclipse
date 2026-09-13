package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/** Observer-only information: no enemy resources or undiscovered affinities. */
public record BattleSceneS2C(List<Scene> scenes) {
 public record Scene(UUID id,String leader,double x,double y,double z,float radius,boolean joinable,List<Integer> enemies){}
 public static void encode(BattleSceneS2C p,FriendlyByteBuf b){b.writeInt(p.scenes.size());for(var s:p.scenes){b.m_130077_(s.id);b.m_130070_(s.leader);b.writeDouble(s.x);b.writeDouble(s.y);b.writeDouble(s.z);b.writeFloat(s.radius);b.writeBoolean(s.joinable);b.writeInt(s.enemies.size());for(int id:s.enemies)b.writeInt(id);}}
 public static BattleSceneS2C decode(FriendlyByteBuf b){int n=b.readInt();if(n<0||n>8)throw new IllegalArgumentException("Invalid scenes");var list=new ArrayList<Scene>();for(int i=0;i<n;i++){var id=b.m_130259_();var name=b.m_130277_();double x=b.readDouble(),y=b.readDouble(),z=b.readDouble();float r=b.readFloat();boolean join=b.readBoolean();int count=b.readInt();if(count<0||count>5)throw new IllegalArgumentException("Invalid enemies");var ids=new ArrayList<Integer>();for(int j=0;j<count;j++)ids.add(b.readInt());list.add(new Scene(id,name,x,y,z,r,join,List.copyOf(ids)));}return new BattleSceneS2C(List.copyOf(list));}
 public static void handle(BattleSceneS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.BattleEncounterClient.accept(p)));c.setPacketHandled(true);}
}
