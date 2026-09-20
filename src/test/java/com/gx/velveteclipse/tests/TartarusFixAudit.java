package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.darkhour.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
/** Regression cases for the reported teleport, wall orientation and 25-floor precedence bugs. */
public final class TartarusFixAudit {
 private static int checks;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception{
  var slot=new CompoundTag();var old=UUID.randomUUID();slot.m_128359_("entity",old.toString());
  check(TartarusGuardianRecovery.reconcile(slot,false,false,false,null)==TartarusGuardianRecovery.Action.WAIT&&slot.m_128461_("entity").equals(old.toString()),"teleport waits for disk entity load; does not replace an unloaded guardian");
  check(TartarusGuardianRecovery.reconcile(slot,false,false,true,old)==TartarusGuardianRecovery.Action.PRESENT,"guardian loaded from disk is reused");
  slot.m_128359_("battle","battle");check(TartarusGuardianRecovery.reconcile(slot,false,true,true,null)==TartarusGuardianRecovery.Action.WAIT&&slot.m_128461_("battle").equals("battle"),"active battle reserves guardian even when world entity is consumed");
  check(TartarusGuardianRecovery.reconcile(slot,false,false,true,null)==TartarusGuardianRecovery.Action.SPAWN&&slot.m_128461_("entity").isEmpty()&&slot.m_128461_("battle").isEmpty(),"stale UUID after teleport no longer suppresses spawn");
  var replacement=UUID.randomUUID();check(TartarusGuardianRecovery.reconcile(slot,false,false,true,replacement)==TartarusGuardianRecovery.Action.PRESENT&&slot.m_128461_("entity").equals(replacement.toString()),"existing guardian adopted instead of duplicated");
  check(TartarusGuardianRecovery.reconcile(slot,true,false,true,null)==TartarusGuardianRecovery.Action.DEFEATED,"defeated guardian stays defeated");
  var state=new TartarusDungeonService.State();state.built.add(50);state.progress.generated.put(50,-1L);state.progress.treasures.add("50:0");state.defeated.add(50);
  check(!state.transitions19.contains(50),"existing transition queued for migration");state.transitions19.add(50);var saved=TartarusDungeonService.State.load(state.m_7176_(new CompoundTag()));check(saved.transitions19.contains(50)&&saved.progress.treasures.contains("50:0")&&saved.defeated.contains(50),"migration marker and existing fixed progress persist");
  for(int f=25;f<=250;f+=25){final int floor=f;var plan=TartarusInteriorLayout.floor(82,f);check(TartarusInteriorLayout.transitionFloor(f)&&!TartarusInteriorLayout.bossFloor(f)&&TartarusInteriorLayout.shadowLimit(f)==0,"every multiple of 25 is a safe transition: "+f);check(plan.spawns().isEmpty()&&plan.entry().z()==275,"transition architecture replaces guardian architecture");check(plan.cells().stream().noneMatch(c->Set.of("boss_gate","barrier").contains(c.material())),"no guardian wall remains");check(plan.cells().stream().anyMatch(c->c.material().equals(floor==250?"summit":"forward")),"transition exit present");}
  for(int f:new int[]{10,20,30,40,60,90,240})check(TartarusInteriorLayout.bossFloor(f)&&TartarusInteriorLayout.floor(82,f).spawns().size()==1,"other multiples of ten retain guardian");
  var assets=Path.of("src/main/resources/assets/velvet_eclipse");var geometry=software.bernie.geckolib.util.JsonUtil.GEO_GSON.fromJson(Files.readString(assets.resolve("geo/entity/tartarus_monad.geo.json")),software.bernie.geckolib.loading.json.raw.Model.class);var model=software.bernie.geckolib.loading.object.BakedModelFactory.DEFAULT_FACTORY.constructGeoModel(software.bernie.geckolib.loading.object.GeometryTree.fromModel(geometry));
  var spec=TartarusInteriorLayout.props(1,25).stream().filter(p->p.kind().equals("monad")).findFirst().orElseThrow();var pose=new PoseStack();pose.m_252781_(new org.joml.Quaternionf().rotationY((float)Math.toRadians(180-spec.yaw())));
  var nativeFaces=new ArrayList<Object>();for(var bone:model.topLevelBones())IzanagiAudit.dump(bone,pose,nativeFaces);
  var faces=new Gson().toJsonTree(nativeFaces).getAsJsonArray();double lo=Double.POSITIVE_INFINITY,hi=Double.NEGATIVE_INFINITY;
  for(var raw:faces)for(var v:raw.getAsJsonObject().getAsJsonArray("vertices")){double x=spec.at().x()+.5+v.getAsJsonArray().get(0).getAsDouble()/16;lo=Math.min(lo,x);hi=Math.max(hi,x);}
  check(lo>20&&hi<22,"entire real Monad mesh stays inside wall plane, including rotated crest/frame");
  var report=Path.of("build/reports/tartarus-fixes");Files.createDirectories(report);Files.writeString(report.resolve("monad-clearance.txt"),"Native GeckoLib geometry world X: "+lo+" .. "+hi+"; east wall starts at X=22.\n");
  for(String name:List.of("terminal","treasure","monad")){String key="tartarus_"+name;check(Files.exists(assets.resolve("models/item/"+key+".json"))&&Files.exists(assets.resolve("textures/item/"+key+".png")),"placement item model and icon "+name);for(String locale:List.of("en_us","pt_br"))check(JsonParser.parseString(Files.readString(assets.resolve("lang/"+locale+".json"))).getAsJsonObject().has("item.velvet_eclipse."+key),"creative item localized");}
  System.out.println("PASS tartarus fixes: "+checks+" checks; asynchronous guardian load/recovery, no duplicates or resurrection, ten transition floors, migration persistence, native Monad wall clearance and creative resources. Offline only.");
 }
}
