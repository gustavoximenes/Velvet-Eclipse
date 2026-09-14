package com.gx.velveteclipse.room;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

/** Invisible mount supplies Minecraft's synchronized sitting pose. */
public final class VelvetSeatEntity extends Entity {
 public VelvetSeatEntity(EntityType<? extends VelvetSeatEntity> type,Level level){super(type,level);m_20242_(true);m_20331_(true);}
 @Override protected void m_8097_(){}
 @Override protected void m_7378_(CompoundTag tag){}
 @Override protected void m_7380_(CompoundTag tag){}
 @Override public Packet<ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
 @Override public double m_6048_(){return 0;}
 @Override public void m_8119_(){super.m_8119_();if(!m_9236_().f_46443_&&(!m_9236_().m_8055_(m_20183_()).m_60713_(VelvetRoomRegistry.CHAIR.get())||f_19797_>20&&m_20197_().isEmpty()))m_146870_();}
 @Override public Vec3 m_7688_(LivingEntity passenger){double angle=Math.toRadians(m_146908_());
  for(double turn:new double[]{0,Math.PI/2,-Math.PI/2,Math.PI}){double x=m_20185_()-Math.sin(angle+turn)*1.2,z=m_20189_()+Math.cos(angle+turn)*1.2,y=Math.floor(m_20186_());var box=passenger.m_20191_().m_82386_(x-passenger.m_20185_(),y-passenger.m_20186_(),z-passenger.m_20189_());if(m_9236_().m_45756_(passenger,box)&&!m_9236_().m_45756_(passenger,box.m_82386_(0,-.8,0)))return new Vec3(x,y,z);}
  return new Vec3(m_20185_(),Math.floor(m_20186_())+.7,m_20189_());}
}
