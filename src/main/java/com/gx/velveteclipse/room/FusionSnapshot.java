package com.gx.velveteclipse.room;
import com.gx.velveteclipse.menu.MenuSnapshot.Persona;
import java.util.List;
public record FusionSnapshot(int level,int xp,int nextXp,List<Persona> owned,List<Recipe> recipes,
 Persona result,String first,String second,String token,boolean allowed,boolean merging,String message,boolean open){
 public record Recipe(String first,String second,String result,String name,int level){}
}
