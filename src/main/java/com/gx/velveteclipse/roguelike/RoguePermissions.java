package com.gx.velveteclipse.roguelike;
import java.util.*;
import com.mojang.brigadier.context.CommandContextBuilder;
/** Server-authoritative identity policy; OP status never substitutes for ownership. */
public final class RoguePermissions {
 public static boolean owner(UUID player,UUID integratedOwner,String dedicatedOwner){return player!=null&&(integratedOwner!=null?player.equals(integratedOwner):player.toString().equalsIgnoreCase(dedicatedOwner==null?"":dedicatedOwner));}
 public static boolean writesRule(CommandContextBuilder<?> context){
  while(context!=null){var names=context.getNodes().stream().map(n->n.getNode().getName()).toList();if(names.contains("velvetRoguelike")&&names.contains("value"))return true;context=context.getChild();}return false;
 }
 private RoguePermissions(){}
}
