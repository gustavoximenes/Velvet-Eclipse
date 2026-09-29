package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.gx.velveteclipse.core.battle.WeaponAttackTiming;
import com.gx.velveteclipse.exploration.ExplorationNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import java.util.*;
public final class RemoteWeaponSwing {
 public record Swing(Family family,float yaw,long started,Object level){public double elapsed(float partial){var w=Minecraft.m_91087_().f_91073_;return w==null?1000:Math.max(0,w.m_46467_()-started+partial);}}
 private static final Map<Integer,Swing> swings=new HashMap<>();
 public static Family family(String value){try{return Family.valueOf(value);}catch(RuntimeException ex){return Family.SWORD;}}
 public static void accept(ExplorationNetwork.Swing p){var w=Minecraft.m_91087_().f_91073_;if(w==null)return;swings.entrySet().removeIf(e->e.getValue().level()!=w||e.getValue().elapsed(0)>24);swings.put(p.entity(),new Swing(family(p.family()),p.yaw(),w.m_46467_(),w));}
 public static Swing get(AbstractClientPlayer p){var s=swings.get(p.m_19879_());if(s==null)return null;if(s.level()!=Minecraft.m_91087_().f_91073_||s.elapsed(0)>=WeaponAttackTiming.end(s.family())||com.gx.velveteclipse.client.ClientBattleState.snapshot().active()){swings.remove(p.m_19879_());return null;}return s;}
}
