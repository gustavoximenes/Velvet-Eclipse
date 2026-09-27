package com.gx.velveteclipse.client.presentation;
import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.client.exploration.PlayerFace;
import com.gx.velveteclipse.network.BattleRosterS2C;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Quaternionf;
import java.util.*;

/** LDLib widget tree hosted in the overlay so the world and battle input remain live. */
public final class ReloadBattleHud {
 private static WidgetGroup root,commands,party;
 private static HudDesign.Canvas canvas;private static HudDesign.View view;
 private static int width,height;private static List<UUID> ids=List.of();private static long lastTick=-1;
 private static boolean showCommands;
 private static final Map<UUID,double[]> resources=new HashMap<>();
 public static void reset(){root=null;commands=null;party=null;ids=List.of();resources.clear();lastTick=-1;}
 private static List<BattleRosterS2C.Target> allies(){return BattleTargetHud.roster().stream().filter(BattleRosterS2C.Target::ally).limit(5).toList();}
 private static BattleRosterS2C.Target member(UUID id){return BattleTargetHud.roster().stream().filter(t->t.id().equals(id)).findFirst().orElse(null);}
 private static void build(int w,int h){
  width=w;height=h;ids=allies().stream().map(BattleRosterS2C.Target::id).toList();resources.keySet().retainAll(ids);
  root=new WidgetGroup(0,0,w,h).setClientSideWidget();
  var background=new Widget(0,0,w,h){@Override public void drawInBackground(GuiGraphics g,int mx,int my,float partial){content(g,mx,my,partial);}};background.setClientSideWidget();root.addWidget(background);
  commands=new WidgetGroup(0,0,w,h).setClientSideWidget();var rects=BattleMenuLayout.main(w,h);
  for(int i=0;i<rects.length;i++){final int index=i;var r=rects[i];
   var button=new ButtonWidget(r.x(),r.y(),r.w(),r.h(),IGuiTexture.EMPTY,click->ClientBattleUiState.click(r.x()+r.w()/2.,r.y()+r.h()/2.,width,height,ClientBattleState.snapshot())){
    @Override public void drawInBackground(GuiGraphics g,int mx,int my,float partial){ReloadBattleDesign.command(canvas,view,index,r,r.contains(mx,my));}
   };button.setClientSideWidget();commands.addWidget(button);
  }root.addWidget(commands);
  party=new WidgetGroup(0,0,w,h).setClientSideWidget();int count=ids.size();
  for(int i=0;i<count;i++){UUID memberId=ids.get(i);var r=ReloadBattleDesign.party(w,h,count,i);double u=r.w()/114.;var initial=member(memberId);if(initial!=null)resources.putIfAbsent(memberId,new double[]{ratio(initial.hp(),initial.maxHp()),ratio(initial.sp(),initial.maxSp())});
   var card=new Widget(r.x(),r.y(),r.w(),r.h()){
    @Override public void drawInBackground(GuiGraphics g,int mx,int my,float partial){var t=member(memberId);if(t==null)return;ReloadBattleDesign.partyCard(canvas,r,t.name(),t.hp(),t.sp(),t.turn(),t.hp()==0);var world=Minecraft.m_91087_().f_91073_;var e=world==null?null:world.m_6815_(t.entityId());if(e instanceof net.minecraft.client.player.AbstractClientPlayer p)PlayerFace.draw(g,p,r.x()+r.w()-(int)(39*u),r.y()-(int)(7*u),(int)(30*u));}
   };card.setClientSideWidget();party.addWidget(card);
   for(int n=0;n<2;n++){final int resource=n;var progress=new ProgressWidget(()->resources.getOrDefault(memberId,new double[]{0,0})[resource],r.x()+(int)(14*u),r.y()+(int)((n==0?30:42)*u),(int)(91*u),Math.max(2,(int)(3*u))){
     @Override public void drawInBackground(GuiGraphics g,int mx,int my,float partial){var p=g.m_280168_();p.m_85836_();p.m_252880_(getPosition().x,getPosition().y,0);p.m_252781_(new Quaternionf().rotationZ((float)Math.toRadians(-9)));p.m_252880_(-getPosition().x,-getPosition().y,0);super.drawInBackground(g,mx,my,partial);p.m_85849_();}
    };progress.setProgressTexture(new ColorRectTexture(0xFF10253F),new ColorRectTexture(n==0?0xFFB9FF65:0xFF4CE8FF));progress.setClientSideWidget();party.addWidget(progress);
   }
  }root.addWidget(party);root.initWidget();
 }
 public static void render(GuiGraphics g,HudDesign.Canvas c,HudDesign.View v,int w,int h,float partial){
  canvas=c;view=v;if(root==null||w!=width||h!=height||!ids.equals(allies().stream().map(BattleRosterS2C.Target::id).toList()))build(w,h);
  showCommands=!BattleDirector.active()&&!ClientBattleUiState.selector()&&v.page().equals("MAIN");commands.setVisible(showCommands);commands.setActive(showCommands&&v.playerTurn()&&!v.locked());
  var world=Minecraft.m_91087_().f_91073_;long tick=world==null?0:world.m_46467_();
  if(tick!=lastTick){lastTick=tick;for(var id:ids){var t=member(id);if(t==null)continue;var value=resources.get(id);value[0]+=(ratio(t.hp(),t.maxHp())-value[0])*.35;value[1]+=(ratio(t.sp(),t.maxSp())-value[1])*.35;}root.updateScreen();}
  root.drawInBackground(g,(int)RuntimeBridge.guiMouseX(),(int)RuntimeBridge.guiMouseY(),partial);root.drawInForeground(g,(int)RuntimeBridge.guiMouseX(),(int)RuntimeBridge.guiMouseY(),partial);
 }
 private static void content(GuiGraphics g,int mx,int my,float partial){
  if(BattleDirector.active()){
   String skill=BattleDirector.skill();ReloadBattleDesign.skillBanner(canvas,skill.isEmpty()?"ATTACK":ClientBattleUiState.displaySkill(skill),width,height,BattleDirector.age(partial));return;
  }
  var s=ClientBattleState.snapshot();if(ClientBattleUiState.selector()){
   if(ClientBattleUiState.page()==ClientBattleUiState.Page.ITEMS)com.gx.velveteclipse.client.equipment.SupplyBattleHud.paint(canvas,width,height);
   else{var choices=s.skills().stream().map(id->com.gx.velveteclipse.core.persona.PersonaRegistry.skill(id).orElse(null)).filter(Objects::nonNull).map(skill->ClientBattleUiState.bonuses(s).displayCost(skill)).toList();PersonaBattleDesign.paint(canvas,new PersonaBattleDesign.View(s.personaName(),choices,ClientBattleUiState.selected(),s.hp(),s.maxHp(),s.sp(),ClientBattleUiState.actionLocked(),ClientBattleUiState.age()),width,height);}
  }else if(view.page().equals("MAIN"))ReloadBattleDesign.commandsBackdrop(canvas,view,width,height);
 }
 public static boolean click(double x,double y,int w,int h){if(root==null||width!=w||height!=h||!showCommands||BattleDirector.active()||!ClientBattleState.snapshot().playerTurn()||ClientBattleUiState.actionLocked())return false;boolean hit=commands.mouseClicked(x,y,0);commands.mouseReleased(x,y,0);return hit;}
 private static double ratio(int n,int max){return max<=0?0:Math.max(0,Math.min(1,n/(double)max));}
}
