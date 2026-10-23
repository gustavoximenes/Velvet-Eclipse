package com.gx.velveteclipse.network;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
/** Server-selected recipients only. World furniture uses ZERO and never contains combat routes. */
public record BattleFeedbackS2C(UUID session,long event,String kind,String key,List<Target> targets) {
 public static final UUID WORLD=new UUID(0,0);
 public static final int MAX_TARGETS=128; // 64 players plus enemies for universal skills.
 public record Target(BattleLightningS2C.Strike point,String outcome){public Target{Objects.requireNonNull(point);if(!Set.of("normal","miss","reflect","absorb","null","critical","weak").contains(outcome))throw new IllegalArgumentException("Feedback outcome");}}
 public BattleFeedbackS2C {Objects.requireNonNull(session);Objects.requireNonNull(kind);Objects.requireNonNull(key);targets=List.copyOf(targets);if(targets.size()>MAX_TARGETS||kind.length()>32||key.length()>128)throw new IllegalArgumentException("Feedback bounds");}
 public static void encode(BattleFeedbackS2C p,FriendlyByteBuf b){b.m_130077_(p.session);b.writeLong(p.event);b.m_130070_(p.kind);b.m_130070_(p.key);b.writeInt(p.targets.size());for(var t:p.targets){var s=t.point;b.writeDouble(s.x());b.writeDouble(s.y());b.writeDouble(s.z());b.writeLong(s.seed());b.writeInt(s.entityId());b.writeBoolean(s.entityUuid()!=null);if(s.entityUuid()!=null)b.m_130077_(s.entityUuid());b.m_130070_(t.outcome);}}
 public static BattleFeedbackS2C decode(FriendlyByteBuf b){var session=b.m_130259_();long event=b.readLong();String kind=b.m_130277_(),key=b.m_130277_();int n=b.readInt();if(n<0||n>MAX_TARGETS)throw new IllegalArgumentException("Feedback target count");var list=new ArrayList<Target>();for(int i=0;i<n;i++){double x=b.readDouble(),y=b.readDouble(),z=b.readDouble();long seed=b.readLong();int entity=b.readInt();UUID id=b.readBoolean()?b.m_130259_():null;list.add(new Target(new BattleLightningS2C.Strike(x,y,z,seed,entity,id),b.m_130277_()));}return new BattleFeedbackS2C(session,event,kind,key,list);}
 public static void handle(BattleFeedbackS2C p,Supplier<NetworkEvent.Context> source){var c=source.get();c.enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->com.gx.velveteclipse.client.vfx.ReloadFeedback.accept(p)));c.setPacketHandled(true);}
}
