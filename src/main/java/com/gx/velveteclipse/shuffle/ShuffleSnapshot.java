package com.gx.velveteclipse.shuffle;
import com.gx.velveteclipse.menu.MenuSnapshot;
import java.util.List;
public record ShuffleSnapshot(String screen,int count,long yen,String token,int floor,List<Choice> choices,List<MenuSnapshot.Persona> owned,String skill,String message){
 public record Choice(ShuffleRules.Kind kind,String id,int amount,int rank,String title,String detail,MenuSnapshot.Persona persona){}
}
