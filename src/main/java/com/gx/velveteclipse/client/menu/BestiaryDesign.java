package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.menu.MenuSnapshot;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.client.EnemyAffinityHud;
import java.util.*;
public final class BestiaryDesign {
 public static Map<Element,Affinity> known(String text){var out=new EnumMap<Element,Affinity>(Element.class);if(text==null)return out;for(String pair:text.split(",")){var v=pair.split(":");if(v.length==2)try{out.put(Element.valueOf(v[0]),Affinity.valueOf(v[1]));}catch(IllegalArgumentException ignored){}}return out;}
 public static String label(Affinity a){if(a==null)return "?";return switch(a){case WEAK->"FRACO";case NORMAL->"NEUTRO";case RESIST->"RESISTE";case NULL->"ANULA";case REPEL->"REPELE";case DRAIN->"ABSORVE";};}
 public static void paint(MenuDesign.Canvas c,MenuSnapshot.Enemy e){
  c.fill(545,139,314,266,0xFF07183D);
  c.text(MenuDesign.fit(c,e.name(),288,18),557,150,18,MenuDesign.WHITE,0);
  c.text("Encontros: "+e.seen()+"    Derrotas: "+e.defeated(),557,176,11,MenuDesign.CYAN,0);
  c.text("Maior nível visto: "+e.level(),557,193,9,MenuDesign.MUTED,0);
  var known=known(e.affinities());var elements=EnemyAffinityHud.ELEMENTS;
  for(int i=0;i<elements.length;i++){
   int x=555+i%4*75,y=311+i/4*30;var affinity=known.get(elements[i]);
   c.fill(x,y,72,27,0xFF102950);PersonaGlyphs.draw(c,elements[i],x+10,y+12,6,PersonaDetails.color(elements[i]));
   c.text(com.gx.velveteclipse.equipment.DamageItems.label(elements[i]),x+3,y+2,6,MenuDesign.CYAN,0);
   c.text(label(affinity),x+22,y+14,affinity==null?11:8,affinity==Affinity.WEAK?0xFFFFDD60:affinity==Affinity.DRAIN?0xFF63FFBD:MenuDesign.WHITE,0);
  }
  c.text(e.changing()?"Afinidades observadas; este chefe muda de fraqueza.":"? = elemento ainda não testado contra este inimigo.",551,414,9,MenuDesign.MUTED,0);
 }
}
