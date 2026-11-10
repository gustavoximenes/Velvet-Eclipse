package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.roguelike.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class RogueScreen extends Screen {
 private static RogueNetwork.State state=RogueNetwork.State.empty();
 private final Screen parent;private int page,selected,keyboard;private boolean waiting;private long sent;
 public RogueScreen(Screen parent){super(com.gx.velveteclipse.localization.ModText.component("Arcana Buffs"));this.parent=parent;}
 public static RogueBonuses bonuses(){return state.bonuses();}
 public static void open(Screen parent,int page){var s=new RogueScreen(parent);s.page=page;Minecraft.m_91087_().m_91152_(s);s.send("open","");}
 public static void accept(String json){try{
  var next=RogueNetwork.JSON.fromJson(json,RogueNetwork.State.class);if(next==null||next.active()==null||next.collected()==null)return;
  boolean newPrompt=!next.token().equals(state.token());state=next;var mc=Minecraft.m_91087_();
  if(next.open()&&!(mc.f_91080_ instanceof RogueScreen))mc.m_91152_(new RogueScreen(mc.f_91080_));
  if(mc.f_91080_ instanceof RogueScreen s){s.waiting=false;if(newPrompt){s.keyboard=0;var up=ArcanaUpCatalog.offered(next.card());if(up!=null){s.page=3;s.selected=ArcanaUpCatalog.CARDS.indexOf(up);}}}
 }catch(RuntimeException e){com.gx.velveteclipse.VelvetEclipseMod.LOGGER.warn("Invalid roguelike state",e);}}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut e){state=RogueNetwork.State.empty();}
 private double scale(){return Math.min(f_96543_/900.,f_96544_/500.);}
 private double left(){return (f_96543_-900*scale())/2;}
 private double top(){return (f_96544_-500*scale())/2;}
 private RogueDesign.View view(){return new RogueDesign.View(state,page,selected,keyboard,waiting);}
 private void send(String action,String value){waiting=true;sent=System.nanoTime();RogueNetwork.request(action,value);}
 private void activate(int i){var list=RogueDesign.buttons(view());if(i<0||i>=list.size())return;keyboard=i;var b=list.get(i);if(!b.enabled())return;
  switch(b.action()){case "page"-> {page=Integer.parseInt(b.value());selected=0;keyboard=page;}case "select","rank_select"->selected=Integer.parseInt(b.value());case "back"->m_7379_();default->send(b.action(),b.value());}
 }
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(waiting&&System.nanoTime()-sent>3_000_000_000L)waiting=false;if(f_96541_.f_91074_==null||com.gx.velveteclipse.client.ClientBattleState.snapshot().active())f_96541_.m_91152_(null);}
 @Override public void m_7379_(){if(!state.card().isEmpty()){send("cancel","");return;}RogueNetwork.request("cancel","");f_96541_.m_91152_(parent);}
 @Override public boolean m_7933_(int key,int scan,int mods){if(key==256){m_7379_();return true;}if(key==258){keyboard=Math.floorMod(keyboard+((mods&1)!=0?-1:1),RogueDesign.buttons(view()).size());return true;}if(key==257||key==335){activate(keyboard);return true;}
  if(key>=262&&key<=265){if(state.card().isEmpty()&&(page==0||page==3)){int columns=page==3?4:3,count=page==3?ArcanaUpCatalog.CARDS.size():RogueCatalog.CARDS.size();selected=Math.floorMod(selected+(key==262?1:key==263?-1:key==264?columns:-columns),count);keyboard=selected+4;}else keyboard=Math.floorMod(keyboard+(key==263||key==265?-1:1),RogueDesign.buttons(view()).size());return true;}return super.m_7933_(key,scan,mods);}
 @Override public boolean m_6375_(double x,double y,int button){if(button!=0)return false;int hit=RogueDesign.hit(view(),(x-left())/scale(),(y-top())/scale());if(hit<0)return false;activate(hit);return true;}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){g.m_280509_(0,0,f_96543_,f_96544_,RogueDesign.NAVY);var p=g.m_280168_();p.m_85836_();p.m_85837_(left(),top(),0);p.m_85841_((float)scale(),(float)scale(),1);try{RogueDesign.paint(new MenuCanvas(g),view());}finally{g.m_280262_();p.m_85849_();}}
}
