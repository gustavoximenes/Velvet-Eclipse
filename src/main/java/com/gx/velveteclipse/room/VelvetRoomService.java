package com.gx.velveteclipse.room;
import com.gx.velveteclipse.forge.BattleService;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.*;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class VelvetRoomService {
 public static final ResourceKey<Level> DIMENSION=ResourceKey.m_135785_(Registries.f_256858_,new ResourceLocation("velvet_eclipse","velvet_room"));
 private static final String RETURN="velvet_eclipse:room_return",COOLDOWN="velvet_eclipse:room_cooldown";
 public static boolean inside(Object p){return p instanceof net.minecraft.world.entity.Entity e&&e.m_9236_().m_46472_().equals(DIMENSION);}
 private static boolean available(ServerPlayer p){return BattleService.current(p)==null&&!com.gx.velveteclipse.exploration.ExplorationService.blocksBattleStart(p)&&!p.m_20159_();}
 private static void tell(ServerPlayer p,String message){p.m_5661_(Component.m_237113_(message),false);}
 public static void summonDoor(ServerPlayer p){
  if(!available(p)){tell(p,"Finalize a ação atual e levante-se antes de usar a Velvet Key.");return;}
  if(inside(p)){tell(p,"A porta de saída está atrás das cadeiras.");return;}
  if(com.gx.velveteclipse.darkhour.TartarusDungeonService.inside(p)){tell(p,"Use as portas fixas da Velvet Room no Tártaro.");return;}
  long now=System.currentTimeMillis();var tag=p.getPersistentData();if(now-tag.m_128454_(COOLDOWN)<1500)return;
  Direction facing=p.m_6350_().m_122424_();BlockPos origin=p.m_20183_();
  for(int range:new int[]{3,2,4})for(int dy:new int[]{0,1,-1}){
   BlockPos at=step(origin,p.m_6350_(),range).m_7918_(0,dy,0);var level=p.m_284548_();
   if(!level.m_8055_(at).m_60795_()||!level.m_8055_(at.m_7494_()).m_60795_()||!level.m_6857_().m_61937_(at))continue;
   var state=VelvetRoomRegistry.DOOR.get().m_49966_().m_61124_(DoorBlock.f_52726_,facing);
   if(!state.m_60710_(level,at))continue;placeDoor(level,at,facing);level.m_186460_(at,VelvetRoomRegistry.DOOR.get(),600);tag.m_128356_(COOLDOWN,now);tell(p,"A porta da Velvet Room apareceu por 30 segundos. Abra-a e atravesse.");return;
  }tell(p,"É preciso chão firme e dois blocos livres à sua frente.");
 }
 private static BlockPos step(BlockPos p,Direction d,int n){for(int i=0;i<n;i++)p=p.m_121945_(d);return p;}
 public static void placeDoor(ServerLevel level,BlockPos at,Direction facing){var state=VelvetRoomRegistry.DOOR.get().m_49966_().m_61124_(DoorBlock.f_52726_,facing);level.m_7731_(at,state.m_61124_(DoorBlock.f_52730_,DoubleBlockHalf.LOWER),3);level.m_7731_(at.m_7494_(),state.m_61124_(DoorBlock.f_52730_,DoubleBlockHalf.UPPER),3);}
 public static void cross(ServerPlayer p,BlockPos door){
  if(!available(p))return;var data=p.getPersistentData();long now=System.currentTimeMillis();if(now-data.m_128454_(COOLDOWN)<1800)return;
  if(inside(p)){leave(p);return;}var room=p.m_20194_().m_129880_(DIMENSION);if(room==null){tell(p,"A dimensão não foi carregada. Reinicie o mundo com o mod instalado.");return;}
  build(room);var back=new CompoundTag();back.m_128359_("dimension",p.m_9236_().m_46472_().m_135782_().toString());
  // Save the safe approach side, outside the doorway, to prevent a return loop.
  var direction=p.m_9236_().m_8055_(door).m_61143_(DoorBlock.f_52726_);var point=step(door,direction,2);
  back.m_128347_("x",point.m_123341_()+.5);back.m_128347_("y",Math.floor(p.m_20186_()));back.m_128347_("z",point.m_123343_()+.5);back.m_128350_("yaw",p.m_146908_());data.m_128365_(RETURN,back);data.m_128356_(COOLDOWN,now);
  BlockPos seat=null;for(int[] xyz:VelvetRoomLayout.seats()){var at=new BlockPos(xyz[0],xyz[1],xyz[2]);if(room.m_45976_(VelvetSeatEntity.class,new AABB(at).m_82400_(.3)).isEmpty()){seat=at;break;}}
  if(seat==null){p.m_8999_(room,.5,64,8.5,180,0);tell(p,"Bem-vindo à Velvet Room. As cadeiras estão ocupadas.");FusionRoomService.greetLater(p);return;}
  p.m_8999_(room,seat.m_123341_()+.5,64,seat.m_123343_()+.5,180,0);sit(p,seat);FusionRoomService.greetLater(p);tell(p,"Velvet Room • SHIFT para levantar. A porta atrás de você retorna ao mundo.");
 }
 public static boolean sit(ServerPlayer p,BlockPos pos){
  if(inside(p)&&pos.equals(new BlockPos(0,64,-5)))return false;
  if(!available(p)||p.m_20275_(pos.m_123341_()+.5,pos.m_123342_()+.5,pos.m_123343_()+.5)>16||!p.m_9236_().m_8055_(pos).m_60713_(VelvetRoomRegistry.CHAIR.get()))return false;
  if(!p.m_9236_().m_45976_(VelvetSeatEntity.class,new AABB(pos).m_82400_(.3)).isEmpty())return false;
  var seat=VelvetRoomRegistry.SEAT.get().m_20615_(p.m_9236_());if(seat==null)return false;seat.m_6034_(pos.m_123341_()+.5,pos.m_123342_()+.2875,pos.m_123343_()+.5);var facing=p.m_9236_().m_8055_(pos).m_61143_(HorizontalDirectionalBlock.f_54117_);float yaw=switch(facing){case SOUTH->0;case WEST->90;case EAST->-90;default->180;};seat.m_146922_(yaw);if(!p.m_284548_().m_7967_(seat))return false;p.m_146922_(yaw);p.m_5616_(yaw);p.m_5618_(yaw);boolean mounted=p.m_7998_(seat,true);if(!mounted)seat.m_146870_();return mounted;
 }
 private static void leave(ServerPlayer p){
  var tag=p.getPersistentData();var back=tag.m_128469_(RETURN);ServerLevel target=null;
  try{target=p.m_20194_().m_129880_(ResourceKey.m_135785_(Registries.f_256858_,new ResourceLocation(back.m_128461_("dimension"))));}catch(RuntimeException ignored){}
  if(target==null||target.m_46472_().equals(DIMENSION))target=p.m_20194_().m_129783_();
  if(target.m_46472_().equals(com.gx.velveteclipse.darkhour.TartarusDungeonService.DIMENSION)){com.gx.velveteclipse.darkhour.TartarusDungeonService.teleportToFloor(p,com.gx.velveteclipse.darkhour.TartarusInteriorLayout.floorAt(back.m_128459_("x")));return;}
  double x=back.m_128459_("x"),y=back.m_128459_("y"),z=back.m_128459_("z");if(!back.m_128441_("dimension")){var spawn=target.m_220360_();x=spawn.m_123341_()+.5;y=spawn.m_123342_();z=spawn.m_123343_()+.5;}
  var safe=safeExit(target,p,x,y,z);if(safe==null){tell(p,"A saída está bloqueada. Libere o espaço no mundo de origem.");return;}
  tag.m_128356_(COOLDOWN,System.currentTimeMillis());p.m_8999_(target,safe.f_82479_,safe.f_82480_,safe.f_82481_,back.m_128457_("yaw"),0);
 }
 private static Vec3 safeExit(ServerLevel level,ServerPlayer p,double x,double y,double z){for(int radius=0;radius<=3;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)for(int dy:new int[]{0,1,-1,2,-2}){var at=new Vec3(x+dx,y+dy,z+dz);var box=p.m_20191_().m_82386_(at.f_82479_-p.m_20185_(),at.f_82480_-p.m_20186_(),at.f_82481_-p.m_20189_());var feet=new BlockPos((int)Math.floor(at.f_82479_),(int)Math.floor(at.f_82480_),(int)Math.floor(at.f_82481_));if(level.m_6857_().m_61937_(feet)&&!level.m_8055_(feet).m_60713_(VelvetRoomRegistry.DOOR.get())&&level.m_8055_(feet).m_60819_().m_76178_()&&level.m_45756_(p,box)&&!level.m_45756_(p,box.m_82386_(0,-.8,0)))return at;}return null;}
 public static void build(ServerLevel level){
  var saved=level.m_8895_().m_164861_(Built::load,Built::new,"velvet_eclipse_room");if(saved.done){ensureIgor(level,saved);return;}
  var palette=Map.of("floor",VelvetRoomRegistry.FLOOR.get(),"wall",VelvetRoomRegistry.WALL.get(),"trim",VelvetRoomRegistry.TRIM.get(),"grille",VelvetRoomRegistry.GATE.get());
  for(var cell:VelvetRoomLayout.cells()){var state=palette.get(cell.material()).m_49966_();if(cell.material().equals("grille"))state=state.m_61124_(HorizontalDirectionalBlock.f_54117_,cell.x()==-7?Direction.EAST:cell.x()==7?Direction.WEST:Direction.SOUTH);level.m_7731_(new BlockPos(cell.x(),cell.y(),cell.z()),state,2);}
  level.m_7731_(new BlockPos(0,64,-2),VelvetRoomRegistry.TABLE.get().m_49966_(),2);
  for(int[] xyz:VelvetRoomLayout.seats())level.m_7731_(new BlockPos(xyz[0],xyz[1],xyz[2]),VelvetRoomRegistry.CHAIR.get().m_49966_(),2);
  level.m_7731_(new BlockPos(0,64,-5),VelvetRoomRegistry.CHAIR.get().m_49966_().m_61124_(HorizontalDirectionalBlock.f_54117_,Direction.SOUTH),2);
  placeDoor(level,new BlockPos(0,64,10),Direction.NORTH);saved.done=true;saved.m_77762_();ensureIgor(level,saved);
 }
 private static void ensureIgor(ServerLevel level,Built saved){
  if(!saved.igor.isEmpty())return;
  level.m_46745_(new BlockPos(0,64,-5));
  var existing=level.m_45976_(IgorEntity.class,new AABB(-2,63,-7,3,68,-2));
  if(!existing.isEmpty()){saved.igor=existing.get(0).m_20148_().toString();saved.m_77762_();return;}
  var igor=VelvetRoomRegistry.IGOR.get().m_20615_(level);if(igor==null)return;
  igor.m_6034_(.5,64.125,-4.5);igor.m_146922_(0);
  if(level.m_7967_(igor)){saved.igor=igor.m_20148_().toString();saved.m_77762_();}
 }
 private static final class Built extends SavedData {boolean done;String igor="";static Built load(CompoundTag tag){var b=new Built();b.done=tag.m_128471_("built");b.igor=tag.m_128461_("igor");return b;}@Override public CompoundTag m_7176_(CompoundTag tag){tag.m_128379_("built",done);tag.m_128359_("igor",igor);return tag;}}
}
