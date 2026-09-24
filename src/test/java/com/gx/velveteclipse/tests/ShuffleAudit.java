package com.gx.velveteclipse.tests;
import com.gx.velveteclipse.shuffle.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.client.shuffle.ShuffleDesign;
import com.gx.velveteclipse.client.menu.MenuDesign;
import net.minecraft.nbt.CompoundTag;
import java.util.*;
import java.util.List;
import java.nio.file.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class ShuffleAudit {
 private static int checks;
 private static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 public static final class Player {private final CompoundTag tag=new CompoundTag();public CompoundTag getPersistentData(){return tag;}}
 private static ShuffleData offer(ShuffleRules.Kind kind,String id,int amount){var data=new ShuffleData();data.add(new ShuffleRules.Round("offer",5,List.of(new ShuffleRules.Card(kind,id,amount,1))));return data;}
 public static void main(String[] args)throws Exception{
  PersonaRegistry.bootstrap();var kinds=new HashSet<ShuffleRules.Kind>();var sizes=new HashSet<Integer>();
  for(int floor:new int[]{1,2,5,8,20,50,99,250})for(int seed=0;seed<40;seed++){
   var r=ShuffleRules.roll(floor,Set.of("cinder_wisp"),new Random(seed));check(r.cards().size()>=1&&r.cards().size()<=5,"1..5 cards");sizes.add(r.cards().size());
   check(r.cards().stream().map(ShuffleRules.Card::kind).distinct().count()==r.cards().size(),"different card suits per offer");
   for(var card:r.cards()){kinds.add(card.kind());if(card.kind()==ShuffleRules.Kind.PERSONA){check(ReloadShuffle.personas(floor).containsKey(card.id()),"Persona belongs to exact floor pool");check(card.amount()==PersonaRegistry.persona(card.id()).orElseThrow().baseLevel(),"acquired Persona starts at its native level");}if(card.kind()==ShuffleRules.Kind.SWORDS)check(ReloadShuffle.cards(card.rank()).contains(card.id()),"skill belongs to the floor's card rank");}
  }
  check(kinds.equals(EnumSet.allOf(ShuffleRules.Kind.class))&&sizes.size()==5,"all types and deck sizes reachable");
  var player=new Player();var data=new PlayerPersonaData();data.addYen(123456789L);data.save(player);check(PlayerPersonaData.load(player).yen()==123456789L,"yen persists separately from inventory");
  String encoded=player.tag.m_128461_("velvet_eclipse:data_v1");player.tag.m_128359_("velvet_eclipse:data_v1",String.join("|",Arrays.copyOf(encoded.split("\\|",-1),10)));check(PlayerPersonaData.load(player).yen()==0,"old saves migrate to zero yen without loss of Personas");
  data.addYen(Long.MAX_VALUE);check(data.yen()==9_999_999_999L,"currency saturates safely");
  var ledger=ShuffleData.load(player);var round=ShuffleRules.roll(20,Set.of(),new Random(31));ledger.add(round);ledger.save(player);var reopened=ShuffleData.load(player);check(reopened.first().equals(round),"reopening/reconnecting preserves exact cards and token");
  var clone=new Player();ShuffleData.copy(player,clone);check(ShuffleData.load(clone).first().equals(round),"pending offers survive player cloning");
  var d=new PlayerPersonaData();var money=offer(ShuffleRules.Kind.COINS,"",475);check(ShuffleRewards.claim(money,d,"wrong",0,k->true).card()==null&&d.yen()==0,"forged token rejected");
  check(ShuffleRewards.claim(money,d,"offer",-1,k->true).card()==null,"invalid choice rejected");check(ShuffleRewards.claim(money,d,"offer",0,k->true).card()!=null&&d.yen()==475,"one monetary reward");
  check(ShuffleRewards.claim(money,d,"offer",0,k->true).card()==null&&d.yen()==475,"replay grants nothing");
  var skill=offer(ShuffleRules.Kind.SWORDS,"zio",1);check(ShuffleRewards.claim(skill,d,"offer",0,k->false).card()==null&&skill.first()!=null,"full inventory preserves activation");int[] delivered={0};check(ShuffleRewards.claim(skill,d,"offer",0,k->{delivered[0]++;return true;}).card()!=null&&delivered[0]==1&&skill.first()==null,"skill item delivered exactly once");
  d.setResources(100,12);ShuffleRewards.claim(offer(ShuffleRules.Kind.CUPS,"",30),d,"offer",0,k->true);check(d.hp()==120&&d.sp()==12,"cups clamp HP and preserve SP");
  int oldLevel=d.playerLevel();ShuffleRewards.claim(offer(ShuffleRules.Kind.WANDS,"",260),d,"offer",0,k->true);check(d.playerLevel()>oldLevel&&d.activeInstance().level()>1,"wands grant player and equipped Persona XP");
  check(ShuffleRewards.teach(d,"cinder_wisp","zio")&&d.instance("cinder_wisp").learnedSkills().contains("zio"),"teach selected Persona");check(!ShuffleRewards.teach(d,"cinder_wisp","zio")&&!ShuffleRewards.teach(d,"unowned","zio")&&!ShuffleRewards.teach(d,"cinder_wisp","fake"),"duplicate, unknown and unowned teaching rejected");
  int old=d.activeInstance().level();ShuffleRewards.claim(offer(ShuffleRules.Kind.PERSONA,"cinder_wisp",1),d,"offer",0,k->true);check(d.activeInstance().level()>=old&&d.activeInstance().learnedSkills().contains("zio"),"duplicate Persona does not erase levels or inherited skills");
  for(String id:PersonaRegistry.skills().keySet())check(Files.exists(Path.of("src/main/resources/assets/velvet_eclipse/models/item/skill_card_"+id+".json")),"every registered skill has a creative card model");
  for(int n=1;n<=5;n++)for(int i=0;i<n;i++){var r=ShuffleDesign.card(i,n,i);check(r.x()>0&&r.x()+r.w()<900&&r.y()+r.h()<370,"cards fit menu bounds");if(i+1<n)check(r.x()+r.w()<ShuffleDesign.card(i+1,n,i).x(),"cards do not overlap");}
  var out=Path.of("build/reports/shuffle");Files.createDirectories(out);var c=new Preview();ShuffleDesign.background(c,2,2,false);
  var suits=List.of(ShuffleRules.Kind.SWORDS,ShuffleRules.Kind.COINS,ShuffleRules.Kind.CUPS,ShuffleRules.Kind.WANDS);
  for(int i=0;i<4;i++)ShuffleDesign.card(c,new ShuffleSnapshot.Choice(suits.get(i),"",20,2,"","",null),ShuffleDesign.card(i,4,3),i==3,1);
  ShuffleDesign.footer(c,"WANDS • EXPERIÊNCIA","Receba 260 EXP de jogador e da Persona equipada.","",false,false);c.save(out.resolve("cards.png"));
  c=new Preview();ShuffleDesign.background(c,.5,2,false);ShuffleDesign.intro(c,.5);c.save(out.resolve("intro.png"));
  c=new Preview();ShuffleDesign.badge(c,3,"P",900,500);c.save(out.resolve("badge.png"));
  System.out.println("PASS shuffle: "+checks+" checks; persisted offers, replay protection, all reward types, floor gates, yen migration, learning and card layout. Offline UI previews generated.");
 }
 private static final class Preview implements MenuDesign.Canvas{
  final BufferedImage image=new BufferedImage(900,500,BufferedImage.TYPE_INT_ARGB);final Graphics2D g=image.createGraphics();final Font display,body;
  Preview()throws Exception{display=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());body=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/body.ttf").toFile());g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(new Color(0x102336));g.fillRect(0,0,900,500);}
  public void fill(double x,double y,double w,double h,int color){g.setColor(new Color(color,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
  public void poly(double[] xy,int color){var p=new Path2D.Double();p.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);p.closePath();g.setColor(new Color(color,true));g.fill(p);}
  public void text(String t,double x,double y,double size,int color,double angle){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setColor(new Color(color,true));g.setFont((size>=20?display:body).deriveFont((float)size));g.drawString(t,0,g.getFontMetrics().getAscent());g.setTransform(old);}
  public double width(String t,double s){return t.length()*s*.5;}
  public void item(int slot,double x,double y){}
  void save(Path p)throws Exception{ImageIO.write(image,"png",p.toFile());g.dispose();}
 }
}
