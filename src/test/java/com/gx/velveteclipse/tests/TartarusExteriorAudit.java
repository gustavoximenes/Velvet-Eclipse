package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.city.*;
import com.gx.velveteclipse.darkhour.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;

/** Native exported portal, walking surface, bounds and persisted version migration. */
public final class TartarusExteriorAudit {
    static int checks;static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        var cells=TartarusExterior.cells();var occupied=new HashSet<String>();var collision=new HashMap<String,List<AABB>>();int portals=0,lit=0,highest=-1,last=-2;
        for(var c:cells){
            check(occupied.add(c.x()+","+c.y()+","+c.z()),"No duplicate architecture/portal cells");
            check(c.y()>=last,"Stable bottom-up construction order");last=c.y();highest=Math.max(highest,c.y());
            check(c.y()>=-1&&c.y()<TartarusExterior.HEIGHT,"Native model fits the build ceiling");
            check(CityNightSchool.contains(c.x(),c.z()+CityNightSchool.CENTER_Z),"Entire tower and path inside city rollback bounds");
            check(c.x()>=TartarusExterior.MIN_X&&c.x()<=TartarusExterior.MAX_X&&c.z()>=TartarusExterior.MIN_Z&&c.z()<=TartarusExterior.MAX_Z,"Inside journaled terrain footprint");
            if(c.material()==4){portals++;check(c.z()==18&&Math.abs(c.x()+.5)<3&&c.y()>=1&&c.y()<=7,"Portal inside the authored arch");continue;}
            var tile=CityArchitecture.tile(c.material()-TartarusExterior.TILE);check(tile!=null,"Native fragment exists");if(tile.light()>0)lit++;
            if(Math.abs(c.x())<4&&c.y()<5&&c.z()>=17){var rows=collision.computeIfAbsent(c.x()+","+c.y()+","+c.z(),k->new ArrayList<>());for(var b:tile.boxes())rows.add(new AABB(c.x()+b[0],c.y()+b[1],c.z()+b[2],c.x()+b[3],c.y()+b[4],c.z()+b[5]));}
        }
        check(highest==237&&portals==42&&lit>1000,"Full approved tower and luminous green windows");
        // Actual player-sized collision, not a visual-only opening or a block occupancy guess.
        for(double x:new double[]{-2,0,2})for(double z=18.5;z<42;z+=.25){
            var bounds=new AABB(x-.3,.183,z-.3,x+.3,1.983,z+.3);
            boolean blocked=false,supported=false;var feet=new AABB(x-.28,-.02,z-.28,x+.28,.184,z+.28);
            for(int xx=(int)Math.floor(x-.3);xx<=Math.floor(x+.3);xx++)for(int zz=(int)Math.floor(z-.3);zz<=Math.floor(z+.3);zz++)for(int yy=-1;yy<3;yy++)for(var b:collision.getOrDefault(xx+","+yy+","+zz,List.of())){blocked|=b.m_82381_(bounds);supported|=b.m_82381_(feet);}
            check(!blocked,"No obstruction on the full entrance walkway at "+x+","+z);check(supported,"No holes under the entrance walkway at "+x+","+z);
        }
        var old=new CompoundTag();old.m_128379_("ready",true);old.m_128405_("format",3);old.m_128405_("height",240);old.m_128405_("drop",1);old.m_128428_("placed",List.of(17L,21L));
        var legacy=TartarusService.State.load(old);check(legacy.removing&&legacy.placed.equals(List.of(17L,21L)),"Old Overworld tower is rolled back using its original positions");
        old.m_128405_("exteriorRevision",TartarusExterior.REVISION);var current=TartarusService.State.load(old);check(!current.removing,"Current model resumes without unnecessary removal");check(current.m_7176_(new CompoundTag()).m_128451_("exteriorRevision")==TartarusExterior.REVISION,"Model revision survives save");
        var load=CityNightSchool.State.class.getDeclaredMethod("load",CompoundTag.class);load.setAccessible(true);var cityOld=new CompoundTag();cityOld.m_128405_("phase",3);
        var city=(CityNightSchool.State)load.invoke(null,cityOld);var saved=city.m_7176_(new CompoundTag());check(saved.m_128451_("exteriorRevision")!=TartarusExterior.REVISION&&saved.m_128451_("phase")==4,"Legacy city state restores the previous journal before rebuilding");
        Path assets=Path.of("src/main/resources/assets/velvet_eclipse");
        for(String lang:List.of("en_us","pt_br")){var json=JsonParser.parseString(Files.readString(assets.resolve("lang/"+lang+".json"))).getAsJsonObject();check(json.has("block.velvet_eclipse.tartarus_exterior"),"Creative tower masonry named in "+lang);}
        check(com.gx.velveteclipse.room.CreativeCategory.of("tartarus_exterior",true)==com.gx.velveteclipse.room.CreativeCategory.BLOCKS,"New native block appears in the blocks creative category");
        check(Files.exists(assets.resolve("models/item/tartarus_exterior.json")),"Creative item has a model");
        var emission=javax.imageio.ImageIO.read(assets.resolve("textures/block/tower_glow_s.png").toFile());check((emission.getRGB(0,0)>>>24)==220,"Green material exposes LabPBR emission");
        System.out.println("PASS Tartarus exterior: "+checks+" checks; "+cells.size()+" local cells, "+lit+" emissive blocks, 42 portal cells, real player entrance clearance and saved-world migration");
    }
}
