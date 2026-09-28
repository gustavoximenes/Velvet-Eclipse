package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public record AllOutCueS2C(boolean active,int leader,List<Integer> fighters,boolean victory,String style,Map<Integer,String> weapons) {
 public AllOutCueS2C(boolean active,int leader,List<Integer> fighters,boolean victory){this(active,leader,fighters,victory,"makoto",Map.of());}
 public AllOutCueS2C{fighters=List.copyOf(fighters);var normalized=new HashMap<Integer,String>();for(int id:fighters)normalized.put(id,weapons.getOrDefault(id,"SWORD"));weapons=Map.copyOf(normalized);style=com.gx.velveteclipse.core.battle.AllOutStyle.parse(style).id();if(fighters.size()>256)throw new IllegalArgumentException("Invalid lineup");}
 public static void encode(AllOutCueS2C p,FriendlyByteBuf b){b.writeBoolean(p.active);b.writeInt(p.leader);b.writeInt(p.fighters.size());for(int id:p.fighters){b.writeInt(id);b.m_130072_(p.weapons.getOrDefault(id,"SWORD"),32);}b.writeBoolean(p.victory);b.m_130072_(p.style,16);}
 public static AllOutCueS2C decode(FriendlyByteBuf b){boolean active=b.readBoolean();int leader=b.readInt(),n=b.readInt();if(n<0||n>256)throw new IllegalArgumentException("Invalid lineup");var ids=new ArrayList<Integer>();var weapons=new HashMap<Integer,String>();for(int i=0;i<n;i++){int id=b.readInt();ids.add(id);weapons.put(id,b.m_130136_(32));}return new AllOutCueS2C(active,leader,List.copyOf(ids),b.readBoolean(),b.m_130136_(16),weapons);}
 public static void handle(AllOutCueS2C p,Supplier<NetworkEvent.Context> s){var c=s.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.AllOutPresentation.accept(p)));c.setPacketHandled(true);}
}
