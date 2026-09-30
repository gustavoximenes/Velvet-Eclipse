package com.gx.velveteclipse.client.presentation;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.function.BiFunction;

/** A command shot remains stable while entities idle or receive tiny network corrections. */
public final class CommandCameraState {
 private BattleFraming.View view;private Vec3 anchor;private int owner,blocked;private double aspect;private long checked;
 public BattleFraming.View view(){return view;}
 public void reset(){view=null;anchor=null;blocked=0;}
 public BattleFraming.View update(Vec3 player,List<Vec3> enemies,double fov,double aspect,int owner,long tick,BiFunction<Vec3,Vec3,Vec3> clip){
  boolean changed=view==null||this.owner!=owner||Math.abs(this.aspect-aspect)>.005||anchor.m_82554_(player)>.5;
  if(!changed&&tick-checked>=12){checked=tick;boolean wall=clip.apply(anchor.m_82520_(0,1.1,0),view.eye()).m_82554_(view.eye())>.6;blocked=wall?blocked+1:0;changed=blocked>=2;}
  if(changed){view=BattleFraming.command(player,enemies,fov,aspect,clip);anchor=player;this.owner=owner;this.aspect=aspect;checked=tick;blocked=0;}
  return view;
 }
}
