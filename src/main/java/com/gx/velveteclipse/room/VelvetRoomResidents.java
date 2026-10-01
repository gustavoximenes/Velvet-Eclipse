package com.gx.velveteclipse.room;

import java.util.*;
import java.util.function.*;

/** One canonical resident per stage anchor; wait for entity loading before any spawn. */
public final class VelvetRoomResidents {
 public static <T> T reconcile(boolean loaded,List<T> nearby,String saved,Function<T,UUID> id,Supplier<T> spawn,Consumer<T> remove){
  if(!loaded)return null;
  T keep=nearby.stream().filter(e->id.apply(e).toString().equals(saved)).findFirst().orElseGet(()->nearby.stream().min(Comparator.comparing(e->id.apply(e).toString())).orElse(null));
  if(keep==null)return spawn.get();
  for(T entry:nearby)if(entry!=keep)remove.accept(entry);
  return keep;
 }
 private VelvetRoomResidents(){}
}
