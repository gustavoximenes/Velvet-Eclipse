package com.gx.velveteclipse.client.party;
import com.gx.velveteclipse.party.*;
import com.gx.velveteclipse.client.equipment.EquipmentCanvas;
import com.gx.velveteclipse.client.menu.MenuDesign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
/** Keyboard and mouse share the same rows and explicit transfer confirmation. */
public final class PartyScreen extends Screen {
 private PartyNetwork.View data;private final Screen parent;private int tab,selected,offset;private String recipient="",amount="1";private boolean amountFocus,waiting,confirm;private long requested;
 private record Row(String label,String detail,String action,String id){}
 private PartyScreen(PartyNetwork.View data,Screen parent){super(Component.m_237113_("Party"));this.data=data;this.parent=parent;}
 public static void accept(String json){var v=PartyNetwork.JSON.fromJson(json,PartyNetwork.View.class);if(v==null)return;var mc=Minecraft.m_91087_();if(mc.f_91080_ instanceof PartyScreen s){s.data=v;s.waiting=false;s.bound();}else if(v.open()&&!com.gx.velveteclipse.client.ClientBattleState.snapshot().active())mc.m_91152_(new PartyScreen(v,mc.f_91080_));}
 private List<Row> rows(){var rows=new ArrayList<Row>();switch(tab){
  case 0->{if(data.members().isEmpty())rows.add(new Row("Criar party","Até "+data.maximumPlayers()+" jogadores • mapa compartilhado","create",""));else{for(var p:data.members())rows.add(new Row(p.name()+(p.id().equals(data.leader())?" • LÍDER":""),p.online()?"Online":"Offline",data.self().equals(data.leader())&&!p.id().equals(data.self())?"kick":"",p.id()));rows.add(new Row("Sair da party","O grupo permanece com os outros integrantes","leave",""));}}
  case 1->{for(var p:data.online())rows.add(new Row(p.name(),"Enviar convite • requer aceite","invite",p.id()));}
  case 2->{if(recipient.isEmpty()){for(var p:data.members())if(p.online()&&!p.id().equals(data.self()))rows.add(new Row(p.name(),"Escolher destinatário","recipient",p.id()));}else{rows.add(new Row("Yen","Seu saldo: ¥ "+data.yen(),"yen",""));for(var item:data.items())rows.add(new Row(item.name(),"Disponível: "+item.count()+(item.id().startsWith("i:")?" • inventário":" • coleção"),"item",item.id()));}}
  case 3->{if(!data.inviter().isEmpty()){rows.add(new Row("Aceitar convite",data.inviter()+" convidou você","accept",""));rows.add(new Row("Recusar convite","Permanecer sem esse grupo","decline",""));}}
 }return rows;}
 private void bound(){selected=Math.max(0,Math.min(selected,rows().size()-1));offset=Math.max(0,Math.min(offset,selected));if(selected>=offset+7)offset=selected-6;}
 private void request(String action,String target,String item){if(waiting)return;long n;try{n=Long.parseLong(amount);}catch(NumberFormatException e){n=0;}PartyNetwork.request(action,target,item,n,data.token());waiting=true;requested=System.currentTimeMillis();confirm=false;}
 private void activate(){if(waiting||rows().isEmpty())return;var r=rows().get(selected);if(r.action().equals("recipient")){recipient=r.id();selected=offset=0;return;}if(r.action().isEmpty())return;if(Set.of("yen","item","kick","leave").contains(r.action())&&!confirm){confirm=true;return;}request(r.action(),tab==2?recipient:r.id(),tab==2?r.id():"");}
 private void change(int value){tab=Math.floorMod(value,4);selected=offset=0;recipient="";confirm=amountFocus=false;}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(com.gx.velveteclipse.client.ClientBattleState.snapshot().active()||f_96541_.f_91074_==null){f_96541_.m_91152_(null);return;}if(waiting&&System.currentTimeMillis()-requested>5000){waiting=false;request("refresh","","");}}
 @Override public void m_7379_(){if(confirm){confirm=false;return;}if(!recipient.isEmpty()){recipient="";selected=offset=0;return;}f_96541_.m_91152_(parent);}
 @Override public boolean m_7933_(int key,int scan,int mods){if(key==256){m_7379_();return true;}if(waiting)return true;if(key==257||key==335){activate();return true;}if(confirm)return true;if(key==81||key==69){change(tab+(key==69?1:-1));return true;}if(key==264||key==265){selected+=key==264?1:-1;bound();return true;}if(key==82){request("refresh","","");return true;}if(tab==2){if(key==258){amountFocus=!amountFocus;return true;}if(key==259){amount=amount.length()>1?amount.substring(0,amount.length()-1):"0";return true;}if(key==262||key==263){try{amount=Long.toString(Math.max(1,Math.min(9_999_999_999L,Long.parseLong(amount)+(key==262?1:-1)*(mods==1?100:1))));}catch(NumberFormatException ignored){}return true;}}return true;}
 @Override public boolean m_5534_(char ch,int mods){if(tab==2&&!confirm&&ch>='0'&&ch<='9'){if(!amountFocus){amount="";amountFocus=true;}if(amount.length()<10)amount=(amount.equals("0")?"":amount)+ch;return true;}return true;}
 @Override public boolean m_6050_(double x,double y,double d){if(!confirm){selected+=d<0?1:-1;bound();}return true;}
 private double scale(){return Math.min(f_96543_/900.,f_96544_/500.);}private double left(){return(f_96543_-900*scale())/2;}private double top(){return(f_96544_-500*scale())/2;}
 @Override public boolean m_6375_(double mx,double my,int button){
  if(button==1){m_7379_();return true;}if(button!=0)return true;double x=(mx-left())/scale(),y=(my-top())/scale();
  if(ReloadPartyDesign.action().contains(x,y)){activate();return true;}if(confirm)return true;
  for(int i=0;i<4;i++)if(ReloadPartyDesign.tab(i).contains(x,y)){change(i);return true;}
  for(int i=0;i<7;i++)if(ReloadPartyDesign.row(i).contains(x,y)&&offset+i<rows().size()){boolean again=selected==offset+i;selected=offset+i;if(again)activate();return true;}
  if(tab==2&&ReloadPartyDesign.amount().contains(x,y))amountFocus=true;return true;
 }
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){
  var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);
  var canvas=new com.gx.velveteclipse.client.menu.MenuCanvas(g);double seconds=System.nanoTime()*1e-9;
  try{g.m_286007_(() -> com.gx.velveteclipse.client.menu.ReloadMenuDesign.backdrop(canvas,seconds,true));
   var displayed=rows().stream().map(r->new ReloadPartyDesign.Row(r.label(),r.detail())).toList();
   String recipientName=data.members().stream().filter(m->m.id().equals(recipient)).map(PartyNetwork.Person::name).findFirst().orElse("");
   pose.m_85837_(0,0,com.gx.velveteclipse.client.menu.MenuDepth.FOREGROUND);
   g.m_286007_(() -> ReloadPartyDesign.paint(canvas,tab,displayed,selected,offset,data.members().size(),data.maximumPlayers(),recipientName,amount,amountFocus,waiting,confirm,data.message()));
  }finally{g.m_280262_();pose.m_85849_();}
 }
}
