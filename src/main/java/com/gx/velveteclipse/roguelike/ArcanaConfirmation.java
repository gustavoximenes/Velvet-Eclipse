package com.gx.velveteclipse.roguelike;
import java.util.*;

/** Server-only, short-lived offers. Client never supplies the card, slot or effects. */
public final class ArcanaConfirmation {
 public record Offer(String token,String card,int slot,long expires){}
 private final Map<UUID,Offer> pending=new HashMap<>();
 public Offer open(UUID player,String card,int slot,long now){var o=new Offer(UUID.randomUUID().toString(),card,slot,now+1200);pending.put(player,o);return o;}
 public Offer take(UUID player,String token,long now){var o=pending.get(player);if(o==null||!o.token().equals(token))return null;pending.remove(player);return now<=o.expires()?o:null;}
 public Offer peek(UUID player,long now){var o=pending.get(player);if(o!=null&&now>o.expires()){pending.remove(player);return null;}return o;}
 public void clearAll(){pending.clear();}
 public void clear(UUID player){pending.remove(player);}
}
