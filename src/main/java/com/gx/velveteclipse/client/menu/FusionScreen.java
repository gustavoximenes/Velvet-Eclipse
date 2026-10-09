package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.menu.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
public final class FusionScreen extends Screen {
 private FusionSnapshot data;private int page,selected,offset,previous=1;private String first="";
 private boolean waiting,confirming,fusionPreview,help;private double sent,changed=now();
 private final PersonaSheetState sheet=new PersonaSheetState();private final PersonaModelPreview model=new PersonaModelPreview();private final IgorPreview igor=new IgorPreview();
 public FusionScreen(FusionSnapshot d){super(Component.m_237113_("Velvet Room • Igor"));data=d;}
 private static double now(){return System.nanoTime()/1e9;}
 public static void accept(String json){
  var d=MenuNetwork.JSON.fromJson(json,FusionSnapshot.class);if(d==null)return;var mc=Minecraft.m_91087_();
  if(mc.f_91074_==null||!VelvetRoomService.inside(mc.f_91074_))return;
  if(d.open()){mc.m_91152_(new FusionScreen(d));return;}
  if(mc.f_91080_ instanceof FusionScreen screen)screen.receive(d);
 }
 private void receive(FusionSnapshot d){boolean wasConfirming=confirming;data=d;waiting=confirming=false;changed=now();
  if(d.result()!=null){fusionPreview=true;sheet.show(List.of(d.result()),0);page=4;}
  else {sheet.close();fusionPreview=false;first="";if(wasConfirming){page=3;selected=offset=0;}else if(page==4){page=1;selected=offset=0;}bound();}
 }
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){
  if(f_96541_.f_91074_==null||!VelvetRoomService.inside(f_96541_.f_91074_)||com.gx.velveteclipse.client.ClientBattleState.snapshot().active()){m_7379_();return;}
  if(waiting&&now()-sent>8){waiting=confirming=false;FusionNetwork.request("open","","","");}
 }
 private double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}private double left(){return (f_96543_-900*scale())/2;}private double top(){return (f_96544_-500*scale())/2;}
 private int count(){return page==0?4:page==2?data.recipes().size():data.owned().size();}
 private void bound(){selected=Math.max(0,Math.min(selected,count()-1));offset=Math.max(0,Math.min(offset,Math.max(0,count()-6)));if(selected<offset)offset=selected;if(selected>=offset+6)offset=selected-5;}
 private List<MenuSnapshot.Persona> inspecting(){return fusionPreview&&data.result()!=null?List.of(data.result()):data.owned();}
 private void request(String action,String a,String b){waiting=true;sent=now();FusionNetwork.request(action,a,b,action.equals("confirm")?data.token():"");}
 private void activate(){
  if(waiting)return;
  if(page==4){if(fusionPreview&&data.allowed()){confirming=true;request("confirm","","");}else if(!fusionPreview)back();return;}
  if(page==0){if(selected==3){m_7379_();return;}page=selected+1;selected=offset=0;first="";changed=now();return;}
  if(count()==0)return;
  if(page==2){var r=data.recipes().get(selected);previous=2;request("preview",r.first(),r.second());return;}
  if(page==3){fusionPreview=false;sheet.show(data.owned(),selected);previous=3;page=4;changed=now();return;}
  String id=data.owned().get(selected).id();if(first.isBlank()){first=id;selected=(selected+1)%count();bound();}
  else if(!first.equals(id)){previous=1;request("preview",first,id);}
 }
 private void back(){
  if(waiting)return;
  if(page==4){sheet.close();page=previous;fusionPreview=false;first="";FusionNetwork.request("cancel","","","");waiting=false;}
  else if(!first.isBlank()){first="";}
  else if(page!=0){page=0;selected=offset=0;}
  else m_7379_();
 }
 @Override public void m_7379_(){FusionNetwork.request("close","","","");super.m_7379_();}
 @Override public boolean m_7933_(int key,int scan,int mods){
  if(key==290){help=!help;return true;}
  if(help){if(key==256||key==257)help=false;return true;}
  if(key==256){back();return true;}
  if(waiting)return true;
  if(key==257||key==335){activate();return true;}
  if(page==4){var p=sheet.persona(inspecting());if(p==null)return true;
   switch(key){case 65->model.rotate(-1);case 68->model.rotate(1);case 70->sheet.toggleAffinity();case 265->sheet.move(p,0,-1);case 264->sheet.move(p,0,1);case 263->sheet.move(p,-1,0);case 262->sheet.move(p,1,0);case 81->{if(!fusionPreview)sheet.cycle(inspecting(),-1);}case 69->{if(!fusionPreview)sheet.cycle(inspecting(),1);}case 88->back();default->{}}
   return true;
  }
  if(key==265||key==264){selected=Math.floorMod(selected+(key==265?-1:1),Math.max(1,count()));bound();return true;}
  return super.m_7933_(key,scan,mods);
 }
 @Override public boolean m_6375_(double x,double y,int button){
  if(button!=0||waiting)return true;double mx=(x-left())/scale(),my=(y-top())/scale();
  if(help){help=false;return true;}
  if(page!=4&&((page==0&&FusionDesign.tutorial().contains(mx,my))||my>=477&&mx>=688)){help=true;return true;}
  if(my>=477&&mx<208){back();return true;}
  if(page!=4&&my>=477&&mx>=468&&mx<678){activate();return true;}
  if(page==4){var p=sheet.persona(inspecting());if(p==null)return true;
   if(FusionDesign.confirm().contains(mx,my)){activate();return true;}
   int skill=PersonaSheetDesign.pickSkill(mx,my),affinity=PersonaSheetDesign.pickAffinity(mx,my);
   if(skill>=0)sheet.selectSkill(p,sheet.page()*8+skill);if(affinity>=0)sheet.selectAffinity(affinity);
   if(!fusionPreview){if(PersonaSheetDesign.previous().contains(mx,my))sheet.cycle(inspecting(),-1);if(PersonaSheetDesign.next().contains(mx,my))sheet.cycle(inspecting(),1);}return true;
  }
  int size=page==0?4:6;for(int i=0;i<size;i++){var rect=page==0?FusionDesign.card(i):FusionDesign.row(i);if(rect.contains(mx,my)&&i+(page==0?0:offset)<count()){selected=i+(page==0?0:offset);activate();return true;}}
  return true;
 }
 @Override public boolean m_6050_(double x,double y,double amount){if(waiting)return true;if(page==4){var p=sheet.persona(inspecting());if(p!=null)sheet.step(p,amount>0?-1:1);}else{selected+=amount>0?-1:1;bound();}return true;}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){
  double time=now();model.advance(time,true);var pose=g.m_280168_();g.m_280509_(0,0,f_96543_,f_96544_,0xFF03062D);pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);var c=new MenuCanvas(g);
  try{
   if(page==4){var p=sheet.persona(inspecting());if(p==null)return;
    var view=new PersonaSheetDesign.View(p,false,sheet.index(inspecting()),inspecting().size(),sheet.skill(),sheet.affinity(),time-changed,time,true,fusionPreview?data.message():"PERSONA POSSUÍDA",model.error(p.id()));
    g.m_286007_(() -> PersonaSheetDesign.background(c,view));g.m_280588_((int)(left()+396*scale()),(int)top(),(int)Math.ceil(left()+900*scale()),(int)Math.ceil(top()+474*scale()));try{model.draw(g,view);}finally{g.m_280618_();}
    pose.m_85837_(0,0,MenuDepth.FOREGROUND);g.m_286007_(() -> {PersonaSheetDesign.foreground(c,view,!fusionPreview);if(fusionPreview)FusionDesign.fusionFooter(c,data,waiting);else{c.fill(0,475,900,25,MenuDesign.INK);c.text("ESC  Voltar   Q / E  Persona   F  Afinidades   A / D  Girar modelo",22,483,11,MenuDesign.WHITE,0);}});
   }else{
    var view=new FusionDesign.View(data,page,selected,offset,first,waiting,time);
    g.m_286007_(() -> FusionDesign.background(c,view));igor.draw(g,time);pose.m_85837_(0,0,MenuDepth.FOREGROUND);g.m_286007_(() -> FusionDesign.foreground(c,view));
   }
   if(help){pose.m_85837_(0,0,20);g.m_286007_(() -> FusionDesign.help(c));}
  }finally{g.m_280262_();pose.m_85849_();}
 }
}
