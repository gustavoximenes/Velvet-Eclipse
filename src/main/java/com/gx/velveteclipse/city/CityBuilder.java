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
    private static CityPlan.Plan legacy(int revision){if(legacy==null||legacy.revision()!=Math.max(1,revision)){
        legacy=CityPlan.legacy(revision);
        if(revision<2){var second=CityPlan.legacy(2);var cells=new ArrayList<int[]>(Arrays.asList(legacy.cells()));cells.addAll(Arrays.asList(second.cells()));legacy=new CityPlan.Plan(1,cells.toArray(int[][]::new),legacy.meshes(),legacy.places(),legacy.doors(),second.palette(),legacy.buildings(),legacy.routes());}
        if(revision==3||revision==4){
            legacyDoorTile(revision,new int[]{Integer.MIN_VALUE,0,0},-9);
            var cells=new ArrayList<int[]>(Arrays.asList(legacy.cells()));var seen=new HashSet<String>();
            for(var c:cells)seen.add(c[0]+","+c[1]+","+c[2]);
            for(var d:oldFurniture.doors())for(var c:d.cells())if(seen.add(c[0]+","+c[1]+","+c[2]))cells.add(new int[]{c[0],c[1],c[2],40,c[4]});
            legacy=new CityPlan.Plan(revision,cells.toArray(int[][]::new),legacy.meshes(),legacy.places(),legacy.doors(),legacy.palette(),legacy.buildings(),legacy.routes());
        }
        legacyChunks=null;
    }return legacy;}
    private static List<BlockPos> legacyChunks(int revision){
        if(legacyChunks==null){var chunks=new LinkedHashSet<BlockPos>();for(var m:legacy(revision).meshes())chunks.add(new BlockPos(m.x()>>4,0,m.z()>>4));legacyChunks=List.copyOf(chunks);}
        return legacyChunks;
    }
    private static CityFurniture.Catalogue oldFurniture;
    private static int oldFurnitureRevision;
    private static boolean legacyDoorTile(int revision,int[] cell,int id){
        if(revision!=3&&revision!=4)return false;
        if(oldFurniture==null||oldFurnitureRevision!=revision)try(var in=new java.util.zip.GZIPInputStream(java.util.Objects.requireNonNull(CityBuilder.class.getResourceAsStream("/data/velvet_eclipse/city/legacy-furniture-v"+revision+".json.gz")))){
            oldFurniture=new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),CityFurniture.Catalogue.class);oldFurnitureRevision=revision;
        }catch(java.io.IOException e){throw new IllegalStateException(e);}
        return oldFurniture.doors().stream().flatMap(d->Arrays.stream(d.cells())).anyMatch(c->c[0]==cell[0]&&c[1]==cell[1]&&c[2]==cell[2]&&(c[4]==id||c[5]==id));
    }
    public static boolean beginRevision(CityState state){
        if(state.choice!=1||state.revision>=CityPlan.get().revision()||state.rebuilding&&state.targetRevision==CityPlan.get().revision())return false;
        state.targetRevision=CityPlan.get().revision();state.rebuilding=true;state.ready=false;state.cursor=state.cleanupCursor=state.conflicts=0;state.m_77762_();return true;
    }
    public static int progress(CityState state){
        if(state.choice!=1)return 0;if(state.ready)return 100;
        int old=state.rebuilding?legacy(state.revision).cells().length+legacyChunks(state.revision).size():0;
        return Math.min(99,(int)(100L*(state.cursor+state.cleanupCursor)/(CityPlan.get().cells().length+old)));
    }
    private static boolean clearLegacy(ServerLevel level,CityState state){
        int[][] cells=legacy(state.revision).cells();int limit=Math.min(cells.length,state.cleanupCursor+1400);
        while(state.cleanupCursor<limit){
            int[] c=cells[state.cleanupCursor++];var at=new BlockPos(c[0],c[1],c[2]);var old=level.m_8055_(at);
            boolean owned=c[3]==30||c[3]==31?old.m_60713_(VelvetRoomRegistry.DOOR.get()):old.equals(CityBlocks.legacyState(c,legacy(state.revision)));
            if(c[3]==40)owned=level.m_7702_(at) instanceof CityArchitectureEntity tile&&(tile.id()==c[4]||legacyDoorTile(state.revision,c,tile.id()));
            if(owned)level.m_7731_(at,Blocks.f_50016_.m_49966_(),2);
        }
        int remaining=2;
        while(state.cleanupCursor>=cells.length&&state.cleanupCursor<cells.length+legacyChunks(state.revision).size()&&remaining-->0){
            var chunk=legacyChunks(state.revision).get(state.cleanupCursor-cells.length);
            for(var be:new ArrayList<>(level.m_6325_(chunk.m_123341_(),chunk.m_123343_()).m_62954_().values())){
                if(!(be instanceof TartarusMeshBlockEntity mesh))continue;
                var spec=mesh.getModelData().get(TartarusMeshBlockEntity.MESH);
                if(spec!=null&&!spec.instances().isEmpty()&&spec.instances().stream().allMatch(i->i.model().startsWith("city_")))
                    level.m_7731_(be.m_58899_(),Blocks.f_50016_.m_49966_(),2);
            }
            state.cleanupCursor++;
        }
        state.m_77762_();return state.cleanupCursor>=cells.length+legacyChunks(state.revision).size();
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
            if(c[3]==40&&level.m_7702_(at) instanceof CityArchitectureEntity existing&&existing.id()!=c[4]){state.conflicts++;continue;}
            level.m_7731_(at,desired,2);
            if(c[3]==40&&level.m_7702_(at) instanceof CityArchitectureEntity tile)tile.configure(c[4]);
        }
        boolean done=state.cursor>=cells.length;
        if(done){state.revision=CityPlan.get().revision();state.rebuilding=false;}
        state.m_77762_();return done;
    }
    private CityBuilder(){}
}
