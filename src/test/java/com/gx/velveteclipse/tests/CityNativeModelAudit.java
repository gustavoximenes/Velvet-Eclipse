package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.city.*;import com.gx.velveteclipse.darkhour.*;import com.gx.velveteclipse.client.tartarus.*;
import com.mojang.blaze3d.platform.NativeImage;import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.*;import net.minecraft.client.renderer.block.model.*;import net.minecraft.client.resources.metadata.animation.*;import net.minecraft.resources.ResourceLocation;import net.minecraft.util.RandomSource;import net.minecraftforge.client.model.data.ModelData;
import java.util.*;import java.nio.file.*;
/** Calls the production Forge baker using memory-only sprites; never opens Minecraft. */
public final class CityNativeModelAudit {
 private static final class Sprite extends TextureAtlasSprite {Sprite(SpriteContents d,int i){super(new ResourceLocation("velvet_eclipse","city_audit"),d,288,16,i*16,0);}}
 public static void main(String[] args)throws Exception{
  var textures=new TextureAtlasSprite[18];var images=new ArrayList<NativeImage>();for(int i=0;i<18;i++){var image=new NativeImage(16,16,true);images.add(image);textures[i]=new Sprite(new SpriteContents(new ResourceLocation("velvet_eclipse","city_material_"+i),new FrameSize(16,16),image,AnimationMetadataSection.f_119012_),i);}
  var ctor=TartarusMeshModel.class.getDeclaredConstructor(String.class,TextureAtlasSprite[].class,ItemTransforms.class,Transformation.class);ctor.setAccessible(true);var model=ctor.newInstance("empty",textures,ItemTransforms.f_111786_,Transformation.m_121093_());var random=RandomSource.m_216327_();long quads=0;int sections=0;
  var occupied=new HashSet<net.minecraft.core.BlockPos>();for(var c:CityPlan.get().cells())occupied.add(new net.minecraft.core.BlockPos(c[0],c[1],c[2]));
  for(var m:CityPlan.get().meshes()){
   int[] anchor=null;search:for(int y=15;y>=0;y--)for(int z=0;z<16;z++)for(int x=0;x<16;x++)if(!occupied.contains(new net.minecraft.core.BlockPos(m.x()+x,m.y()+y,m.z()+z))){anchor=new int[]{x,y,z};break search;}
   if(anchor==null)throw new AssertionError("No empty anchor for "+m.name());
   var spec=new TartarusMeshBlockEntity.Spec(List.of(new TartarusMeshBlockEntity.Instance(m.name(),-anchor[0],-anchor[1],-anchor[2])),-anchor[0],-anchor[1],-anchor[2],true);var q=model.getQuads(null,null,random,ModelData.builder().with(TartarusMeshBlockEntity.MESH,spec).build(),null);quads+=q.size();sections++;
   for(var quad:q){int[] raw=quad.m_111303_();for(int i=0;i<4;i++){for(int a=0;a<3;a++){float f=Float.intBitsToFloat(raw[i*8+a])+anchor[a];if(!Float.isFinite(f)||f<-.001||f>16.001)throw new AssertionError("Escaped section: "+m.name());}float u=Float.intBitsToFloat(raw[i*8+4]),v=Float.intBitsToFloat(raw[i*8+5]);if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||u>1||v<0||v>1)throw new AssertionError("Invalid city atlas UV");}}
  }
  images.forEach(NativeImage::close);if(quads<10000)throw new AssertionError("Missing city geometry");var out=Path.of("build/reports/city/native.json");Files.createDirectories(out.getParent());Files.writeString(out,new com.google.gson.Gson().toJson(Map.of("sections",sections,"quads",quads,"finiteCoordinates",true,"boundedUV",true)));
  System.out.println("PASS native city geometry: "+sections+" sections / "+quads+" baked quads");
 }
}
