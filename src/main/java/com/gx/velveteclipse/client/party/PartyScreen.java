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
  case 0->{if(data.members().isEmpty())rows.add(new Row("Criar party","Até "+PartyRoster.LIMIT+" jogadores • mapa compartilhado","create",""));else{for(var p:data.members())rows.add(new Row(p.name()+(p.id().equals(data.leader())?" • LÍDER":""),p.online()?"Online":"Offline",data.self().equals(data.leader())&&!p.id().equals(data.self())?"kick":"",p.id()));rows.add(new Row("Sair da party","O grupo permanece com os outros integrantes","leave",""));}}
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
 @Override public boolean m_6375_(double mx,double my,int button){if(button==1){m_7379_();return true;}if(button!=0)return true;double x=(mx-left())/scale(),y=(my-top())/scale();if(confirm){if(y>400)activate();return true;}if(x<220&&y>=136&&y<316){change((int)((y-136)/45));return true;}if(x>=245&&x<=615&&y>=135&&y<387){int n=offset+(int)((y-135)/36);if(n<rows().size())selected=n;}if(x>630&&y>230&&y<300)amountFocus=true;if(y>402&&x>620)activate();return true;}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){var p=g.m_280168_();p.m_85836_();p.m_85837_(left(),top(),0);p.m_85841_((float)scale(),(float)scale(),1);var c=new EquipmentCanvas(g,f_96547_);try{g.m_286007_(()->{
  c.fill(0,0,900,500,0xF004103A);c.poly(new double[]{0,28,900,0,875,100,0,124},0xFF1549E9);c.text("PARTY",31,43,48,MenuDesign.WHITE,-3);c.text("EXPLORAR JUNTOS",500,54,25,MenuDesign.CYAN,-3);
  String[] tabs={"INTEGRANTES","CONVIDAR","COMPARTILHAR","CONVITES"};for(int i=0;i<4;i++){c.fill(18,136+i*45,203,37,i==tab?MenuDesign.CYAN:0xFF082259);c.text(tabs[i],28,146+i*45,18,i==tab?MenuDesign.INK:MenuDesign.WHITE,0);}
  var rows=rows();for(int i=0;i<Math.min(7,rows.size()-offset);i++){var r=rows.get(i+offset);boolean focus=selected==i+offset;c.fill(245,135+i*36,367,34,focus?MenuDesign.WHITE:0xFF082559);c.text(MenuDesign.fit(c,r.label(),349,18),254,136+i*36,18,focus?MenuDesign.INK:MenuDesign.WHITE,0);c.text(MenuDesign.fit(c,r.detail(),349,11),254,157+i*36,11,focus?0xFF245E99:MenuDesign.CYAN,0);}
  if(rows.isEmpty())c.wrap(tab==3?"Nenhum convite pendente.":"Nenhum jogador disponível nesta lista.",254,154,22,340,4);
  String to=data.members().stream().filter(m->m.id().equals(recipient)).map(PartyNetwork.Person::name).findFirst().orElse("");c.wrap(tab==2&&!recipient.isEmpty()?"Enviar para "+to:"A party compartilha os setores e baús descobertos no mapa durante a Dark Hour.",635,147,19,235,6);
  if(tab==2&&!recipient.isEmpty()){c.text("QUANTIDADE",639,263,13,MenuDesign.CYAN,0);c.fill(635,282,229,32,amountFocus?0xFF163F96:0xFF072457);c.text(amount.isEmpty()?"0":amount,647,288,23,MenuDesign.WHITE,0);c.wrap("Digite um valor ou use ← / →. Equipamentos precisam estar desequipados.",638,327,14,228,4);}
  c.fill(632,406,236,35,MenuDesign.CYAN);c.text(waiting?"Aguarde…":confirm?"ENTER  Confirmar":"ENTER  Selecionar",645,416,19,MenuDesign.INK,0);
  if(confirm){c.fill(245,135,367,251,0xFA082051);var r=rows.get(selected);c.wrap("Confirmar: "+r.label()+(tab==2?" • quantidade "+amount+" • para "+to:""),267,193,24,325,6);c.text("ESC  Cancelar",270,354,17,MenuDesign.CYAN,0);}
  c.wrap(data.message(),246,449,14,626,1);c.text("Q / E  Aba   SETAS  Selecionar   ENTER  Ação   R  Atualizar   ESC  Voltar",25,478,13,MenuDesign.WHITE,0);
 });}finally{g.m_280262_();p.m_85849_();}}
}
