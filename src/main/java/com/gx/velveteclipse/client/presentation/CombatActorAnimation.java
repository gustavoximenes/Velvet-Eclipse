package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.exploration.WeaponPose;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.*;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import java.util.*;

/** Weapon-specific anticipation, impact and return on real player models, including teammates. */
public final class CombatActorAnimation implements IAnimation {
 private static final Map<AbstractClientPlayer,ModifierLayer<IAnimation>> layers=new WeakHashMap<>();
 private final UUID player;
 private CombatActorAnimation(UUID id){player=id;}
 public static void attach(AbstractClientPlayer p){if(layers.containsKey(p))return;var layer=new ModifierLayer<IAnimation>();layer.setAnimation(new CombatActorAnimation(p.m_20148_()));layers.put(p,layer);PlayerAnimationAccess.getPlayerAnimLayer(p).addAnimLayer(750,layer);}
 public void tick(){}public void setupAnim(float partial){}
 public boolean isActive(){return BattleDirector.attacking(player)||BattleDirector.hitPlayer(player);}
 public Vec3f get3DTransform(String part,TransformType type,float partial,Vec3f base){
  if(!isActive())return base;
  if(BattleDirector.hitPlayer(player)){float recoil=(float)Math.sin(Math.min(1,BattleDirector.hitAge(partial)/12)*Math.PI);if(type==TransformType.ROTATION&&part.equals("body"))return new Vec3f(base.getX()-.19f*recoil,base.getY(),base.getZ()+.05f*recoil);return base;}
  double t=BattleDirector.age(partial),attack=com.gx.velveteclipse.core.battle.MeleeTimeline.slash(t),speed=t<12||t>30?.85:0;
  if(type==TransformType.ROTATION)return WeaponPose.rotation(BattleDirector.weapon(),part,t*.7,speed,attack);
  if(type==TransformType.BEND)return WeaponPose.bend(BattleDirector.weapon(),part,t*.7,speed,attack);
  return base;
 }
}
