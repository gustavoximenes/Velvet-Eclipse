package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.forge.*;import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.core.fusion.*;import net.minecraft.nbt.CompoundTag;
public final class ProgressionAudit {
 private static int checks;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static final class Player {private final CompoundTag data=new CompoundTag();public CompoundTag getPersistentData(){return data;}}
 public static void main(String[] args){
  PersonaRegistry.bootstrap();var data=new PlayerPersonaData();data.givePersona("cinder_wisp");data.addPlayerExperience(100);data.addYen(750);data.setResources(75,20);
  data.givePersona("izanagi");var persona=data.instance("izanagi");persona.inheritSkill("media");persona.addExperience(PersonaRegistry.persona("izanagi").orElseThrow(),20);data.updatePersona(persona);
  data.setPlayerLevel(25);check(data.playerLevel()==25&&data.playerExperience()==0,"player exact level with zero partial XP");
  check(data.setPersonaLevel("izanagi",5),"owned persona changes");persona=data.instance("izanagi");
  check(persona.level()==5&&persona.experience()==0&&persona.learnedSkills().containsAll(java.util.List.of("rakunda","tarukaja","media")),"level learns natural skills and preserves inherited skill");
  data.setPersonaLevel("izanagi",1);check(data.instance("izanagi").level()==1&&data.instance("izanagi").learnedSkills().contains("media"),"lowering preserves skill cards");
  check(data.hp()==75&&data.sp()==20&&data.yen()==750&&data.activePersona().equals("cinder_wisp"),"resources, money and equipped persona unchanged");
  check(!data.setPersonaLevel("missing",8)&&!data.rosterIds().contains("missing"),"unowned persona is not granted implicitly");
  for(int invalid:new int[]{-1,0,100,Integer.MAX_VALUE}){try{data.setPlayerLevel(invalid);throw new AssertionError("accepted invalid player level");}catch(IllegalArgumentException expected){}try{data.setPersonaLevel("izanagi",invalid);throw new AssertionError("accepted invalid persona level");}catch(IllegalArgumentException expected){}}
  check(data.playerLevel()==25&&data.instance("izanagi").level()==1,"failed requests are atomic");
  var player=new Player();data.save(player);var copy=PlayerPersonaData.load(player);check(copy.playerLevel()==25&&copy.instance("izanagi").level()==1&&copy.instance("izanagi").learnedSkills().contains("media"),"both levels and inherited skills survive save/load");
  copy.expandPersonaCapacity();copy.expandPersonaCapacity();copy.givePersona("iron_oni");copy.givePersona("lumen_hare");copy.setPlayerLevel(99);check(FusionTransaction.preview(copy,"iron_oni","lumen_hare",null).allowed(),"raised player level unlocks eligible fusion");copy.setPlayerLevel(1);check(!FusionTransaction.preview(copy,"iron_oni","lumen_hare",null).allowed(),"lowered player level restores fusion restriction");
  var dispatcher=new com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>();ProgressionCommands.register(dispatcher);
  var root=dispatcher.getRoot().getChild("velvet");check(root.getChild("player").getChild("level").getChild("nivel").getChild("jogador")!=null&&root.getChild("persona").getChild("level").getChild("id").getChild("nivel").getChild("jogador")!=null,"both native command trees expose self and target forms");
  System.out.println("PASS progression: "+checks+" checks; exact levels, XP reset, inherited skills, save/load, rejected values, fusion gates and command registration. Offline only.");
 }
}
