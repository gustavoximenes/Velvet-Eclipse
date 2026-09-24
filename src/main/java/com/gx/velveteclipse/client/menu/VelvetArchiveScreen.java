package com.gx.velveteclipse.client.menu;

import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.core.fusion.PartyFusion;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.menu.MenuNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** Igor's fusion, cooperative contribution and Compendium share one keyboard/mouse UI. */
public final class VelvetArchiveScreen extends Screen {
 private VelvetArchive.View data;private int page,selected,offset,recipient;private boolean waiting,help;
 private long sent;private String message="";private final List<PartyFusion.Source> sources=new ArrayList<>();private final LinkedHashSet<String> inherited=new LinkedHashSet<>();
 private final PersonaModelPreview model=new PersonaModelPreview();private final IgorPreview igor=new IgorPreview();
 private static final String[] HOME={"FUSÃO NORMAL","FUSÃO ESPECIAL","COMPENDIUM","CONTRIBUIR COM A PARTY","VOLTAR À SALA"};
 public VelvetArchiveScreen(VelvetArchive.View d){super(Component.m_237113_("Velvet Room • Igor"));data=d;}
 public static void accept(String json){var d=MenuNetwork.JSON.fromJson(json,VelvetArchive.View.class);if(d==null)return;var mc=Minecraft.m_91087_();if(mc.f_91074_==null||!VelvetRoomService.inside(mc.f_91074_))return;
  if(d.open()){mc.m_91152_(new VelvetArchiveScreen(d));return;}if(mc.f_91080_ instanceof VelvetArchiveScreen s){s.data=d;s.waiting=false;s.message=d.message();if(d.result()!=null){s.page=5;s.selected=s.offset=0;s.inherited.clear();}else if(s.page==5){s.page=0;s.selected=s.offset=0;s.sources.clear();}if(d.result()==null&&s.page==1)s.sources.clear();s.bound();}
 }
 private List<VelvetArchive.Stock> own(){return data.stock().stream().filter(s->s.owner().equals(data.self())).toList();}
 private int count(){return switch(page){case 0->HOME.length;case 1->data.stock().size();case 2->data.recipes().size();case 3->data.book().size();case 4->own().size();case 5->data.skills().size();default->0;};}
 private void bound(){selected=Math.max(0,Math.min(selected,count()-1));offset=Math.max(0,Math.min(offset,Math.max(0,count()-7)));if(selected<offset)offset=selected;if(selected>=offset+7)offset=selected-6;}
 private void request(String action,String result,String target){waiting=true;sent=System.currentTimeMillis();FusionNetwork.archiveRequest(new VelvetArchive.Request(action,result,List.copyOf(sources),List.copyOf(inherited),data.token(),target));}
 private static PartyFusion.Source source(VelvetArchive.Stock s){return new PartyFusion.Source(UUID.fromString(s.owner()),s.id());}
 private void activate(){
  if(waiting||count()==0)return;
  if(page==0){if(selected==4){m_7379_();return;}page=selected+1;selected=offset=0;sources.clear();inherited.clear();return;}
  if(page==1){var s=source(data.stock().get(selected));if(sources.contains(s)){sources.remove(s);return;}if(sources.stream().anyMatch(x->x.persona().equals(s.persona()))){message="Selecione duas Personas diferentes.";return;}sources.add(s);if(sources.size()==2)request("preview","","");return;}
  if(page==2){var recipe=data.recipes().get(selected);sources.clear();for(String id:recipe.ingredients()){var found=data.stock().stream().filter(s->s.id().equals(id)).findFirst().orElse(null);if(found==null){message="Falta "+name(id)+". Obtenha a Persona ou peça uma contribuição da party.";sources.clear();return;}sources.add(source(found));}request("preview",recipe.id(),"");return;}
  if(page==3){var entry=data.book().get(selected);if(entry.owned()){message="Você já possui esta Persona.";return;}if(data.yen()<entry.price()){message="Yen insuficiente.";return;}page=6;return;}
  if(page==4){if(data.party().isEmpty()){message="Os integrantes da party precisam estar próximos de Igor.";return;}page=7;return;}
  if(page==5){String id=data.skills().get(selected).id();if(!inherited.remove(id)){if(inherited.size()<2)inherited.add(id);else message="O limite é de duas habilidades herdadas.";}}
 }
 private static String name(String id){return PersonaRegistry.persona(id).map(PersonaDefinition::displayName).orElse(id);}
 private void confirm(){
  if(waiting)return;
  if(page==5&&data.allowed())request("confirm",data.special(),"");
  else if(page==6){var e=data.book().get(selected);page=3;request("summon",e.id(),"");}
  else if(page==7){if(data.party().isEmpty())return;var e=own().get(selected);var to=data.party().get(recipient%data.party().size());page=4;request("offer",e.id(),to.id());}
 }
 private void back(){if(waiting)return;if(page==6){page=3;return;}if(page==7){page=4;return;}if(page==0){m_7379_();return;}page=selected=offset=0;sources.clear();inherited.clear();request("cancel","","");}
 @Override public void m_7379_(){FusionNetwork.archiveRequest(new VelvetArchive.Request("close","",List.of(),List.of(),"",""));super.m_7379_();}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(f_96541_.f_91074_==null||!VelvetRoomService.inside(f_96541_.f_91074_)||com.gx.velveteclipse.client.ClientBattleState.snapshot().active()){m_7379_();return;}if(waiting&&System.currentTimeMillis()-sent>8000){waiting=false;message="Sem resposta. Pressione R para atualizar antes de tentar novamente.";}}
 @Override public boolean m_7933_(int key,int scan,int mods){
  if(key==290){help=!help;return true;}if(help){if(key==256||key==257)help=false;return true;}if(key==256){back();return true;}if(waiting)return true;
  if(key==82){request("refresh","","");return true;}
  if(key==263||key==262){if(page==4||page==7)recipient=Math.floorMod(recipient+(key==262?1:-1),Math.max(1,data.party().size()));else model.rotate(key==262?1:-1);return true;}
  if(key==70){confirm();return true;}
  if(key==88&&page==4&&count()>0){request("withdraw",own().get(selected).id(),"");return true;}
  if(key==257||key==335){if(page==6||page==7)confirm();else activate();return true;}
  if((key==265||key==264)&&page<6){selected=Math.floorMod(selected+(key==265?-1:1),Math.max(1,count()));bound();return true;}
  return super.m_7933_(key,scan,mods);
 }
 private double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}private double left(){return(f_96543_-900*scale())/2;}private double top(){return(f_96544_-500*scale())/2;}
 @Override public boolean m_6375_(double x,double y,int button){if(button!=0||waiting)return true;double mx=(x-left())/scale(),my=(y-top())/scale();if(help){help=false;return true;}if(my>=473){if(mx<190)back();else if(mx>680)confirm();else if(mx>490)request("refresh","","");else activate();return true;}if(page==6||page==7){if(my>330&&my<380&&mx>480)confirm();return true;}for(int i=0;i<7;i++)if(new Rect(32,112+i*41,488,38).contains(mx,my)&&i+offset<count()){selected=i+offset;activate();return true;}if(page==4&&mx>535&&my>322&&my<368)recipient=(recipient+1)%Math.max(1,data.party().size());return true;}
 @Override public boolean m_6050_(double x,double y,double amount){if(!waiting&&page<6){selected+=amount>0?-1:1;bound();}return true;}
 private String title(){return page==0?"VELVET ROOM":page==1?"FUSÃO NORMAL":page==2?"FUSÃO ESPECIAL":page==3||page==6?"COMPENDIUM":page==4||page==7?"CONTRIBUIÇÃO DA PARTY":"HERDAR HABILIDADES";}
 private String rowTitle(int n){return switch(page){case 0->HOME[n];case 1->{var s=data.stock().get(n);yield(sources.contains(source(s))?"✓  ":"")+"Lv "+s.level()+"  "+s.name();}case 2->{var r=data.recipes().get(n);yield "Lv "+r.level()+"  "+r.name();}case 3->{var b=data.book().get(n);yield "Lv "+b.level()+"  "+b.name();}case 4->{var s=own().get(n);yield(s.offered()?"OFERECIDA  ":"")+s.name();}case 5->{var s=data.skills().get(n);yield(inherited.contains(s.id())?"✓  ":"□  ")+s.displayName();}default->"";};}
 private String rowSub(int n){return switch(page){case 1->"Dono: "+data.stock().get(n).ownerName();case 2->data.recipes().get(n).ingredients().size()+" Personas • "+String.join(" + ",data.recipes().get(n).ingredients().stream().map(VelvetArchiveScreen::name).toList());case 3->data.book().get(n).owned()?"JÁ POSSUÍDA":"¥ "+data.book().get(n).price();case 4->"ENTER autorizar • X retirar contribuição";case 5->PersonaDetails.cost(data.skills().get(n));default->"";};}
 private String previewId(){if(data.result()!=null&&page==5)return data.result().id();if(count()==0)return "";return switch(page){case 1->data.stock().get(selected).id();case 2->data.recipes().get(selected).id();case 3->data.book().get(selected).id();case 4->own().get(selected).id();default->"";};}
 @Override public void m_88315_(GuiGraphics g,int mouseX,int mouseY,float partial){
  double time=System.nanoTime()/1e9;model.advance(time,true);var pose=g.m_280168_();g.m_280509_(0,0,f_96543_,f_96544_,INK);pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);var c=new MenuCanvas(g);
  try{
   g.m_286007_(() -> {c.fill(0,0,900,500,0xFF070B34);c.poly(new double[]{0,0,670,0,355,500,0,500},0xFF102884);c.poly(new double[]{0,25,880,2,865,83,0,106},0xFF0B1655);c.text(title(),32,34,30,WHITE,-2);c.text("JOGADOR Lv "+data.level()+"     ¥ "+data.yen(),35,84,12,CYAN,0);});
   String modelId=previewId();if(!modelId.isBlank())model.drawCard(g,modelId,535,113,330,226);else if(page==0)igor.draw(g,time);
   pose.m_85837_(0,0,MenuDepth.FOREGROUND);
   g.m_286007_(() -> {
    if(page<6){for(int i=0;i<7&&i+offset<count();i++){int n=i+offset;boolean active=n==selected;double y=112+i*41;c.fill(32,y,488,38,active?CYAN:0xE8192862);c.text(fit(c,rowTitle(n),450,17),44,y+3,17,active?INK:WHITE,0);c.text(fit(c,rowSub(n),450,9),44,y+25,9,active?INK:MUTED,0);}if(count()==0)c.text(page==5?"Nenhuma habilidade adicional para herdar.":"Nenhum registro disponível.",40,132,15,WHITE,0);
     c.text(count()==0?"0 / 0":(selected+1)+" / "+count()+"    ↑ ↓ Rolar",38,404,10,CYAN,0);
    }
    if(page==5&&data.result()!=null){c.text(data.result().name(),548,113,24,WHITE,0);c.text("RESULTADO • Lv "+data.result().level(),550,145,11,CYAN,0);c.text("HERANÇA  "+inherited.size()+" / 2",550,337,16,CYAN,0);int y=360;for(var s:data.sources()){String owner=data.stock().stream().filter(x->x.owner().equals(s.owner().toString())&&x.id().equals(s.persona())).map(VelvetArchive.Stock::ownerName).findFirst().orElse("Jogador");c.text(fit(c,name(s.persona())+" • "+owner,320,10),550,y,10,WHITE,0);y+=13;}}
    if(page==4||page==7){String target=data.party().isEmpty()?"Nenhum integrante próximo":data.party().get(recipient%data.party().size()).name();c.fill(540,322,325,48,0xFF193778);c.text("DESTINATÁRIO   ← / →",550,329,10,CYAN,0);c.text(fit(c,target,300,16),550,347,16,WHITE,0);}
    if(page==6||page==7){c.fill(60,145,780,160,0xFF101E54);c.text(page==6?"CONFIRMAR INVOCAÇÃO":"AUTORIZAR CONTRIBUIÇÃO",86,164,25,CYAN,0);String text=page==6?data.book().get(selected).name()+" • ¥ "+data.book().get(selected).price():own().get(selected).name()+" será consumida se o destinatário confirmar a fusão.";var lines=wrap(c,text,710,17);for(int i=0;i<lines.size();i++)c.text(lines.get(i),86,210+i*22,17,WHITE,0);c.text("ENTER confirmar     ESC cancelar",86,271,13,WHITE,0);}
    c.fill(20,422,860,47,0xFF081331);c.text("IGOR",34,428,10,CYAN,0);String note=waiting?"Consultando a Velvet Room...":!message.isBlank()?message:data.message();var lines=wrap(c,note,826,11);for(int i=0;i<Math.min(2,lines.size());i++)c.text(lines.get(i),34,442+i*12,11,WHITE,0);
    FusionDesign.control(c,20,475,165,"ESC","VOLTAR");FusionDesign.control(c,205,475,265,"ENTER",page==5?"MARCAR HABILIDADE":"SELECIONAR");FusionDesign.control(c,490,475,170,"R","ATUALIZAR");FusionDesign.control(c,680,475,200,"F",page==5?"CONFIRMAR FUSÃO":page>=6?"CONFIRMAR":"F1 AJUDA");
    if(help){c.fill(24,108,852,308,0xF0071437);c.text("VELVET ROOM • AJUDA",48,130,26,CYAN,0);String[] lines2={"Fusão normal: selecione duas Personas diferentes.","Fusão especial: a lista mostra todas as fontes exigidas.","Na prévia, marque até duas skills e pressione F para fundir.","Contribuição: escolha uma Persona e o destinatário da party.","A autorização dura 3 minutos; X retira sua oferta.","Compendium: registre ao adquirir; recompre com yen e espaço livre.","F1 / ESC fecha esta ajuda."};for(int i=0;i<lines2.length;i++)c.text(lines2[i],48,184+i*27,14,WHITE,0);}
   });
  }finally{g.m_280262_();pose.m_85849_();}
 }
}
