package com.gx.velveteclipse.client.vfx;
import com.google.gson.*;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleConfig;
import java.nio.file.*;
import java.util.*;
/** Inspect the actual Photon configuration without creating a world, GPU context or running Minecraft. */
public final class ElementalConfigAudit {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  var root=Path.of("src/main/resources");var doc=JsonParser.parseString(Files.readString(root.resolve("data/velvet_eclipse/presentation/elemental-effects.json"))).getAsJsonObject();
  var meshes=JsonParser.parseString(Files.readString(Path.of("tools/vfx/approved02/meshes.json"))).getAsJsonObject();
  for(var recipe:doc.getAsJsonArray("recipes"))for(var entry:recipe.getAsJsonObject().getAsJsonArray("parts")){
   var p=entry.getAsJsonObject();var c=new ParticleConfig();PhotonElementalInstance.configureMotion(c,p);String model=p.get("model").getAsString();boolean sprite=Set.of("fire_flipbook","ground_glow").contains(model);
   var size=c.getStartSize().get(0,()->.5f);var r=c.getStartRotation().get(0,()->.5f);
   for(int axis=0;axis<3;axis++){check(Math.abs(size.get(axis)-p.getAsJsonArray("scale").get(axis).getAsFloat()*(sprite?.5:1))<.00001,"native particle size, not ignored emitter scale");check(r.get(axis)==p.getAsJsonArray("rotation").get(axis).getAsFloat(),"native model orientation");}
   for(float u:new float[]{0,.15f,.27f,.6f,.95f,1}){
    var g=p.getAsJsonArray("growth");float a=g.get(0).getAsFloat(),b=g.get(1).getAsFloat(),d=g.get(2).getAsFloat();float expected=u<.27f?a+(b-a)*u/.27f:b+(d-b)*(u-.27f)/.73f;
    var actual=c.sizeOverLifetime.getSize().get(u,()->.5f);for(int axis=0;axis<3;axis++)check(Math.abs(actual.get(axis)-expected)<.0001,"growth matches authored silhouette");
    var spin=p.getAsJsonArray("spin");check(Math.abs(c.rotationOverLifetime.getYaw().get(u,()->.5f).floatValue()-spin.get(0).getAsFloat()*u)<.0001,"Photon yaw=X");check(Math.abs(c.rotationOverLifetime.getPitch().get(u,()->.5f).floatValue()-spin.get(1).getAsFloat()*u)<.0001,"Photon pitch=Y");check(Math.abs(c.rotationOverLifetime.getRoll().get(u,()->.5f).floatValue()-spin.get(2).getAsFloat()*u)<.0001,"Photon roll=Z");
   }
   check(c.getMaxParticles()==1&&!c.isLooping(),"bounded finite emitter");
  }
  for(var entry:meshes.entrySet()){
   var points=entry.getValue().getAsJsonObject().getAsJsonArray("vertices");var lines=Files.readAllLines(root.resolve("assets/velvet_eclipse/models/vfx/audit02/"+entry.getKey()+".obj")).stream().filter(s->s.startsWith("v ")).toList();check(lines.size()==points.size(),"all approved vertices exported");
   for(int i=0;i<lines.size();i++){var coords=lines.get(i).substring(2).split(" ");for(int axis=0;axis<3;axis++)check(Math.abs(Double.parseDouble(coords[axis])-.5-points.get(i).getAsJsonArray().get(axis).getAsDouble())<.000001,"Photon Model -0.5 transform recovers original vertex");}
  }
  var registered=new HashSet<net.minecraft.resources.ResourceLocation>();ElementalPhoton.registerModels(registered::add);check(registered.size()>10,"actual registration covers model shapes");for(var id:registered)check(Files.exists(root.resolve("assets/velvet_eclipse/models/"+id.m_135815_()+".json")),"registered model exists");
  System.out.println("PASS native Photon config: "+checks+" checks; size, spin, OBJ origin, finite lifetimes and model registration. No Minecraft execution.");
 }
}
