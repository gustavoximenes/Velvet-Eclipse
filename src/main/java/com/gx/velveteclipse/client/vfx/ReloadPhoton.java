package com.gx.velveteclipse.client.vfx;
import com.google.gson.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.presentation.FeedbackLibrary;
import com.gx.velveteclipse.network.BattleLightningS2C.Strike;
import net.minecraft.client.Minecraft;
import org.joml.Vector3f;
import java.util.*;
import java.io.*;
/** One owner for skill VFX. Summoning flame, shift and Evoker visuals stay in their existing renderer. */
public final class ReloadPhoton {
 private record Live(PhotonSkillInstance fx,long start,int cost){}
 private static final List<Live> live=new ArrayList<>();
 private static final Map<String,PhotonSkillInstance.Recipe> recipes=load();
 private static net.minecraft.client.multiplayer.ClientLevel level;
 private static Map<String,PhotonSkillInstance.Recipe> load(){
  try(var r=new InputStreamReader(Objects.requireNonNull(ReloadPhoton.class.getResourceAsStream("/data/velvet_eclipse/presentation/photon-effects.json")),java.nio.charset.StandardCharsets.UTF_8)){
   var out=new HashMap<String,PhotonSkillInstance.Recipe>();for(var p:PhotonSkillInstance.read(r))out.put(p.id(),p);return Map.copyOf(out);
  }catch(IOException e){throw new IllegalStateException(e);}
 }
 public static void reset(){for(var x:live)x.fx.close();live.clear();level=null;}
 public static void tick(){var w=Minecraft.m_91087_().f_91073_;if(w!=level){reset();return;}if(w==null)return;
  for(var it=live.iterator();it.hasNext();){var x=it.next();int age=(int)(w.m_46467_()-x.start);if(age<0||age>100){x.fx.close();it.remove();continue;}x.fx.tick(age);if(x.fx.done()){x.fx.close();it.remove();}}
 }
 static String family(Element e){return switch(e){case FIRE->"agi";case ELECTRIC->"zio";case ICE->"bufu";case WIND->"garu";case LIGHT->"kouha";case DARK->"eiga";case ALMIGHTY->"megido";case PSY->"psy";case NUCLEAR->"nuclear";case HEALING->"dia";case SUPPORT->"tarukaja";case AILMENT->"ailment";case SLASH->"slash";case PIERCE->"pierce";default->"strike";};}
 public static void outcome(String state,Strike target){String profile=switch(state){case "reflect","null"->"makarakarn";case "absorb"->"dia";default->"strike";};playProfile(profile,1,new ImpactAnchor(target),true,16);}
 public static void play(String skill,Element element,float scale,ImpactAnchor anchor,long seed){
  String profile=FeedbackLibrary.profile(skill);if(profile.isEmpty())profile=family(element);
  boolean follow=element==Element.SUPPORT||element==Element.HEALING||element==Element.AILMENT;
  playProfile(profile,Math.max(.75f,Math.min(1.65f,scale)),anchor,follow,32);
 }
 public static void batch(String skill,List<Strike> targets){
  var e=SpellVisuals.family(skill);String p=FeedbackLibrary.profile(skill);if(p.isEmpty())p=family(e);int cost=Math.max(2,Math.min(32,192/Math.max(1,targets.size())));
  for(var t:targets)playProfile(p,Math.max(.75f,Math.min(1.65f,SpellVisuals.scale(skill))),new ImpactAnchor(t),e==Element.SUPPORT||e==Element.HEALING||e==Element.AILMENT,cost);
 }
 private static void playProfile(String id,float scale,ImpactAnchor anchor,boolean follow,int budget){
  var w=Minecraft.m_91087_().f_91073_;if(w==null)return;if(level!=w){reset();level=w;}
  var original=recipes.get(id);if(original==null)return;
  int room=256-live.stream().mapToInt(Live::cost).sum();budget=Math.min(room,budget);if(budget<1)return;
  var layers=new ArrayList<JsonObject>();
  if(original.layers().size()>budget&&id.equals("zio")){
   var l=original.layers().get(0).deepCopy();var end=new JsonArray();end.add(0);end.add(.1);end.add(0);l.add("b",end);layers.add(l);
  }else {
   // Allocate primary shapes before optional debris; never evict a currently visible core.
   for(var layer:original.layers())if(layers.size()<budget)layers.add(layer.deepCopy());
  }
  for(var l:layers){
   for(String k:List.of("at","a","b","size"))if(l.has(k)){var a=l.getAsJsonArray(k);for(int i=0;i<a.size();i++)a.set(i,new JsonPrimitive(a.get(i).getAsFloat()*scale));}
   if(l.has("points"))for(var point:l.getAsJsonArray("points")){var a=point.getAsJsonArray();for(int i=0;i<a.size();i++)a.set(i,new JsonPrimitive(a.get(i).getAsFloat()*scale));}
   if(l.has("width"))l.addProperty("width",l.get("width").getAsFloat()*scale);
   if(l.has("count"))l.addProperty("count",Math.min(24,l.get("count").getAsInt()));
  }
  var fixed=anchor.position(1);var world=w;
  var fx=new PhotonSkillInstance(new PhotonSkillInstance.Recipe(id,layers),()->world,()->{var p=follow?anchor.position(1):fixed;return new Vector3f((float)p.f_82479_,(float)p.f_82480_,(float)p.f_82481_);});
  fx.tick(0);live.add(new Live(fx,w.m_46467_(),layers.size()));
 }
}
