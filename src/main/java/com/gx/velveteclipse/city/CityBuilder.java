package com.gx.velveteclipse.city;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import com.gx.velveteclipse.darkhour.TartarusMeshBlockEntity;
import com.gx.velveteclipse.room.*;

/** Restartable native construction. Revision 1 cleanup only touches its generated cells and city mesh entities. */
public final class CityBuilder {
    private static CityPlan.Plan legacy;
    private static List<BlockPos> legacyChunks;
    private static CityPlan.Plan legacy(){if(legacy==null)legacy=CityPlan.legacy();return legacy;}
    private static List<BlockPos> legacyChunks(){
        if(legacyChunks==null){var chunks=new LinkedHashSet<BlockPos>();for(var m:legacy().meshes())chunks.add(new BlockPos(m.x()>>4,0,m.z()>>4));legacyChunks=List.copyOf(chunks);}
        return legacyChunks;
    }
    public static boolean beginRevision(CityState state){
        if(state.choice!=1||state.revision>=CityPlan.get().revision()||state.rebuilding)return false;
        state.rebuilding=true;state.ready=false;state.cursor=state.cleanupCursor=state.conflicts=0;state.m_77762_();return true;
    }
    public static int progress(CityState state){
        if(state.choice!=1)return 0;if(state.ready)return 100;
        int old=state.rebuilding?legacy().cells().length+legacyChunks().size():0;
        return Math.min(99,(int)(100L*(state.cursor+state.cleanupCursor)/(CityPlan.get().cells().length+old)));
    }
    private static boolean clearLegacy(ServerLevel level,CityState state){
        int[][] cells=legacy().cells();int limit=Math.min(cells.length,state.cleanupCursor+1400);
        while(state.cleanupCursor<limit){
            int[] c=cells[state.cleanupCursor++];var at=new BlockPos(c[0],c[1],c[2]);var old=level.m_8055_(at);
            boolean owned=c[3]==30||c[3]==31?old.m_60713_(VelvetRoomRegistry.DOOR.get()):old.equals(CityBlocks.state(c));
            if(owned)level.m_7731_(at,Blocks.f_50016_.m_49966_(),2);
        }
        int remaining=2;
        while(state.cleanupCursor>=cells.length&&state.cleanupCursor<cells.length+legacyChunks().size()&&remaining-->0){
            var chunk=legacyChunks().get(state.cleanupCursor-cells.length);
            for(var be:new ArrayList<>(level.m_6325_(chunk.m_123341_(),chunk.m_123343_()).m_62954_().values())){
                if(!(be instanceof TartarusMeshBlockEntity mesh))continue;
                var spec=mesh.getModelData().get(TartarusMeshBlockEntity.MESH);
                if(spec!=null&&!spec.instances().isEmpty()&&spec.instances().stream().allMatch(i->i.model().startsWith("city_")))
                    level.m_7731_(be.m_58899_(),Blocks.f_50016_.m_49966_(),2);
            }
            state.cleanupCursor++;
        }
        state.m_77762_();return state.cleanupCursor>=cells.length+legacyChunks().size();
    }
    public static boolean step(ServerLevel level,CityState state){
        if(state.rebuilding&&!clearLegacy(level,state))return false;
        int[][] cells=CityPlan.get().cells();int limit=Math.min(cells.length,state.cursor+1200);
        while(state.cursor<limit){
            int[] c=cells[state.cursor++];var at=new BlockPos(c[0],c[1],c[2]);var old=level.m_8055_(at);
            if(c[3]==31)continue;
            // A player's different block is preserved during migration; it is never blindly erased.
            if(c[3]==30){
                if(old.m_60795_()&&level.m_8055_(at.m_7494_()).m_60795_())VelvetRoomService.placeDoor(level,at,Direction.NORTH);
                else if(!old.m_60713_(VelvetRoomRegistry.DOOR.get()))state.conflicts++;
                continue;
            }
            var desired=CityBlocks.state(c);
            if(!old.m_60795_()&&!old.equals(desired)){state.conflicts++;continue;}
            level.m_7731_(at,desired,2);
        }
        boolean done=state.cursor>=cells.length;
        if(done){state.revision=CityPlan.get().revision();state.rebuilding=false;}
        state.m_77762_();return done;
    }
    private CityBuilder(){}
}
