package com.gx.velveteclipse.client.tartarus;
import java.util.*;
import com.google.gson.*;
import com.gx.velveteclipse.darkhour.*;
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

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class TartarusMeshModel implements IDynamicBakedModel {
 @SubscribeEvent public static void loaders(net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders e){e.register("tartarus_mesh",(IGeometryLoader<Geometry>)(json,context)->new Geometry(json.has("mesh")?json.get("mesh").getAsString():"empty"));}
 public record Geometry(String mesh) implements IUnbakedGeometry<Geometry>{
  @Override public BakedModel bake(IGeometryBakingContext c,ModelBaker baker,java.util.function.Function<Material,TextureAtlasSprite> sprites,ModelState state,ItemOverrides overrides,ResourceLocation location){var atlas=new TextureAtlasSprite[18];for(int i=0;i<18;i++)atlas[i]=sprites.apply(c.getMaterial("m"+i));return new TartarusMeshModel(mesh,atlas,c.getTransforms(),state.m_6189_());}
 }
 private final String mesh;private final TextureAtlasSprite[] textures;private final ItemTransforms transforms;private final com.mojang.math.Transformation rotation;
 private final Map<TartarusMeshBlockEntity.Spec,List<BakedQuad>> cache=new LinkedHashMap<>(128,.75f,true);private int cachedQuads;
 private TartarusMeshModel(String mesh,TextureAtlasSprite[] textures,ItemTransforms transforms,com.mojang.math.Transformation rotation){this.mesh=mesh;this.textures=textures;this.transforms=transforms;this.rotation=rotation;}
 @Override public List<BakedQuad> getQuads(BlockState state,Direction side,RandomSource random,ModelData data,RenderType type){
  if(side!=null)return List.of();var spec=data.get(TartarusMeshBlockEntity.MESH);
  if(spec==null||spec.instances().isEmpty()){if(mesh.equals("empty")||mesh.equals("emissive"))return List.of();spec=new TartarusMeshBlockEntity.Spec(List.of(new TartarusMeshBlockEntity.Instance(mesh,0,0,0)),0,0,0,false);}
  List<BakedQuad> result;
  synchronized(cache){result=cache.get(spec);if(result==null){result=bake(spec);cache.put(spec,result);cachedQuads+=result.size();var it=cache.entrySet().iterator();while(cache.size()>1&&(cache.size()>192||cachedQuads>200000)){cachedQuads-=it.next().getValue().size();it.remove();}}}
  var origin=data.get(TartarusMeshBlockEntity.CUTAWAY_ORIGIN);var volumes=com.gx.velveteclipse.client.presentation.BattleScenery.snapshot();
  if(origin!=null&&spec.clipped()&&!volumes.isEmpty()){double x=origin.m_123341_()+spec.clipX(),y=origin.m_123342_()+spec.clipY(),z=origin.m_123343_()+spec.clipZ();volumes=volumes.stream().filter(v->v.intersects(x,y,z,x+16,y+16,z+16)).toList();}
  return CutawayQuads.filter(result,origin,volumes);
 }
 private List<BakedQuad> bake(TartarusMeshBlockEntity.Spec spec){
  var quads=new ArrayList<BakedQuad>();
  for(var instance:spec.instances()){
   float[] raw=TartarusMeshData.load(instance.model());var portalBounds=TartarusMeshData.portalBounds(raw);
   for(int index=0;index<raw.length;index+=33){
    boolean luminous=raw[index+9]==5||raw[index+9]==6;
    if(mesh.equals("emissive")&&!luminous||mesh.equals("empty")&&luminous&&!instance.model().startsWith("city_"))continue;
    var poly=new ArrayList<float[]>();for(int v=0;v<3;v++){float[] a=TartarusMeshData.withPortalUv(raw,index+v*11,portalBounds);var position=new org.joml.Vector3f(a[0]-.5f,a[1]-.5f,a[2]-.5f);rotation.m_252783_().transformPosition(position);a[0]=position.x+.5f+instance.x();a[1]=position.y+.5f+instance.y();a[2]=position.z+.5f+instance.z();var normal=new org.joml.Vector3f(a[3],a[4],a[5]);rotation.getNormalMatrix().transform(normal).normalize();a[3]=normal.x;a[4]=normal.y;a[5]=normal.z;poly.add(a);}
    List<float[]> clipped=poly;
    if(spec.clipped())for(int axis=0;axis<3;axis++){int origin=axis==0?spec.clipX():axis==1?spec.clipY():spec.clipZ();clipped=TartarusMeshData.clip(clipped,axis,origin,true);clipped=TartarusMeshData.clip(clipped,axis,origin+16,false);}
    if(clipped.size()<3)continue;
    if(clipped.get(0)[9]>=7&&clipped.get(0)[9]<=9){
     for(int k=1;k+1<clipped.size();k++){float[] p=clipped.get(0),q=clipped.get(k),r=clipped.get(k+1);if(TartarusMeshData.backwards(p,q,r)){var swap=q;q=r;r=swap;}quads.add(quad(new float[][]{p,q,r,r},11,12,0,0,0,0,1));}
     continue;
    }
    // Preserve the authored city texture frequency; the Tartarus pattern repeats every eight blocks.
    int normal=TartarusMeshData.dominant(clipped.get(0)),u=normal==0?2:0,v=normal==1?2:1;
    int material=Math.round(clipped.get(0)[9]);float span=material>=10&&material<=17?(material==11?1:material==14?4:2):8;
    // City anchors can occupy any empty block in a section. UVs remain tied to
    // the authored section origin, rather than sliding with the anchor block.
    float[] shift=instance.model().startsWith("city_")?new float[]{instance.x(),instance.y(),instance.z()}:new float[3];
    float uMin=Float.POSITIVE_INFINITY,uMax=-uMin,vMin=uMin,vMax=-uMin;for(var p:clipped){uMin=Math.min(uMin,p[u]);uMax=Math.max(uMax,p[u]);vMin=Math.min(vMin,p[v]);vMax=Math.max(vMax,p[v]);}
    int u0=(int)Math.floor((uMin-shift[u])/span),u1=(int)Math.floor((uMax-shift[u]-.00001)/span),v0=(int)Math.floor((vMin-shift[v])/span),v1=(int)Math.floor((vMax-shift[v]-.00001)/span);
    for(int a=u0;a<=Math.max(u0,u1);a++)for(int b=v0;b<=Math.max(v0,v1);b++){
     var tile=TartarusMeshData.clip(TartarusMeshData.clip(TartarusMeshData.clip(TartarusMeshData.clip(clipped,u,a*span+shift[u],true),u,(a+1)*span+shift[u],false),v,b*span+shift[v],true),v,(b+1)*span+shift[v],false);
     for(int k=1;k+1<tile.size();k++){float[] p=tile.get(0),q=tile.get(k),r=tile.get(k+1);if(TartarusMeshData.backwards(p,q,r)){var tmp=q;q=r;r=tmp;}quads.add(quad(new float[][]{p,q,r,r},u,v,a,b,shift[u],shift[v],span));}
    }
   }
  }
  return List.copyOf(quads);
 }
 private BakedQuad quad(float[][] p,int uAxis,int vAxis,int uTile,int vTile,float uShift,float vShift,float uvScale){
  int material=Math.max(0,Math.min(17,Math.round(p[0][9])));var texture=textures[material];int[] data=new int[32];
  for(int i=0;i<4;i++){var a=p[i];int offset=i*8;for(int j=0;j<3;j++)data[offset+j]=Float.floatToRawIntBits(a[j]);
   int r=Math.round(Math.max(0,Math.min(1,a[6]))*255),g=Math.round(Math.max(0,Math.min(1,a[7]))*255),b=Math.round(Math.max(0,Math.min(1,a[8]))*255);
   if(material>=7&&material<=9)r=g=b=255;
   data[offset+3]=0xff000000|b<<16|g<<8|r;
   data[offset+4]=Float.floatToRawIntBits(texture.m_118367_(Math.max(.008,Math.min(15.992,((a[uAxis]-uShift)/uvScale-uTile)*16))));data[offset+5]=Float.floatToRawIntBits(texture.m_118393_(Math.max(.008,Math.min(15.992,((a[vAxis]-vShift)/uvScale-vTile)*16))));
   int light=material==5||material==6?15:material>=7&&material<=9?12:7;
   data[offset+6]=light<<4;data[offset+7]=(Math.round(a[3]*127)&255)|((Math.round(a[4]*127)&255)<<8)|((Math.round(a[5]*127)&255)<<16);
  }
  float[] n=p[0];int axis=TartarusMeshData.dominant(n);Direction direction=axis==0?(n[3]>=0?Direction.EAST:Direction.WEST):axis==1?(n[4]>=0?Direction.UP:Direction.DOWN):(n[5]>=0?Direction.SOUTH:Direction.NORTH);
  return new BakedQuad(data,-1,direction,texture,material!=5&&material!=6,false);
 }
 @Override public ChunkRenderTypeSet getRenderTypes(BlockState state,RandomSource random,ModelData data){return ChunkRenderTypeSet.of(RenderType.m_110451_());}
 @Override public boolean m_7541_(){return false;}@Override public boolean m_7539_(){return true;}@Override public boolean m_7547_(){return true;}@Override public boolean m_7521_(){return false;}
 @Override public TextureAtlasSprite m_6160_(){return textures[2];}@Override public ItemOverrides m_7343_(){return ItemOverrides.f_111734_;}@Override public ItemTransforms m_7442_(){return transforms;}
}
