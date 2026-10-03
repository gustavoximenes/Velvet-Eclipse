package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.client.tartarus.*;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.resources.metadata.animation.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;
import java.nio.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

/** Exercises the production Forge quad baker without creating a window or starting Minecraft. */
public final class TartarusNativeModelAudit {
 private static final class Sprite extends TextureAtlasSprite {Sprite(SpriteContents data,int index){super(new ResourceLocation("velvet_eclipse","test_atlas"),data,160,16,index*16,0);}}
 public static void main(String[] args)throws Exception{
  var textures=new TextureAtlasSprite[10];var images=new ArrayList<NativeImage>();
  for(int i=0;i<10;i++){var image=new NativeImage(16,16,true);images.add(image);textures[i]=new Sprite(new SpriteContents(new ResourceLocation("velvet_eclipse","material_"+i),new FrameSize(16,16),image,AnimationMetadataSection.f_119012_),i);}
  var ctor=TartarusMeshModel.class.getDeclaredConstructor(String.class,TextureAtlasSprite[].class,ItemTransforms.class,Transformation.class);ctor.setAccessible(true);
  var model=ctor.newInstance("empty",textures,ItemTransforms.f_111786_,Transformation.m_121093_());var emissionModel=ctor.newInstance("emissive",textures,ItemTransforms.f_111786_,Transformation.m_121093_());var random=RandomSource.m_216327_();int quads=0;var root=Path.of("build/reports/tartarus-rework/native");Files.createDirectories(root);
  for(int floor:new int[]{0,7,8,10,20,25}){
   var p=TartarusInteriorLayout.floor(781241,floor);
   if(floor==8){for(int seed=0;seed<100&&!p.architecture().stream().anyMatch(piece->piece.model().contains("_platforms_"));seed++)p=TartarusInteriorLayout.floor(seed,floor);if(p.architecture().stream().noneMatch(piece->piece.model().contains("_platforms_")))throw new AssertionError("missing twin-stair sample");Files.writeString(root.getParent().resolve("floor-8.json"),new com.google.gson.Gson().toJson(p));}
   var all=new ByteArrayOutputStream();int count=0;var portalSeams=new HashMap<String,float[]>();int centerChecks=0;
   for(var anchor:TartarusMeshPlacement.anchors(p)){
    var data=ModelData.builder().with(TartarusMeshBlockEntity.MESH,anchor.spec()).build();var baked=(anchor.emissive()?emissionModel:model).getQuads(null,null,random,data,null);
    if(baked.isEmpty())continue;quads+=baked.size();
    for(var quad:baked){int[] a=quad.m_111303_();for(int vertex=0;vertex<3;vertex++){
     int offset=vertex*8;float x=Float.intBitsToFloat(a[offset]),y=Float.intBitsToFloat(a[offset+1]),z=Float.intBitsToFloat(a[offset+2]);
     int[] origin={anchor.spec().clipX(),anchor.spec().clipY(),anchor.spec().clipZ()};float[] position={x,y,z};for(int axis=0;axis<3;axis++)if(position[axis]<origin[axis]-.0001||position[axis]>origin[axis]+16.0001)throw new AssertionError("quad escapes section");
     float u=Float.intBitsToFloat(a[offset+4]),v=Float.intBitsToFloat(a[offset+5]);if(u<0||u>1||v<0||v>1)throw new AssertionError("UV spills atlas");
     int mat=Math.min(9,(int)(u*10));int col=a[offset+3],normal=a[offset+7];
     if(anchor.emissive()!=(mat==5||mat==6))throw new AssertionError("shader emitter contains non-emissive stone or loses luminous details");
     if(mat==5&&(a[offset+6]&0xffff)<240)throw new AssertionError("green ornament is not fully emissive");
     if(mat>=7){
      float globalX=x+anchor.x(),globalY=y+anchor.y(),globalZ=z+anchor.z(),localU=u*10-mat;
      String key=mat+":"+Math.round(globalX*1000)+":"+Math.round(globalY*1000)+":"+Math.round(globalZ*1000)+":"+normal;
      var previous=portalSeams.putIfAbsent(key,new float[]{localU,v});if(previous!=null&&(Math.abs(previous[0]-localU)>.002||Math.abs(previous[1]-v)>.002))throw new AssertionError("portal UV restarts at section boundary "+key);
      if(mat==7&&Math.abs(globalX)<.0001&&((byte)(normal>>16))>120){centerChecks++;if(Math.abs(localU-.5)>.002)throw new AssertionError("clock texture splits at centre");}
     }
     var bytes=ByteBuffer.allocate(52).order(ByteOrder.LITTLE_ENDIAN);bytes.putFloat(x+anchor.x()).putFloat(y+anchor.y()).putFloat(z+anchor.z());for(int k=0;k<3;k++)bytes.putFloat((byte)(normal>>(k*8))/127f);for(int k=0;k<3;k++)bytes.putFloat(((col>>(k*8))&255)/255f);bytes.putFloat(mat).putFloat(mat==6?1:mat>=7?.35f:0).putFloat(u*10-mat).putFloat(v);all.write(bytes.array());count++;
    }}
   }
   if(floor==0&&centerChecks==0)throw new AssertionError("clock centre seam was not exercised");
   Files.write(root.resolve("floor-"+floor+".bin"),all.toByteArray());Files.writeString(root.resolve("floor-"+floor+"-count.txt"),Integer.toString(count));
  }
  for(var image:images)image.close();Files.writeString(root.getParent().resolve("native.json"),new com.google.gson.Gson().toJson(Map.of("result","PASS","quads",quads,"floors",6,"inGame",false)));System.out.println("PASS native Forge baker: "+quads+" quads, chunk bounds, emitter isolation and continuous portal UVs; six production floor exports. No Minecraft launch.");
 }
}
