package com.gx.velveteclipse.client.city;

import java.io.*;import java.util.*;import java.util.zip.GZIPInputStream;
import com.gx.velveteclipse.city.*;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.data.*;
import net.minecraftforge.client.model.geometry.*;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Baked into ordinary chunk buffers: correct local culling, depth and shader material passes. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class CityArchitectureModel implements IDynamicBakedModel {
    @SubscribeEvent public static void loaders(net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders e){e.register("city_architecture",(IGeometryLoader<Geometry>)(json,context)->new Geometry());}
    public record Geometry() implements IUnbakedGeometry<Geometry>{
        @Override public BakedModel bake(IGeometryBakingContext c,ModelBaker baker,java.util.function.Function<Material,TextureAtlasSprite> sprites,ModelState state,ItemOverrides overrides,ResourceLocation location){var atlas=new TextureAtlasSprite[36];for(int i=0;i<atlas.length;i++)atlas[i]=sprites.apply(c.getMaterial("m"+i));return new CityArchitectureModel(atlas,c.getTransforms());}
    }
    private static volatile float[] vertices;
    private final TextureAtlasSprite[] textures;private final ItemTransforms transforms;
    private record Quads(List<BakedQuad> opaque,List<BakedQuad> glass){}
    private final Map<Integer,Quads> cache=new LinkedHashMap<>(256,.75f,true);
    private CityArchitectureModel(TextureAtlasSprite[] textures,ItemTransforms transforms){this.textures=textures;this.transforms=transforms;}
    private static float[] vertices(){if(vertices==null)synchronized(CityArchitectureModel.class){if(vertices==null)try(var input=new DataInputStream(new GZIPInputStream(Objects.requireNonNull(CityArchitectureModel.class.getResourceAsStream("/assets/velvet_eclipse/city/architecture.bin.gz"))))){var last=CityArchitecture.tile(CityArchitecture.size()-1);var out=new float[last.offset()+last.length()];for(int i=0;i<out.length;i++)out[i]=input.readFloat();vertices=out;}catch(IOException e){throw new IllegalStateException(e);}}return vertices;}
    @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type){
        if(side!=null)return List.of();Integer id=data.get(CityArchitectureEntity.TILE);if(id==null||CityArchitecture.tile(id)==null)return List.of();
        Quads q;synchronized(cache){q=cache.computeIfAbsent(id,this::bake);while(cache.size()>2048)cache.remove(cache.keySet().iterator().next());}
        if(type==RenderType.m_110466_())return q.glass();if(type!=null)return q.opaque();var both=new ArrayList<>(q.opaque());both.addAll(q.glass());return both;
    }
    private Quads bake(int id){var tile=CityArchitecture.tile(id);var raw=vertices();var opaque=new ArrayList<BakedQuad>();var glass=new ArrayList<BakedQuad>();
        for(int at=tile.offset();at<tile.offset()+tile.length();at+=39){int mat=Math.max(0,Math.min(textures.length-1,Math.round(raw[at+9])));boolean transparent=mat==18;var texture=textures[mat];int[] data=new int[32];
            for(int i=0;i<4;i++){int s=at+Math.min(i,2)*13,d=i*8;for(int j=0;j<3;j++)data[d+j]=Float.floatToRawIntBits(raw[s+j]);int r=Math.round(raw[s+6]*255),g=Math.round(raw[s+7]*255),b=Math.round(raw[s+8]*255);data[d+3]=(transparent?0x72:0xff)<<24|b<<16|g<<8|r;data[d+4]=Float.floatToRawIntBits(texture.m_118367_(Math.max(.001,Math.min(15.999,raw[s+11]*16))));data[d+5]=Float.floatToRawIntBits(texture.m_118393_(Math.max(.001,Math.min(15.999,raw[s+12]*16))));int light=raw[s+10]>=.2?Math.min(15,Math.round(9+raw[s+10]*5)):0;data[d+6]=light<<4;data[d+7]=(Math.round(raw[s+3]*127)&255)|((Math.round(raw[s+4]*127)&255)<<8)|((Math.round(raw[s+5]*127)&255)<<16);}
            float x=raw[at+3],y=raw[at+4],z=raw[at+5];Direction face=Math.abs(y)>=Math.max(Math.abs(x),Math.abs(z))?(y>=0?Direction.UP:Direction.DOWN):Math.abs(x)>=Math.abs(z)?(x>=0?Direction.EAST:Direction.WEST):(z>=0?Direction.SOUTH:Direction.NORTH);
            (transparent?glass:opaque).add(new BakedQuad(data,-1,face,texture,raw[at+10]<.2,false));
        }return new Quads(List.copyOf(opaque),List.copyOf(glass));
    }
    @Override public ChunkRenderTypeSet getRenderTypes(BlockState state,RandomSource random,ModelData data){return ChunkRenderTypeSet.of(RenderType.m_110451_(),RenderType.m_110466_());}
    @Override public boolean m_7541_(){return false;}@Override public boolean m_7539_(){return true;}@Override public boolean m_7547_(){return true;}@Override public boolean m_7521_(){return false;}
    @Override public TextureAtlasSprite m_6160_(){return textures[2];}@Override public ItemOverrides m_7343_(){return ItemOverrides.f_111734_;}@Override public ItemTransforms m_7442_(){return transforms;}
}
