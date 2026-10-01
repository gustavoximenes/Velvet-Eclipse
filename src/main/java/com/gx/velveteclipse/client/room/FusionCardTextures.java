package com.gx.velveteclipse.client.room;

import java.util.concurrent.ConcurrentHashMap;

/** Ingredient IDs come from the successful transaction, including other party members' Personas. */
public final class FusionCardTextures {
 private static final ConcurrentHashMap<String,String> CACHE=new ConcurrentHashMap<>();
 public static String front(String persona){
  if(persona==null||!persona.matches("[a-z0-9_]+"))return "velvet_fusion_card";
  return CACHE.computeIfAbsent(persona,id->{
   String path="fusion_cards/"+id;
   return FusionCardTextures.class.getResource("/assets/velvet_eclipse/textures/entity/"+path+".png")!=null?path:"velvet_fusion_card";
  });
 }
 private FusionCardTextures(){}
}
