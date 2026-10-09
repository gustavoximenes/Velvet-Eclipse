package com.gx.velveteclipse.client.menu;

import com.gx.velveteclipse.room.*;
import com.gx.velveteclipse.core.fusion.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.menu.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

/** Every fusion opens the existing TAB inspector; only an acknowledged commit starts the film. */
public final class VelvetArchiveScreen extends Screen {
 private VelvetArchive.View data;
 private int page,selected,offset,recipient,combination,skill,affinity=-1,pick,inspectReturn;
 private boolean waiting,picker,readOnly,finished;
 private long sent;private double entered=System.nanoTime()/1e9;
 private String message="",sequence="";
 private MenuSnapshot.Persona inspected;
 private final List<PartyFusion.Source> sources=new ArrayList<>();
 private final LinkedHashSet<String> inherited=new LinkedHashSet<>();
 private final PersonaModelPreview model=new PersonaModelPreview();
 private final com.gx.velveteclipse.client.room.VelvetScene background=com.gx.velveteclipse.client.room.VelvetScene.load("menu");
 private final com.gx.velveteclipse.client.room.VelvetScene portrait=com.gx.velveteclipse.client.room.VelvetScene.load("portrait");
 private static final int HOME=0,NORMAL=1,SPECIAL=2,BOOK=3,PARTY=4,INSPECT=5,BUY=6,OFFER=7,SEARCH=8;
 public VelvetArchiveScreen(VelvetArchive.View data){super(Component.m_237113_("Velvet Room"));this.data=data;}
 public static void accept(String json){
  var d=MenuNetwork.JSON.fromJson(json,VelvetArchive.View.class);if(d==null)return;var mc=Minecraft.m_91087_();
  if(mc.f_91074_==null||!VelvetRoomService.inside(mc.f_91074_))return;
  if(d.open()){mc.m_91152_(new VelvetArchiveScreen(d));return;}
  if(!(mc.f_91080_ instanceof VelvetArchiveScreen s))return;
  s.waiting=false;s.data=d;s.message=d.message();
  if(d.fused()){
   if(d.sequence().equals(s.sequence))return;s.sequence=d.sequence();s.inspected=d.result();s.readOnly=s.finished=true;s.picker=false;s.page=INSPECT;s.skill=0;s.inherited.clear();
   mc.m_91152_(new com.gx.velveteclipse.client.room.VelvetFusionScreen(d,()->mc.m_91152_(s)));return;
  }
  if(d.result()!=null){s.inspected=d.result();s.readOnly=s.finished=false;s.picker=false;s.page=INSPECT;s.skill=0;s.affinity=-1;s.inherited.clear();s.entered=System.nanoTime()/1e9;}
  else if(s.page==INSPECT&&!s.readOnly){s.page=HOME;s.selected=s.offset=0;s.sources.clear();}
  if(s.page==NORMAL&&d.result()==null)s.sources.clear();s.bound();
 }
 private List<VelvetArchive.Stock> own(){return data.stock().stream().filter(x->x.owner().equals(data.self())).toList();}
 private List<FusionSearch.Option> results(){var map=new LinkedHashMap<String,FusionSearch.Option>();if(data.results()!=null)for(var r:data.results())map.putIfAbsent(r.id(),r);return List.copyOf(map.values());}
 private List<FusionSearch.Option> recipes(){var results=results();if(results.isEmpty())return List.of();String id=results.get(Math.min(selected,results.size()-1)).id();return data.results().stream().filter(r->r.id().equals(id)).toList();}
 private FusionSearch.Option chosenRecipe(){var recipes=recipes();return recipes.get(Math.floorMod(combination,recipes.size()));}
 private int count(){return switch(page){case HOME->5;case NORMAL->data.stock().size();case SPECIAL->data.recipes().size();case BOOK,BUY->data.book().size();case PARTY,OFFER->own().size();case SEARCH->results().size();default->0;};}
 private void bound(){selected=Math.max(0,Math.min(selected,count()-1));offset=Math.max(0,Math.min(offset,Math.max(0,count()-7)));if(selected<offset)offset=selected;if(selected>=offset+7)offset=selected-6;}
 private void request(String action,String result,String recipient){waiting=true;sent=System.currentTimeMillis();FusionNetwork.archiveRequest(new VelvetArchive.Request(action,result,List.copyOf(sources),List.copyOf(inherited),data.token(),recipient));}
 private static PartyFusion.Source source(VelvetArchive.Stock s){return new PartyFusion.Source(UUID.fromString(s.owner()),s.id());}
 private static String name(String id){return PersonaRegistry.persona(id).map(PersonaDefinition::displayName).orElse(id);}
 private void change(int value){page=value;selected=offset=0;sources.clear();inherited.clear();message="";}
 private void activate(){
  if(waiting)return;
  if(page==INSPECT){if(!readOnly){if(picker){toggle();return;}int base=inspected.skills().size();if(skill>=base&&skill<base+inherited.size()){inherited.remove(new ArrayList<>(inherited).get(skill-base));return;}if(skill>=base){picker=true;pick=0;}}return;}
  if(count()==0)return;
  switch(page){
   case HOME->{int next=new int[]{NORMAL,SPECIAL,SEARCH,BOOK,PARTY}[selected];change(next);if(next==SEARCH)request("search","0","");}
   case NORMAL->{var input=source(data.stock().get(selected));if(sources.remove(input))return;if(sources.stream().anyMatch(s->s.persona().equals(input.persona()))){message="Escolha Personas diferentes.";return;}sources.add(input);if(sources.size()==2)request("preview","","");}
   case SPECIAL->{var recipe=data.recipes().get(selected);sources.clear();for(String id:recipe.ingredients()){var s=data.stock().stream().filter(x->x.id().equals(id)).findFirst().orElse(null);if(s==null){message="Falta "+name(id)+". Obtenha a Persona ou uma contribuição autorizada.";sources.clear();return;}sources.add(source(s));}request("preview",recipe.id(),"");}
   case SEARCH->{var recipe=chosenRecipe();sources.clear();sources.addAll(recipe.sources());request("preview",recipe.special(),"");}
   case BOOK->{var entry=data.book().get(selected);if(entry.owned()){message="Você já possui esta Persona.";return;}if(data.yen()<entry.price()||data.ownedCount()>=data.capacity()){message="Confira o saldo e o espaço livre antes de invocar.";return;}page=BUY;}
   case PARTY->{if(data.party().isEmpty()){message="Os integrantes da party precisam estar na Velvet Room.";return;}page=OFFER;}
   case BUY,OFFER->confirm();
  }
 }
 private void toggle(){if(data.skills().isEmpty())return;String id=data.skills().get(pick).id();if(!inherited.remove(id)){if(inherited.size()==2){message="O limite é de duas habilidades herdadas.";return;}inherited.add(id);}picker=false;skill=inspected.skills().size()+Math.max(0,inherited.size()-1);}
 private void confirm(){if(waiting)return;
  if(page==INSPECT){if(readOnly){back();return;}if(!data.allowed()){message=data.message();return;}request("confirm",data.special(),"");}
  if(page==BUY){String id=data.book().get(selected).id();page=BOOK;request("summon",id,"");}
  if(page==OFFER&&!data.party().isEmpty()){String id=own().get(selected).id();page=PARTY;request("offer",id,data.party().get(recipient%data.party().size()).id());}
 }
 private void back(){if(waiting)return;if(picker){picker=false;return;}if(page==INSPECT&&readOnly&&!finished){page=inspectReturn;return;}if(page==BUY){page=BOOK;return;}if(page==OFFER){page=PARTY;return;}if(page==HOME){m_7379_();return;}change(HOME);request("cancel","","");}
 @Override public void m_7379_(){FusionNetwork.archiveRequest(new VelvetArchive.Request("close","",List.of(),List.of(),"",""));super.m_7379_();}
 private void stand(){FusionNetwork.archiveRequest(new VelvetArchive.Request("stand","",List.of(),List.of(),"",""));super.m_7379_();}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(f_96541_.f_91074_==null||!VelvetRoomService.inside(f_96541_.f_91074_)){m_7379_();return;}if(waiting&&System.currentTimeMillis()-sent>8000){waiting=false;message="Sem resposta. Atualize com R antes de confirmar novamente.";}}
 @Override public boolean m_7933_(int key,int scan,int mods){
  if(key==340||key==344){stand();return true;}if(key==256){back();return true;}if(waiting)return true;
  if(key==82){request("refresh","","");return true;}
  if(page==INSPECT){
   if(key==67){confirm();return true;}if(key==65||key==68){model.rotate(key==68?1:-1);return true;}
   if(key==70){affinity=affinity<0?0:-1;return true;}
   if(key==265||key==264||key==263||key==262){int dir=key==265||key==263?-1:1;if(picker)pick=Math.floorMod(pick+dir,Math.max(1,data.skills().size()));else if(affinity>=0)affinity=Math.floorMod(affinity+dir,PersonaDetails.ELEMENTS.length);else skill=Math.floorMod(skill+dir,Math.max(1,inspected.skills().size()+(readOnly?0:2)));return true;}
  }else{
   if(key==88&&page==BOOK&&count()>0){inspected=data.book().get(selected).persona();if(inspected!=null){inspectReturn=BOOK;page=INSPECT;readOnly=true;finished=false;skill=0;affinity=-1;}return true;}
   if(key==88&&page==PARTY&&count()>0){request("withdraw",own().get(selected).id(),"");return true;}
   if(page==SEARCH&&(key==266||key==267)){int target=data.resultOffset()+(key==267?16:-16);if(target>=0&&target<data.resultCount()){selected=offset=combination=0;request("search",""+target,"");}return true;}
   if(key==263||key==262||key==65||key==68){int direction=key==262||key==68?1:-1;if(page==PARTY||page==OFFER)recipient=Math.floorMod(recipient+direction,Math.max(1,data.party().size()));else if(page==SEARCH){combination=Math.floorMod(combination+direction,Math.max(1,recipes().size()));}else model.rotate(direction);return true;}
   if(key==265||key==264){selected=Math.floorMod(selected+(key==264?1:-1),Math.max(1,count()));bound();return true;}
  }
  if(key==257||key==335){activate();return true;}return super.m_7933_(key,scan,mods);
 }
 private double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}private double left(){return(f_96543_-900*scale())/2;}private double top(){return(f_96544_-500*scale())/2;}
 @Override public boolean m_6375_(double x,double y,int button){if(button!=0||waiting)return true;double mx=(x-left())/scale(),my=(y-top())/scale();
  if(my>=474){if(mx<140)back();else if(mx>680)confirm();else activate();return true;}
  if(page==INSPECT){if(picker){int i=(int)((my-152)/32)+Math.max(0,pick-5);if(mx>486&&mx<872&&i>=0&&i<data.skills().size()){pick=i;toggle();}return true;}int i=PersonaSheetDesign.pickSkill(mx,my);if(i>=0){skill=(skill/8)*8+i;activate();}else{int a=PersonaSheetDesign.pickAffinity(mx,my);if(a>=0)affinity=a;}return true;}
  if(page==BUY||page==OFFER){if(my>317&&my<356)confirm();return true;}
  int index=page==HOME?VelvetArchiveDesign.pickCard(mx,my):VelvetArchiveDesign.pickRow(mx,my);if(index>=0){int row=page==HOME?index:index+offset;if(row<count()){selected=row;activate();}}return true;
 }
 @Override public boolean m_6050_(double x,double y,double amount){if(!waiting){if(page==INSPECT){if(picker)pick=Math.max(0,Math.min(data.skills().size()-1,pick+(amount>0?-1:1)));else skill=Math.max(0,Math.min(inspected.skills().size()+1,skill+(amount>0?-1:1)));}else{selected+=amount>0?-1:1;bound();}}return true;}
 private MenuSnapshot.Persona sheetPersona(){var p=inspected;var skills=new ArrayList<>(p.skills());if(!readOnly)for(String id:inherited)PersonaRegistry.skill(id).ifPresent(skills::add);return new MenuSnapshot.Persona(p.id(),p.name(),p.arcana(),p.level(),p.xp(),p.nextXp(),p.stats(),skills,p.affinities());}
 private String previewId(){if(count()==0)return "";return switch(page){case NORMAL->data.stock().get(selected).id();case SPECIAL->data.recipes().get(selected).id();case BOOK,BUY->data.book().get(selected).id();case PARTY,OFFER->own().get(selected).id();case SEARCH->results().get(selected).id();default->"";};}
 private List<VelvetArchiveDesign.Row> rows(){var list=new ArrayList<VelvetArchiveDesign.Row>();for(int i=0;i<count();i++){String title="",detail="";switch(page){case NORMAL->{var s=data.stock().get(i);title=(sources.contains(source(s))?"✓ ":"")+"Lv "+s.level()+"   "+s.name();detail="Dono: "+s.ownerName();}case SPECIAL->{var r=data.recipes().get(i);title="Lv "+r.level()+"   "+r.name();detail=String.join(" + ",r.ingredients().stream().map(VelvetArchiveScreen::name).toList());}case BOOK,BUY->{var b=data.book().get(i);title=String.format("Lv %02d   %s",b.level(),b.name());detail=b.owned()?"POSSUÍDA":"¥ "+b.price();}case PARTY,OFFER->{var s=own().get(i);title=s.name();detail=s.offered()?"CONTRIBUIÇÃO AUTORIZADA":"ENTER autorizar • X retirar";}case SEARCH->{var r=results().get(i);title="Lv "+r.level()+"   "+r.name();detail=data.results().stream().filter(o->o.id().equals(r.id())).count()+" combinações";}}list.add(new VelvetArchiveDesign.Row(title,detail));}return list;}
 @Override public void m_88315_(GuiGraphics g,int mouseX,int mouseY,float partial){
  double time=System.nanoTime()/1e9;model.advance(time,true);g.m_280509_(0,0,f_96543_,f_96544_,0xFF030421);
  if(page!=INSPECT&&background!=null)background.draw(g,f_96543_,f_96544_,0,"",0);
  if(page==HOME&&portrait!=null){
   g.m_280588_((int)(left()+470*scale()),(int)(top()+65*scale()),(int)(left()+890*scale()),(int)(top()+451*scale()));
   try{g.m_280509_((int)(left()+470*scale()),(int)(top()+65*scale()),(int)(left()+890*scale()),(int)(top()+451*scale()),0xCF0D237C);portrait.draw(g,f_96543_,f_96544_,0,"",0);}finally{g.m_280618_();}
  }
  var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);var c=new MenuCanvas(g);
  try{
   if(page==INSPECT&&inspected!=null){
    var p=sheetPersona();var view=new PersonaSheetDesign.View(p,false,0,1,skill,affinity,time-entered,time,true,readOnly?(finished?"FUSÃO CONCLUÍDA":"COMPÊNDIO"):"HERANÇA  "+inherited.size()+" / 2",model.error(p.id()));
    g.m_286007_(() ->PersonaSheetDesign.background(c,view));g.m_280588_((int)(left()+396*scale()),(int)top(),(int)Math.ceil(left()+900*scale()),(int)Math.ceil(top()+474*scale()));try{model.draw(g,view);}finally{g.m_280618_();}
    pose.m_85837_(0,0,MenuDepth.FOREGROUND);g.m_286007_(() ->{
     PersonaSheetDesign.foreground(c,view,false);
     if(!readOnly){for(int i=p.skills().size();i<inspected.skills().size()+2;i++){if(i/8!=skill/8)continue;var r=PersonaSheetDesign.skill(i%8);c.fill(r.x(),r.y(),r.w(),r.h(),i==skill?CYAN:0xFF153B62);c.text("+ Adicionar habilidade",r.x()+9,r.y()+5,12,i==skill?INK:CYAN,0);}}
     VelvetArchiveDesign.footer(c,"ESC Voltar    F Afinidades    A / D Girar",readOnly?"C Continuar":"ENTER Adicionar / remover    C Confirmar fusão");
     if(!message.isBlank()||waiting){c.fill(510,86,373,30,0xEE041031);c.text(fit(c,waiting?"Confirmando no servidor...":message,355,10),520,97,10,CYAN,0);}
     if(picker){c.fill(480,116,401,339,0xF0051037);c.text("HERDAR HABILIDADE",495,124,20,CYAN,0);int start=Math.max(0,pick-5);for(int i=0;i<8&&i+start<data.skills().size();i++){var s=data.skills().get(i+start);int y=152+i*32;c.fill(491,y,379,29,i+start==pick?CYAN:0xFF202C68);c.text((inherited.contains(s.id())?"✓ ":"")+s.displayName(),502,y+4,15,i+start==pick?INK:WHITE,0);c.text(PersonaDetails.cost(s),735,y+8,10,i+start==pick?INK:MUTED,0);}if(data.skills().isEmpty())c.text("Nenhuma habilidade adicional.",499,172,14,WHITE,0);c.text("ENTER Escolher   ESC Voltar ao inspect",497,435,12,CYAN,0);}
    });
   }else{
    String title=switch(page){case NORMAL->"FUSÃO NORMAL";case SPECIAL->"FUSÃO ESPECIAL";case SEARCH->"BUSCA POR RESULTADO";case BOOK,BUY->"COMPÊNDIO";default->"CONTRIBUIÇÃO";};
    g.m_286007_(() ->{if(page!=HOME)VelvetArchiveDesign.listBackground(c,title,page==SEARCH?"Resultados possíveis com suas Personas e contribuições.":page==BOOK||page==BUY?"Personas registradas • seu progresso é preservado.":"Escolha as Personas e confira o resultado no inspect.",page==SEARCH?data.resultCount()+" RESULTADOS DISPONÍVEIS":"JOGADOR Lv "+data.level()+"       YEN "+data.yen());});
    String id=previewId();if(!id.isEmpty()&&page!=SEARCH)model.drawCard(g,id,524,148,340,199);
    pose.m_85837_(0,0,MenuDepth.FOREGROUND);g.m_286007_(() ->{
     if(page==HOME)VelvetArchiveDesign.home(c,selected);else{
      VelvetArchiveDesign.rows(c,rows(),selected,offset,page==SEARCH?data.resultCount():count(),page==SEARCH?data.resultOffset():0);
      c.text(fit(c,name(id).toUpperCase(Locale.ROOT),335,25),520,115,25,WHITE,0);
      if(page==BOOK||page==BUY){var b=count()==0?null:data.book().get(selected);if(b!=null){c.text(PersonaDetails.arcana(b.persona().arcana())+"  /  Lv "+b.level(),525,145,12,CYAN,0);c.poly(new double[]{519,355,878,346,873,427,515,439},0xEF050C2D);c.text("CUSTO",531,363,10,MUTED,0);c.text("¥ "+b.price(),532,384,20,WHITE,0);c.text("ESPAÇO",738,361,10,MUTED,0);c.text(data.ownedCount()+" / "+data.capacity()+" Personas",738,385,14,CYAN,0);c.text("ENTER Invocar   X Inspect",578,416,15,CYAN,0);}}
      if(page==SEARCH&&count()>0){
       var available=recipes();int chosen=Math.floorMod(combination,available.size()),first=Math.max(0,chosen-3);
       c.text("FUSÕES DISPONÍVEIS",528,158,13,CYAN,0);
       for(int i=first;i<Math.min(available.size(),first+4);i++){var result=available.get(i);int y=183+(i-first)*56;c.fill(521,y,353,52,i==chosen?0xFF103958:0xEB0C1A43);if(i==chosen)c.fill(519,y,2,52,CYAN);c.text(result.special().isBlank()?"FUSÃO NORMAL":"FUSÃO ESPECIAL",532,y+5,10,CYAN,0);var names=wrap(c,String.join(" + ",result.sources().stream().map(s->name(s.persona())).toList()),329,12);for(int j=0;j<Math.min(2,names.size());j++)c.text(names.get(j),532,y+23+j*13,12,WHITE,0);}
       c.text("ENTER abre o inspect / herança",531,420,13,CYAN,0);c.text("A / D Combinação   PgUp / PgDn Página",531,442,10,MUTED,0);
      }
      if(page==PARTY||page==OFFER){c.fill(520,350,360,61,0xED081745);c.text("DESTINATÁRIO  ← / →",533,359,12,CYAN,0);c.text(data.party().isEmpty()?"Nenhum integrante próximo":data.party().get(recipient%data.party().size()).name(),533,384,16,WHITE,0);}
      VelvetArchiveDesign.footer(c,"ESC Voltar    ENTER Selecionar    R Atualizar",page==SEARCH?"A / D Combinação   PgUp / PgDn Página":page==BOOK?"X Inspect     SHIFT Levantar":"SHIFT Levantar");
     }
     if(waiting||!message.isBlank()){c.fill(23,444,854,27,0xED040A26);c.text(fit(c,waiting?"Consultando a Velvet Room...":message,826,11),35,452,11,CYAN,0);}
     if(page==BUY&&count()>0){var b=data.book().get(selected);VelvetArchiveDesign.dialog(c,"INVOCAR "+b.name().toUpperCase(Locale.ROOT)+"?","Custo: ¥ "+b.price()+"  •  Saldo após: ¥ "+(data.yen()-b.price()),"A Persona registrada será adicionada à sua lista.");}
     if(page==OFFER&&count()>0)VelvetArchiveDesign.dialog(c,"AUTORIZAR CONTRIBUIÇÃO",own().get(selected).name()+" para "+data.party().get(recipient%data.party().size()).name(),"Só será consumida quando o destinatário confirmar a fusão.");
    });
   }
  }finally{g.m_280262_();pose.m_85849_();}
 }
}
