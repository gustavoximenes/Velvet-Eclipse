package com.gx.velveteclipse.client.exploration;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.google.gson.Gson;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** Native cuboids, one Box-UV atlas per category, same assets as the editable Blockbench files. */
public final class WeaponModels {
 private record Cube(String name,float[] origin,float[] size,int[] uv){}
 private record Model(List<Cube> cubes){}
 private static final Map<Family,ModelPart> CACHE=new EnumMap<>(Family.class);
 private static ModelPart model(Family f){return CACHE.computeIfAbsent(f,k->{
  try(var in=WeaponModels.class.getResourceAsStream("/assets/velvet_eclipse/weapons/"+k.name().toLowerCase(Locale.ROOT)+".json")){
   if(in==null)throw new IOException("Missing weapon "+k);var data=new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Model.class);var cubes=new ArrayList<ModelPart.Cube>();
   for(var c:data.cubes())if(k!=Family.BOW||!arrowPart(c.name()))cubes.add(new ModelPart.Cube(c.uv()[0],c.uv()[1],c.origin()[0],c.origin()[1],c.origin()[2],c.size()[0],c.size()[1],c.size()[2],0,0,0,false,128,128,EnumSet.allOf(Direction.class)));
   return new ModelPart(cubes,Map.of());
  }catch(IOException e){throw new IllegalStateException(e);}
 });}
 private static ModelPart arrow;
 private static boolean arrowPart(String name){return name!=null&&(name.startsWith("arrow_")||name.equals("fletching"));}
 public static void arrow(PoseStack p,MultiBufferSource out,int light){if(arrow==null)try(var in=WeaponModels.class.getResourceAsStream("/assets/velvet_eclipse/weapons/bow.json")){var data=new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Model.class);var cubes=new ArrayList<ModelPart.Cube>();for(var c:data.cubes())if(arrowPart(c.name()))cubes.add(new ModelPart.Cube(c.uv()[0],c.uv()[1],c.origin()[0],c.origin()[1],c.origin()[2],c.size()[0],c.size()[1],c.size()[2],0,0,0,false,128,128,EnumSet.allOf(Direction.class)));arrow=new ModelPart(cubes,Map.of());}catch(IOException e){throw new IllegalStateException(e);}arrow.m_104301_(p,out.m_6299_(RenderType.m_110452_(new ResourceLocation("velvet_eclipse","textures/entity/weapon_bow.png"))),light,655360);}
 public static void draw(Family f,PoseStack p,MultiBufferSource out,int light){if(f==Family.NONE)f=Family.SWORD;var texture=new ResourceLocation("velvet_eclipse","textures/entity/weapon_"+f.name().toLowerCase(Locale.ROOT)+".png");model(f).m_104301_(p,out.m_6299_(RenderType.m_110452_(texture)),light,655360);}
}
