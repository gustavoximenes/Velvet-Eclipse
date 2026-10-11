package com.gx.velveteclipse.city;

import java.util.*;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.forge.BattleService;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Gekkoukan becomes Tartarus for the shared world night, with a persisted lossless rollback. */
@Mod.EventBusSubscriber(modid="velvet_eclipse")
public final class CityNightSchool {
    public static final int MIN_X=-51,MAX_X=51,MIN_Z=-137,MAX_Z=-65,BASE=64,CENTER_Z=-108;
    public static boolean contains(double x,double z){return x>=MIN_X&&x<=MAX_X+1&&z>=MIN_Z&&z<=MAX_Z+1;}
    public static final class State extends SavedData {
        int phase,column,y=-1,index,height,exteriorRevision=TartarusExterior.REVISION; // day, clearing, building, night, restoring
        final LinkedHashMap<Long,CompoundTag> journal=new LinkedHashMap<>();
        List<TartarusLayout.Cell> tower;
        List<TartarusLayout.Cell> tower(){if(tower==null)tower=TartarusExterior.cells();return tower;}
        static State load(CompoundTag tag){var s=new State();s.phase=tag.m_128451_("phase");s.column=tag.m_128451_("column");s.y=tag.m_128451_("y");s.index=tag.m_128451_("index");s.height=tag.m_128451_("height");s.exteriorRevision=tag.m_128451_("exteriorRevision");var rows=tag.m_128437_("journal",10);for(int i=0;i<rows.size();i++){var e=rows.m_128728_(i);s.journal.put(e.m_128454_("pos"),e);}if(s.phase!=0&&s.exteriorRevision!=TartarusExterior.REVISION)s.phase=4;return s;}
        @Override public CompoundTag m_7176_(CompoundTag tag){tag.m_128405_("phase",phase);tag.m_128405_("column",column);tag.m_128405_("y",y);tag.m_128405_("index",index);tag.m_128405_("height",height);tag.m_128405_("exteriorRevision",exteriorRevision);var rows=new ListTag();rows.addAll(journal.values());tag.m_128365_("journal",rows);return tag;}
    }
    public static State state(ServerLevel level){return level.m_8895_().m_164861_(State::load,State::new,"velvet_city_school_night");}
    public static boolean open(ServerPlayer p){return CityService.inside(p)&&state(p.m_284548_()).phase==3&&DarkHourService.active(p.m_9236_());}
    public static boolean unavailable(ServerPlayer p){return CityService.inside(p)&&state(p.m_284548_()).phase!=0;}
    private static void edit(ServerLevel level,State s,BlockPos pos,BlockState value){
        var old=level.m_8055_(pos);if(old.equals(value))return;long key=pos.m_121878_();
        if(!s.journal.containsKey(key)){var row=new CompoundTag();row.m_128356_("pos",key);row.m_128365_("state",NbtUtils.m_129202_(old));var be=level.m_7702_(pos);if(be!=null)row.m_128365_("entity",be.m_187480_());s.journal.put(key,row);s.m_77762_();}
        // Preserve inventories before removing their block entity; never drop duplicate contents.
        if(level.m_7702_(pos)!=null)level.m_46747_(pos);
        level.m_7731_(pos,value,2);
    }
    private static boolean evacuate(ServerLevel level){
        for(var p:level.m_6907_())if(contains(p.m_20185_(),p.m_20189_())&&BattleService.current(p)!=null)return false;
        for(var p:level.m_6907_())if(contains(p.m_20185_(),p.m_20189_())){p.m_8127_();p.m_8999_(level,.5,64,-36.5,180,0);p.f_19789_=0;}
        return true;
    }
    /** Returns true when it is safe for a city revision to rebuild the campus. */
    public static boolean tick(ServerLevel level,boolean cityReady){
        var s=state(level);boolean night=cityReady&&DarkHourService.active(level);
        if(s.phase==0&&!night)return true;
        if(s.phase!=0&&s.exteriorRevision!=TartarusExterior.REVISION){if(!evacuate(level))return false;s.phase=4;}
        if(s.phase==0){if(!evacuate(level))return false;s.phase=1;s.exteriorRevision=TartarusExterior.REVISION;s.height=TartarusExterior.HEIGHT;s.column=s.index=0;s.y=-1;s.tower=null;s.m_77762_();}
        if(!night&&s.phase!=4){if(!evacuate(level))return false;s.phase=4;s.m_77762_();}
        if(night&&s.phase==3)return false;
        if(s.phase!=3&&!evacuate(level))return false;
        int budget=1600;
        if(s.phase==1){
            int width=MAX_X-MIN_X+1,total=width*(MAX_Z-MIN_Z+1);
            while(s.column<total&&budget>0){int x=MIN_X+s.column%width,z=MIN_Z+s.column/width;if(s.y<0)s.y=Math.max(BASE,level.m_6924_(Heightmap.Types.WORLD_SURFACE,x,z)-1);
                while(s.y>=BASE&&budget-->0){var pos=new BlockPos(x,s.y--,z);if(!level.m_8055_(pos).m_60795_())edit(level,s,pos,Blocks.f_50016_.m_49966_());}
                if(s.y<BASE){s.column++;s.y=-1;}
            }if(s.column>=total)s.phase=2;
        }
        if(s.phase==2){var cells=s.tower();while(s.index<cells.size()&&budget-->0){var c=cells.get(s.index++);var pos=new BlockPos(c.x(),BASE+c.y(),CENTER_Z+c.z());edit(level,s,pos,TartarusExterior.state(c));TartarusExterior.configure(level,pos,c);}if(s.index>=cells.size())s.phase=3;}
        if(s.phase==4){var it=s.journal.entrySet().iterator();while(it.hasNext()&&budget-->0){restore(level,it.next().getValue());it.remove();}if(s.journal.isEmpty()){s.phase=s.column=s.index=0;s.y=-1;s.tower=null;}}
        s.m_77762_();return s.phase==0;
    }
    private static void restore(ServerLevel level,CompoundTag row){
        var pos=BlockPos.m_122022_(row.m_128454_("pos"));var original=NbtUtils.m_247651_(BuiltInRegistries.f_256975_.m_255303_(),row.m_128469_("state"));
        if(!level.m_8055_(pos).equals(original)){if(level.m_7702_(pos)!=null)level.m_46747_(pos);level.m_7731_(pos,original,2);}
        if(row.m_128441_("entity")){
            var payload=row.m_128469_("entity");var be=level.m_7702_(pos);
            // Replaying a persisted entry is safe even when its chunk already contains the restored state.
            if(be==null){be=net.minecraft.world.level.block.entity.BlockEntity.m_155241_(pos,original,payload);if(be!=null)level.m_151523_(be);}
            if(be!=null){be.m_142466_(payload);be.m_6596_();level.m_7260_(pos,original,original,3);}
        }
    }
    private static boolean protectedAt(net.minecraft.world.level.LevelAccessor level,BlockPos pos){return level instanceof ServerLevel server&&server.m_46472_().equals(CityService.DIMENSION)&&contains(pos.m_123341_(),pos.m_123343_())&&pos.m_123342_()>=BASE-1&&state(server).phase!=0;}
    @SubscribeEvent public static void breakBlock(net.minecraftforge.event.level.BlockEvent.BreakEvent e){if(protectedAt(e.getLevel(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public static void placeBlock(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent e){if(protectedAt(e.getLevel(),e.getPos()))e.setCanceled(true);}
    @SubscribeEvent public static void explosion(net.minecraftforge.event.level.ExplosionEvent.Detonate e){e.getAffectedBlocks().removeIf(p->protectedAt(e.getLevel(),p));}
    private CityNightSchool(){}
}
