package com.gx.velveteclipse.roguelike;
import java.util.*;

/** Individual persistent buffs and ranks. Buff duplicates do not stack; Up cards raise ranks to 10. */
public final class RogueProfile {
 public String focus="none";
 public Set<String> collected=new LinkedHashSet<>(),active=new LinkedHashSet<>();
 public Map<String,Integer> ranks=new LinkedHashMap<>();
 public Set<String> upCollected=new LinkedHashSet<>();
 public RogueProfile normalized(){
  focus=RogueCatalog.Focus.parse(focus).id();collected=clean(collected);active=clean(active);collected.addAll(active);
  ranks=ArcanaUpCatalog.clean(ranks);var found=new LinkedHashSet<String>();
  for(var c:ArcanaUpCatalog.CARDS)if(ranks.containsKey(c.id())||upCollected!=null&&upCollected.contains(c.id()))found.add(c.id());
  upCollected=found;return this;
 }
 private static Set<String> clean(Set<String> input){var result=new LinkedHashSet<String>();if(input!=null)for(var c:RogueCatalog.CARDS)if(input.contains(c.id()))result.add(c.id());return result;}
 public boolean collect(String id){var up=ArcanaUpCatalog.offered(id);return up!=null?upCollected.add(up.id()):RogueCatalog.card(id)!=null&&collected.add(id);}
 public boolean activate(String id){if(RogueCatalog.card(id)==null||active.contains(id))return false;collect(id);return active.add(id);}
 public int rank(String id){return Math.max(0,Math.min(ArcanaUpCatalog.MAX_RANK,ranks.getOrDefault(id,0)));}
 public boolean canUse(String id){var up=ArcanaUpCatalog.offered(id);return up!=null?rank(up.id())<ArcanaUpCatalog.MAX_RANK:RogueCatalog.card(id)!=null&&!active.contains(id);}
 public boolean activateFromItem(String offered,String held,boolean enabled,boolean editable){
  if(!enabled||!editable||!Objects.equals(offered,held)||!canUse(offered))return false;
  var up=ArcanaUpCatalog.offered(offered);if(up==null)return activate(offered);
  collect(offered);ranks.put(up.id(),rank(up.id())+1);return true;
 }
 public RogueBonuses bonuses(boolean enabled){return enabled?new RogueBonuses(RogueCatalog.Focus.parse(focus),active):RogueBonuses.NONE;}
}
