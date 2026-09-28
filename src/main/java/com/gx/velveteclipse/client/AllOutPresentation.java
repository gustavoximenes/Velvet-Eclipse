package com.gx.velveteclipse.client;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.equipment.EquipmentCatalog.Family;
import com.gx.velveteclipse.network.AllOutCueS2C;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import java.util.*;

/** Runtime adapter for the approved scene painter and each participant's real skin. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class AllOutPresentation {
 private static final ResourceLocation SMOKE=new ResourceLocation("velvet_eclipse","textures/gui/all_out_smoke.png");
 private static AllOutCueS2C cue;private static int ticks;
 public static void accept(AllOutCueS2C p){
  if(!p.active()){reset();return;}
  if(p.elapsed()>0){
   if(cue==null||cue.leader()!=p.leader()||!cue.fighters().equals(p.fighters()))return;
   cue=p;ticks=Math.min(ticks,89);return;
  }
  cue=p;ticks=0;PersonaCinematics.reset();
 }
 public static boolean active(){return cue!=null;}
 public static void reset(){cue=null;ticks=0;}
 public static AllOutStyle style(){return cue==null?AllOutStyle.MAKOTO:AllOutStyle.parse(cue.style());}
 private static Family weapon(int id){try{return Family.valueOf(cue.weapons().getOrDefault(id,"SWORD"));}catch(IllegalArgumentException ex){return Family.SWORD;}}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END&&cue!=null&&!Minecraft.m_91087_().m_91104_()){if(Minecraft.m_91087_().f_91073_==null||++ticks>AllOutTimeline.VICTORY_END+100)reset();}}
 public static void paint(GuiGraphics g,HudDesign.Canvas c,int w,int h,float partial){
  var mc=Minecraft.m_91087_();if(cue==null||mc.f_91073_==null||h<=0)return;
  // Slot zero is the initiator, independently of turn-order or packet order.
  var party=new ArrayList<AbstractClientPlayer>();
  var leader=mc.f_91073_.m_6815_(cue.leader());if(leader instanceof AbstractClientPlayer p)party.add(p);
  for(int id:cue.fighters())if(id!=cue.leader()&&mc.f_91073_.m_6815_(id) instanceof AbstractClientPlayer p)party.add(p);
  if(party.isEmpty())return;
  boolean confirmed=cue.elapsed()>=AllOutTimeline.IMPACT,victory=confirmed&&cue.victory();
  double t=confirmed?ticks+partial:Math.min(89,ticks+partial);
  // Normalize vertical resolution without stretching skins or overflowing GUI depth.
  float scale=h/506f;int width=Math.round(w/scale);g.m_280262_();var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(0,0,420);pose.m_85841_(scale,scale,1);
  try{AllOutDesign.paint(new SceneCanvas(g,c,party,victory),width,506,t,victory,style(),party.get(0).m_36316_().getName(),party.size());}
  finally{g.m_280262_();pose.m_85849_();}
 }
 private static final class SceneCanvas implements AllOutDesign.Canvas {
  private final GuiGraphics g;private final HudDesign.Canvas delegate;private final List<AbstractClientPlayer> party;private final boolean victory;private boolean foreground;
  SceneCanvas(GuiGraphics g,HudDesign.Canvas delegate,List<AbstractClientPlayer> party,boolean victory){this.g=g;this.delegate=delegate;this.party=party;this.victory=victory;}
  private void overlay(Runnable draw){var p=g.m_280168_();p.m_85836_();if(foreground)p.m_85837_(0,0,400);try{draw.run();}finally{p.m_85849_();}}
  public void fill(int x,int y,int w,int h,int color){overlay(()->delegate.fill(x,y,w,h,color));}
  public void poly(double[] points,int color){overlay(()->delegate.poly(points,color));}
  public void text(String text,int x,int y,int color){overlay(()->delegate.text(text,x,y,color));}
  public int width(String text){return delegate.width(text);}
  public void styled(String text,int x,int y,float scale,int color){overlay(()->delegate.styled(text,x,y,scale,color));}
  public void angled(String text,int x,int y,float scale,int color,float angle){overlay(()->delegate.angled(text,x,y,scale,color,angle));}
  public void actor(int index,AllOutDesign.Frame frame,double tick){var player=party.get(index);AllOutPlayerRenderer.draw(g,player,frame,tick,victory,style(),weapon(player.m_19879_()),index);foreground=true;}
  public void smoke(AllOutDesign.Puff puff){
   if(puff.alpha()<=0||puff.size()<=0)return;g.m_280262_();var p=g.m_280168_();float[] color=RenderSystem.getShaderColor().clone();p.m_85836_();
   try{
    p.m_85837_(puff.x(),puff.y(),0);p.m_252781_(new Quaternionf().rotationZ(puff.rotation()));float size=(float)puff.size();p.m_85841_(size/128,size/128,1);
    RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();RenderSystem.setShaderColor(1,1,1,puff.alpha());
    g.m_280411_(SMOKE,-64,-64,128,128,(puff.variant()%4)*128,(puff.variant()/4)*128,128,128,512,256);
   }finally{RenderSystem.setShaderColor(color[0],color[1],color[2],color[3]);RenderSystem.enableDepthTest();p.m_85849_();}
  }
 }
 public static double formationDensity(int n){return AllOutDesign.density(n);}
 public static void background(HudDesign.Canvas c,int w,int h,double t){AllOutDesign.arena(c,w,h,t);}
 public static void victoryBackdrop(HudDesign.Canvas c,int w,int h,String name){victoryBackdrop(c,w,h,name,AllOutStyle.MAKOTO);}
 public static void victoryBackdrop(HudDesign.Canvas c,int w,int h,String name,AllOutStyle s){AllOutDesign.card(c,w,h,145,s,name);}
 public static void finisherText(HudDesign.Canvas c,int w,int h,AllOutStyle s){AllOutDesign.motto(c,w,h,s);}
 public static void cloud(HudDesign.Canvas c,int w,int h,double t,double fade){if(c instanceof AllOutDesign.Canvas scene)for(var puff:AllOutDesign.puffs(w,h,t))scene.smoke(puff);}
}
