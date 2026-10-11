package com.gx.velveteclipse.darkhour;
import com.gx.velveteclipse.network.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.*;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;

/** A grounded landmark with a persisted rollback journal for every terrain edit. */
public final class TartarusService {
 public static final class State extends SavedData {
  public int x,y=Integer.MIN_VALUE,z,height,drop,index,attempt,exteriorRevision=TartarusExterior.REVISION;public boolean removing;
  public String owner="",anchor="";public final List<Long> placed=new ArrayList<>();List<TartarusLayout.Cell> cells;
  public final Map<Long,BlockState> terrain=new LinkedHashMap<>();
  public int column,scanY=Integer.MIN_VALUE,scanBottom;
  public boolean leveled;
  public boolean ready(){return y!=Integer.MIN_VALUE;}
  List<TartarusLayout.Cell> cells(){if(cells==null)cells=TartarusExterior.cells();return cells;}
  public static State load(CompoundTag t){
   var s=new State();s.exteriorRevision=t.m_128451_("exteriorRevision");s.owner=t.m_128461_("owner");s.anchor=t.m_128461_("anchor");
   if(t.m_128471_("ready")){s.x=t.m_128451_("x");s.y=t.m_128451_("y");s.z=t.m_128451_("z");s.height=t.m_128451_("height");s.drop=t.m_128451_("drop");s.index=t.m_128451_("index");s.removing=t.m_128471_("removing");
    if(t.m_128451_("format")>=2)for(long pos:t.m_128467_("placed"))s.placed.add(pos);
    else {var old=TartarusLayout.legacy(s.height,s.drop);for(int i=0;i<Math.min(s.index,old.size());i++){var c=old.get(i);s.placed.add(new BlockPos(s.x+c.x(),s.y+c.y(),s.z+c.z()).m_121878_());}s.removing=true;}
    if(t.m_128451_("format")>=3){s.column=t.m_128451_("column");s.scanY=t.m_128451_("scanY");s.scanBottom=t.m_128451_("scanBottom");s.leveled=t.m_128471_("leveled");var journal=t.m_128437_("terrain",10);for(int i=0;i<journal.size();i++){var entry=journal.m_128728_(i);s.terrain.put(entry.m_128454_("pos"),NbtUtils.m_247651_(BuiltInRegistries.f_256975_.m_255303_(),entry.m_128469_("state")));}}
    else s.removing=true; // Remove the airborne 0.6.14 tower before grounding the new one.
    if(s.exteriorRevision!=TartarusExterior.REVISION)s.removing=true;
   }return s;
  }
  @Override public CompoundTag m_7176_(CompoundTag t){t.m_128405_("format",3);t.m_128405_("exteriorRevision",exteriorRevision);t.m_128379_("ready",ready());t.m_128405_("x",x);t.m_128405_("y",y);t.m_128405_("z",z);t.m_128405_("height",height);t.m_128405_("drop",drop);t.m_128405_("index",index);t.m_128379_("removing",removing);t.m_128359_("owner",owner);t.m_128359_("anchor",anchor);t.m_128428_("placed",placed);t.m_128405_("column",column);t.m_128405_("scanY",scanY);t.m_128405_("scanBottom",scanBottom);t.m_128379_("leveled",leveled);var journal=new ListTag();for(var e:terrain.entrySet()){var entry=new CompoundTag();entry.m_128356_("pos",e.getKey());entry.m_128365_("state",NbtUtils.m_129202_(e.getValue()));journal.add(entry);}t.m_128365_("terrain",journal);return t;}
  private void clear(){y=Integer.MIN_VALUE;height=drop=index=attempt=column=0;scanY=Integer.MIN_VALUE;leveled=false;cells=null;placed.clear();terrain.clear();anchor="";removing=false;m_77762_();}
 }
 public static State data(ServerLevel l){return l.m_8895_().m_164861_(State::load,State::new,"velvet_eclipse_tartarus");}
 private static int ground(ServerLevel l,int x,int z){return l.m_6924_(Heightmap.Types.WORLD_SURFACE,x,z);}
 private static ServerPlayer preferred(ServerLevel l,State s){
  var server=l.m_7654_();var host=server.m_236731_();var choices=new ArrayList<TartarusPlacement.Player>();
  for(var p:l.m_6907_())choices.add(new TartarusPlacement.Player(p.m_20148_(),host!=null&&host.getId().equals(p.m_20148_()),s.owner.equals(p.m_20148_().toString()),server.m_129944_(p.m_36316_())));
  UUID id=TartarusPlacement.preferred(choices);return l.m_6907_().stream().filter(p->p.m_20148_().equals(id)).findFirst().orElse(null);
 }
 private static int range(ServerPlayer p){int server=p.m_20194_().m_6846_().m_11312_();var tag=p.getPersistentData();return TartarusPlacement.range(server,tag.m_128441_("velvet_eclipse:view_chunks")?tag.m_128451_("velvet_eclipse:view_chunks"):4);}
 private static void findSite(ServerLevel l,State s,ServerPlayer p){
  // Four nearby candidates, never an expanding search beyond render distance.
  int px=(int)Math.floor(p.m_20185_()),pz=(int)Math.floor(p.m_20189_());
  var at=TartarusPlacement.candidate(px,pz,range(p),s.attempt++);int x=at.x(),z=at.z();var heights=new ArrayList<Integer>();
  for(int dx=-32;dx<=32;dx+=4)for(int dz=-32;dz<=44;dz+=4){
   if(!l.m_6857_().m_61937_(new BlockPos(x+dx,64,z+dz)))return;
   heights.add(l.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,x+dx,z+dz));
  }
  heights.sort(Integer::compare);int base=Math.min(heights.get(heights.size()/2),l.m_151558_()-TartarusExterior.HEIGHT),height=TartarusExterior.HEIGHT;if(base<=l.m_141937_()+2)return;
  int drop=1;
  // Avoid occupied containers/machines entirely: no block-entity inventories are ever destroyed.
  for(int dx=-32;dx<=32;dx++)for(int dz=-32;dz<=42;dz++)for(int y=Math.min(base-1,ground(l,x+dx,z+dz)-1);y<ground(l,x+dx,z+dz);y++)if(l.m_7702_(new BlockPos(x+dx,y,z+dz))!=null)return;
  s.exteriorRevision=TartarusExterior.REVISION;s.x=x;s.y=base;s.z=z;s.height=height;s.drop=drop;s.index=0;s.cells=TartarusExterior.cells();s.anchor=p.m_20148_().toString();s.m_77762_();
  // Move anybody in the excavation to a journaled safe apron before clearing/filling.
  for(var player:l.m_6907_())if(Math.abs(player.m_20185_()-x)<=32&&player.m_20189_()>=z-32&&player.m_20189_()<=z+42){
   for(int dx=-1;dx<=1;dx++)for(int dz=30;dz<=32;dz++){edit(l,s,new BlockPos(x+dx,base-1,z+dz),DarkHourRegistry.STONE.get().m_49966_());for(int dy=0;dy<=3;dy++)edit(l,s,new BlockPos(x+dx,base+dy,z+dz),Blocks.f_50016_.m_49966_());}
   player.m_6021_(x+.5,base+.2,z+31.5);player.f_19789_=0;
  }
  for(var player:l.m_6907_())player.m_5661_(com.gx.velveteclipse.localization.ModText.component("§aDark Hour • O Tártaro surge perto de "+p.m_36316_().getName()+": "+x+", "+(base+1)+", "+(z+TartarusLayout.FRONT)+". /locate tartarus"),false);
 }
 private static boolean edit(ServerLevel l,State s,BlockPos pos,BlockState next){
  var old=l.m_8055_(pos);if(old.equals(next))return true;if(l.m_7702_(pos)!=null)return false;
  s.terrain.putIfAbsent(pos.m_121878_(),old);l.m_7731_(pos,next,2);return true;
 }
 private static void levelTerrain(ServerLevel l,State s){
  int budget=1000;
  while(s.column<65*75&&budget>0){int x=s.x+s.column%65-32,z=s.z+s.column/65-32;
   if(s.scanY==Integer.MIN_VALUE){s.scanY=Math.max(s.y-1,ground(l,x,z)-1);s.scanBottom=Math.max(l.m_141937_(),Math.min(s.y-2,ground(l,x,z)-1));}
   int bottom=s.scanBottom;
   while(s.scanY>=bottom&&budget-->0){var pos=new BlockPos(x,s.scanY,z);var state=s.scanY>=s.y?Blocks.f_50016_.m_49966_():DarkHourRegistry.STONE.get().m_49966_();
    if(!edit(l,s,pos,state)){beginRemoval(l,s);return;}s.scanY--;
   }
   if(s.scanY<bottom){s.column++;s.scanY=Integer.MIN_VALUE;}
  }
  s.leveled=s.column>=65*75;s.m_77762_();
 }
 private static void beginRemoval(ServerLevel l,State s){
  s.removing=true;s.m_77762_();
  int exitZ=s.z+TartarusExterior.MAX_Z+3;
  for(var p:l.m_6907_())if(Math.abs(p.m_20185_()-s.x)<32&&p.m_20189_()>s.z-32&&p.m_20189_()<exitZ){
   var feet=p.m_20183_();boolean supported=DarkHourRegistry.temporary(l.m_8055_(feet).m_60734_())||DarkHourRegistry.temporary(l.m_8055_(feet.m_7495_()).m_60734_());
   if(supported){p.m_6021_(s.x+.5,ground(l,s.x,exitZ)+.1,exitZ+.5);p.f_19789_=0;}
  }
 }
 public static int owner(CommandSourceStack source,ServerPlayer player){var s=data(source.m_81377_().m_129783_());s.owner=player.m_20148_().toString();s.m_77762_();source.m_288197_(()->com.gx.velveteclipse.localization.ModText.component("Prioridade do Tártaro configurada para "+player.m_36316_().getName()+". O anfitrião de mundo local continua tendo prioridade."),false);return 1;}
 public static int locate(CommandSourceStack source){
  var level=source.m_81377_().m_129783_();var s=data(level);
  if(!s.ready()||s.removing){String message=DarkHourService.active(level)?"O Tártaro está se posicionando perto do anfitrião/jogador. Tente novamente em alguns segundos; é necessário espaço abaixo do teto do mundo.":"O Tártaro surge durante a Dark Hour. Use /time set dark_hour.";source.m_288197_(()->com.gx.velveteclipse.localization.ModText.component(message),false);return 0;}
  int x=s.x,y=s.y+1,z=s.z+TartarusLayout.FRONT;var from=source.m_81371_();int distance=(int)Math.hypot(x-from.f_82479_,z-from.f_82481_);
  var coords=com.gx.velveteclipse.localization.ModText.component("["+x+", "+y+", "+z+"]").m_130938_(style->style.m_131142_(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/tp @s "+x+" "+y+" "+z)));
  source.m_288197_(()->com.gx.velveteclipse.localization.ModText.component("Tártaro no Overworld: ").m_7220_(coords).m_130946_(" • "+distance+" blocos. Entrada da torre."),false);return Math.max(1,distance);
 }
 public static void tick(ServerLevel l){
  var s=data(l);boolean active=DarkHourService.active(l);long tick=l.m_46467_();var preferred=preferred(l,s);
  if(active&&s.ready()&&!s.removing&&preferred!=null&&tick%20==0){
   boolean atTower=Math.abs(preferred.m_20185_()-s.x)<=34&&Math.abs(preferred.m_20189_()-s.z)<=34;
   if(!s.anchor.equals(preferred.m_20148_().toString())||(!atTower&&!TartarusPlacement.visible((int)preferred.m_20185_(),(int)preferred.m_20189_(),s.x,s.z,range(preferred))))beginRemoval(l,s);
  }
  if(!active&&s.ready()&&!s.removing)beginRemoval(l,s);
  if(s.removing){
   beginRemoval(l,s);
   int budget=900;while(!s.placed.isEmpty()&&budget-->0){var pos=BlockPos.m_122022_(s.placed.remove(s.placed.size()-1));if(DarkHourRegistry.temporary(l.m_8055_(pos).m_60734_()))l.m_7731_(pos,Blocks.f_50016_.m_49966_(),3);}
   if(s.placed.isEmpty()){var journal=s.terrain.entrySet().iterator();while(journal.hasNext()&&budget-->0){var e=journal.next();var pos=BlockPos.m_122022_(e.getKey());var current=l.m_8055_(pos);if(current.m_60795_()||DarkHourRegistry.temporary(current.m_60734_()))l.m_7731_(pos,e.getValue(),2);journal.remove();}}
   s.m_77762_();if(s.placed.isEmpty()&&s.terrain.isEmpty())s.clear();
  }
  if(active&&!s.ready()&&preferred!=null&&tick%10==0)findSite(l,s,preferred);
  if(!s.ready())return;
  var cells=s.cells();
  if(active&&!s.removing&&!s.leveled)levelTerrain(l,s);
  if(active&&!s.removing&&s.leveled&&s.index<cells.size()){
   int layer=cells.get(s.index).y(),budget=600;
   while(s.index<cells.size()&&cells.get(s.index).y()<=layer&&budget-->0){var c=cells.get(s.index++);var pos=new BlockPos(s.x+c.x(),s.y+c.y(),s.z+c.z());
    var state=TartarusExterior.state(c);
    if(l.m_6857_().m_61937_(pos)&&edit(l,s,pos,state)&&s.terrain.containsKey(pos.m_121878_())){s.placed.add(pos.m_121878_());TartarusExterior.configure(l,pos,c);}
   }s.m_77762_();
  }
  if(tick%20==0)for(var p:l.m_6907_())ModNetwork.sendPresentation(p,new DarkHourS2C(s.x,s.y,s.z,s.height,s.index==0?0:cells.get(Math.min(cells.size(),s.index)-1).y()+1,active&&!s.removing));
 }
}
