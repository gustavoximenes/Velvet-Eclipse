package com.gx.velveteclipse.progression;
import java.util.*;
/** Server-owned, per-player first-clear receipts and durable delivery queue. */
public final class RewardLedger {
 public static final class Receipt {public boolean boss;public Set<Integer> chests=new HashSet<>();public List<String> pending=new ArrayList<>();}
 public Map<String,Receipt> players=new HashMap<>();
 private Receipt receipt(String player){return players.computeIfAbsent(player,k->new Receipt());}
 public boolean boss(String player){var r=receipt(player);if(r.boss)return false;r.boss=true;r.pending.add(permit(25));r.pending.add("igor_contract");return true;}
 public static boolean permitChest(int floor,int index){return index==0&&floor>=50&&floor<=250&&floor%25==0;}
 public boolean chest(String player,int floor){if(!permitChest(floor,0))return false;var r=receipt(player);if(!r.chests.add(floor))return false;r.pending.add(permit(floor));return true;}
 public boolean claimed(String player,int floor){var r=players.get(player);return r!=null&&r.chests.contains(floor);}
 public List<String> pending(String player){var r=players.get(player);return r==null?List.of():List.copyOf(r.pending);}
 public void delivered(String player,String item){var r=players.get(player);if(r!=null)r.pending.remove(item);}
 public static String permit(int floor){return "purchase_permit_"+floor;}
}
