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
 private String furniture="";private Vec3 stand;private boolean standSoundPlayed;
 public void cityFurniture(String id,Vec3 position){furniture=id;stand=position;}
 public VelvetSeatEntity(EntityType<? extends VelvetSeatEntity> type,Level level){super(type,level);m_20242_(true);m_20331_(true);}
 @Override protected void m_8097_(){}
 @Override protected void m_7378_(CompoundTag tag){furniture=tag.m_128461_("city_furniture");if(!furniture.isEmpty())stand=new Vec3(tag.m_128459_("stand_x"),tag.m_128459_("stand_y"),tag.m_128459_("stand_z"));}
 @Override protected void m_7380_(CompoundTag tag){if(!furniture.isEmpty()&&stand!=null){tag.m_128359_("city_furniture",furniture);tag.m_128347_("stand_x",stand.f_82479_);tag.m_128347_("stand_y",stand.f_82480_);tag.m_128347_("stand_z",stand.f_82481_);}}
 @Override public Packet<ClientGamePacketListener> m_5654_(){return NetworkHooks.getEntitySpawningPacket(this);}
 @Override public double m_6048_(){return 0;}
 @Override protected void m_19956_(Entity passenger,Entity.MoveFunction move){
  super.m_19956_(passenger,move);
  if(m_20363_(passenger)&&passenger instanceof LivingEntity living){
   float yaw=m_146908_(),delta=net.minecraft.util.Mth.m_14177_(passenger.m_146908_()-yaw);
   float look=yaw+Math.max(-65,Math.min(65,delta));
   passenger.m_146922_(look);living.m_5616_(look);living.m_5618_(yaw);living.f_20884_=yaw;
  }
 }
 @Override public void m_8119_(){super.m_8119_();if(!m_9236_().f_46443_&&(!(furniture.isEmpty()?m_9236_().m_8055_(m_20183_()).m_60713_(VelvetRoomRegistry.CHAIR.get()):com.gx.velveteclipse.city.CityFurniture.validSeat(this,furniture))||f_19797_>20&&m_20197_().isEmpty()))m_146870_();}
 @Override public Vec3 m_7688_(LivingEntity passenger){
  if(!standSoundPlayed&&passenger instanceof net.minecraft.server.level.ServerPlayer p){standSoundPlayed=true;com.gx.velveteclipse.forge.FeedbackServer.furniture(p,"seat_stand",0,m_20185_(),m_20186_(),m_20189_());}
  if(!furniture.isEmpty()&&stand!=null&&m_9236_() instanceof net.minecraft.server.level.ServerLevel level&&passenger instanceof net.minecraft.server.level.ServerPlayer p){var safe=com.gx.velveteclipse.city.CityArrival.find(level,p,stand.f_82479_,stand.f_82480_,stand.f_82481_,3);if(safe!=null)return safe;}
  double angle=Math.toRadians(m_146908_());
  for(double turn:new double[]{0,Math.PI/2,-Math.PI/2,Math.PI}){double x=m_20185_()-Math.sin(angle+turn)*1.2,z=m_20189_()+Math.cos(angle+turn)*1.2,y=Math.floor(m_20186_());var box=passenger.m_20191_().m_82386_(x-passenger.m_20185_(),y-passenger.m_20186_(),z-passenger.m_20189_());if(m_9236_().m_45756_(passenger,box)&&!m_9236_().m_45756_(passenger,box.m_82386_(0,-.8,0)))return new Vec3(x,y,z);}
  return new Vec3(m_20185_(),Math.floor(m_20186_())+.7,m_20189_());}
}
