package com.gx.velveteclipse.core.battle;
import com.gx.velveteclipse.core.persona.*;
import java.util.*;
/** Only observed outcomes are transmitted; untested affinities stay unknown. */
public final class AffinityKnowledge {
 private final Map<String,EnumMap<Element,Affinity>> known=new TreeMap<>();
 public void learn(String enemy,DamageOutcome hit){if(!hit.missed()&&hit.element()!=Element.NONE)known.computeIfAbsent(enemy,k->new EnumMap<>(Element.class)).put(hit.element(),hit.affinity());}
 public Affinity get(String enemy,Element element){var map=known.get(enemy);return map==null?null:map.get(element);}
 public String forEnemy(String enemy){var map=known.get(enemy);if(map==null)return "";return String.join(",",map.entrySet().stream().map(e->e.getKey()+":"+e.getValue()).toList());}
 public String encode(){List<String> out=new ArrayList<>();known.forEach((id,map)->map.forEach((e,a)->out.add(id+":"+e+":"+a)));return String.join(";",out);}
 public static AffinityKnowledge decode(String text){var k=new AffinityKnowledge();if(text==null)return k;for(String s:text.split(";")){var p=s.split(":");if(p.length==3)try{k.known.computeIfAbsent(p[0],id->new EnumMap<>(Element.class)).put(Element.valueOf(p[1]),Affinity.valueOf(p[2]));}catch(IllegalArgumentException ignored){}}return k;}
}
