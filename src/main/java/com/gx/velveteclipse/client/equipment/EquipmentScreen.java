package com.gx.velveteclipse.client.equipment;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.client.menu.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public final class EquipmentScreen extends Screen {
 private static EquipmentNetwork.Snapshot latest=new EquipmentNetwork.Snapshot(EquipmentLoadout.starter(),0,"sync","",-1,false,"");
 public static EquipmentLoadout bag(){return latest.bag();}
 public static EquipmentCatalog.Family family(){return bag().bonuses().family();}
 private EquipmentNetwork.Snapshot data;private final Screen parent;private final ElizabethPreview portrait=new ElizabethPreview();
 private boolean dialogue,shop,tip,help;private int slot,selected,offset,tipIndex,family;private double changed=now();private boolean waiting;
 private static final String[] TIPS={
  "Você começa com espaço para duas Personas. O Contrato de Igor amplia esse limite em um, até oito. A primeira vitória contra a Sacerdotisa concede um contrato.",
  "As Permissões de Compra ampliam meu estoque. A primeira vem da Sacerdotisa; as próximas estão nos baús dos andares 50, 75 e seguintes. Use cada item para registrar sua permissão.",
  "A Velvet Room acompanha o crescimento da sua alma. Igor pode fundir duas Personas para criar uma nova. Seu nível determina quais fusões você consegue realizar.",
  "Abra o Tab para equipar Personas e equipamentos. Armaduras, calçados e acessórios alteram seus atributos sem substituir sua aparência. Com H, você empunha a categoria de arma equipada.",
  "Teste os elementos contra cada inimigo. As afinidades desconhecidas aparecem com interrogações. O bestiário guarda os inimigos vistos, as vitórias e tudo que você descobriu.",
  "Atacar uma Shadow pelas costas concede vantagem à equipe. Aproximar-se pela frente exige cautela: se ela atacar primeiro, os inimigos começam a batalha.",
  "Uma fraqueza ou um golpe crítico pode derrubar o alvo e conceder 1 More. Use Shift para passar a vantagem a um aliado. Com todos os inimigos derrubados, o All-Out Attack fica disponível.",
  "Uma vitória decisiva com All-Out Attack concede Shuffle Time. Fora da batalha, pressione P para escolher sua recompensa. Cartas de Persona e de habilidade acompanham o andar explorado.",
  "A Dark Hour dura trinta minutos. O labirinto comum se reorganiza no próximo ciclo. Guardiões derrotados, tesouros fixos e terminais descobertos preservam seu progresso.",
  "Os guardiões bloqueiam a subida a cada dez andares. As salas de transição surgem a cada vinte e cinco. Use os terminais já descobertos para preparar sua próxima expedição.",
  "As Shadows deixam yen. Aqui no pavilhão, posso trocar esse dinheiro por equipamentos e suprimentos. Os itens de recuperação podem ser usados em você ou em um aliado durante seu turno."
 };
 public EquipmentScreen(EquipmentNetwork.Snapshot d,Screen parent){super(com.gx.velveteclipse.localization.ModText.component("Elizabeth • Equipamentos"));data=d;this.parent=parent;dialogue=d.view().equals("dialogue");shop=d.view().equals("shop");if(d.view().equals("items"))slot=4;}
 private static double now(){return System.nanoTime()*1e-9;}
 public static void accept(String json){
  var d=EquipmentNetwork.JSON.fromJson(json,EquipmentNetwork.Snapshot.class);if(d==null||d.bag()==null)return;d.bag().sanitize();latest=d;var mc=Minecraft.m_91087_();
  if(mc.f_91080_ instanceof PersonaMenuScreen&&!d.message().isBlank()&&(d.view().equals("sync")||d.view().equals("update")))PersonaMenuScreen.equipmentUpdated(d.message());
  if(d.view().equals("closed")){if(mc.f_91080_ instanceof EquipmentScreen)mc.m_91152_(null);return;}
  if(d.view().equals("dialogue")||d.view().equals("equip")||d.view().equals("items")){if(!com.gx.velveteclipse.client.ClientBattleState.snapshot().active())mc.m_91152_(new EquipmentScreen(d,mc.f_91080_));return;}
  if(mc.f_91080_ instanceof EquipmentScreen s){if(d.view().equals("sync"))s.data=new EquipmentNetwork.Snapshot(d.bag(),d.yen(),s.data.view(),s.data.message(),s.data.npc(),s.data.merchant(),s.data.token(),d.shopTier());else s.data=d;s.waiting=false;if(d.view().equals("shop")){s.dialogue=false;s.shop=true;s.selected=s.offset=0;}if(!s.dialogue)s.bound();}
 }
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(f_96541_.f_91074_==null||com.gx.velveteclipse.client.ClientBattleState.snapshot().active()){f_96541_.m_91152_(null);return;}if(shop||dialogue){var e=f_96541_.f_91073_.m_6815_(data.npc());if(e==null||f_96541_.f_91074_.m_20280_(e)>49)f_96541_.m_91152_(null);}if(waiting&&now()-changed>5){waiting=false;request("sync","");}}
 private void request(String action,String id){waiting=true;changed=now();EquipmentNetwork.request(action,id,data.token());}
 private List<String> choices(){if(slot==4)return Supplies.all().stream().filter(s->shop||data.bag().count(s.id())>0).map(Supplies.Supply::id).toList();var category=EquipmentCatalog.Slot.values()[slot];return EquipmentCatalog.all().stream().filter(g->g.slot()==category&&(shop?ShopStock.available(g.id(),data.shopTier()):data.bag().owned.contains(g.id()))&&(slot!=0||family==0||g.family().ordinal()==family)).sorted(Comparator.comparingInt(EquipmentCatalog.Gear::value).thenComparing(EquipmentCatalog.Gear::name)).map(EquipmentCatalog.Gear::id).toList();}
 private void bound(){int n=choices().size();selected=Math.max(0,Math.min(selected,n-1));if(selected<offset)offset=selected;if(selected>=offset+8)offset=selected-7;offset=Math.max(0,Math.min(offset,Math.max(0,n-8)));}
 @Override public void m_7379_(){if(help){help=false;return;}if(tip){tip=false;return;}if(shop){dialogue=true;shop=false;selected=0;return;}f_96541_.m_91152_(parent instanceof EquipmentScreen?null:parent);}
 private void activate(){if(waiting||help)return;if(dialogue){if(selected==0){tip=true;tipIndex=0;changed=now();}else if(data.merchant()&&selected==1){if(com.gx.velveteclipse.client.city.CityScreen.worldEnabled())request("heal","");else request("shop","");}else if(data.merchant()&&selected==2&&!com.gx.velveteclipse.client.city.CityScreen.worldEnabled())request("heal","");else m_7379_();return;}var list=choices();if(list.isEmpty())return;request(shop?"buy":slot==4?"use":"equip",list.get(selected));}
 @Override public boolean m_7933_(int key,int scan,int mods){
  if(key==290&&!dialogue){help=!help;return true;}
  if(help){if(key==256||key==259||key==257)help=false;return true;}
  if(key==256||key==259){m_7379_();return true;}
  if(tip){if(key==257||key==335||key==262){tipIndex++;changed=now();if(tipIndex>=TIPS.length)tip=false;}else if(key==263)tipIndex=Math.max(0,tipIndex-1);return true;}
  if(key==257||key==335){activate();return true;}
  if(key==264||key==265||key==266||key==267){int d=key==264?1:key==265?-1:key==267?8:-8;selected=dialogue?Math.floorMod(selected+d,data.merchant()?(com.gx.velveteclipse.client.city.CityScreen.worldEnabled()?3:4):2):Math.max(0,selected+d);if(!dialogue)bound();return true;}
  if(!dialogue&&(key==81||key==69||key==263||key==262)){slot=Math.floorMod(slot+(key==69||key==262?1:-1),5);selected=offset=0;return true;}
  if(!dialogue&&key==70&&slot==0){family=(family+1)%EquipmentCatalog.Family.values().length;selected=offset=0;return true;}
  if(!dialogue&&!shop&&key==88&&slot<4){request("remove",EquipmentCatalog.Slot.values()[slot].name());return true;}return true;
 }
 @Override public boolean m_6050_(double x,double y,double d){if(!dialogue){selected+=d<0?1:-1;bound();}return true;}
 private double scale(){return Math.min(f_96543_/900.,f_96544_/500.);}private double left(){return(f_96543_-900*scale())/2;}private double top(){return(f_96544_-500*scale())/2;}
 @Override public boolean m_6375_(double mx,double my,int b){if(b==1){m_7379_();return true;}if(b!=0)return true;double x=(mx-left())/scale(),y=(my-top())/scale();if(help){help=false;return true;}if(tip){m_7933_(257,0,0);return true;}if(dialogue){int i=(int)((y-180)/36);if(x>=380&&x<=820&&i>=0&&i<(data.merchant()?(com.gx.velveteclipse.client.city.CityScreen.worldEnabled()?3:4):2)){selected=i;activate();}return true;}if(x<220&&y>=130&&y<355){slot=(int)((y-130)/45);selected=offset=0;return true;}if(x>=250&&x<=540&&y>=135&&y<399){int i=(int)((y-135)/33)+offset;if(i<choices().size())selected=i;}if(x>=565&&y>=405)activate();return true;}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){
  var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);
  var c=new EquipmentCanvas(g,f_96547_);double t=now();
  try{
   g.m_286007_(()->{c.fill(0,0,900,500,0xF2031034);for(int i=0;i<8;i++){double x=i*170-120+Math.sin(t*.2)*15;c.poly(new double[]{x,0,x+70,0,x-160,500,x-230,500},0x991328B0);}c.poly(new double[]{0,30,890,0,854,98,0,123},0xFF103DDE);c.text(dialogue?"VELVET ROOM":shop?"ELIZABETH'S SHOP":"EQUIPMENT",35,40,42,MenuDesign.WHITE,-3);c.text("¥ "+data.yen(),660,90,20,MenuDesign.CYAN,0);});
   if(dialogue){portrait.draw(g,t);pose.m_85837_(0,0,MenuDepth.FOREGROUND);g.m_286007_(()->drawDialogue(c,t));}
   else g.m_286007_(()->{drawEquipment(c);if(help)drawHelp(c);});
  }finally{g.m_280262_();pose.m_85849_();}
 }
 private String tipText(){if(com.gx.velveteclipse.client.city.CityScreen.worldEnabled()){if(tipIndex==1)return "As Permissões de Compra ampliam o estoque da polícia em Paulownia Mall. Use cada permissão obtida nas suas expedições.";if(tipIndex==10)return "As lojas de Paulownia Mall fornecem equipamentos e suprimentos. Use M na cidade para abrir o mapa e viajar até elas.";}return TIPS[Math.min(tipIndex,TIPS.length-1)];}
 private void drawDialogue(EquipmentCanvas c,double t){
  if(!tip){String[] options=data.merchant()&&com.gx.velveteclipse.client.city.CityScreen.worldEnabled()?new String[]{"Sobre a Velvet Room","Restaurar HP e SP • grátis","Até logo"}:data.merchant()?new String[]{"Sobre a Velvet Room","Comprar equipamentos e itens","Restaurar HP e SP • grátis","Até logo"}:new String[]{"Aprender sobre a jornada","Até logo"};for(int i=0;i<options.length;i++){c.poly(new double[]{370,175+i*36,830,175+i*36,814,209+i*36,354,209+i*36},selected==i?MenuDesign.CYAN:0xDD062460);c.text(options[i],390,183+i*36,21,selected==i?MenuDesign.INK:MenuDesign.WHITE,0);}}
  c.poly(new double[]{207,352,878,324,858,465,240,477,180,381},0xFF134DFF);c.poly(new double[]{224,365,859,342,840,449,252,460,202,384},0xFF030E30);
  c.text("Elizabeth",240,345,22,MenuDesign.CYAN,-2);c.text("T A L K",242,371,10,MenuDesign.CYAN,0);
  String text=!tip&&!data.message().isBlank()?data.message():tip?tipText():data.merchant()?"Bem-vindo. Posso apresentar a Velvet Room ou ajudá-lo a se preparar para a próxima expedição.":"É um prazer acompanhá-lo. Permita-me compartilhar algumas orientações para a sua jornada.";
  c.wrap(text,251,391,18,575,4);c.text(tip?"ENTER  Próximo   "+(tipIndex+1)+" / "+TIPS.length:"ENTER  Conversar    ESC  Voltar",270,479,13,MenuDesign.WHITE,0);
 }
 private void drawHelp(EquipmentCanvas c){var list=choices();if(list.isEmpty())return;String id=list.get(selected);var gear=EquipmentCatalog.get(id);var item=Supplies.get(id);c.fill(95,105,710,350,0xFC05123A);c.poly(new double[]{95,105,805,90,792,148,95,166},0xFF174BE6);c.text("HABILIDADE / EFEITO",125,119,29,MenuDesign.CYAN,-2);c.wrap(gear==null?item.name():gear.name(),125,181,26,650,2);c.wrap(gear==null?item.description():EquipmentHelp.describe(gear.effect()),125,243,19,644,8);c.text("F1 / ESC / ENTER  Voltar",125,424,16,MenuDesign.CYAN,0);}
 private void drawEquipment(EquipmentCanvas c){
  if(shop)c.text("ESTOQUE • "+ShopStock.period(data.shopTier()),252,112,12,MenuDesign.CYAN,0);
  for(int i=0;i<5;i++){c.poly(new double[]{15,130+i*45,224,130+i*45,211,168+i*45,15,168+i*45},slot==i?MenuDesign.CYAN:0xAA052466);c.text(i==4?"ITENS":EquipmentCatalog.label(EquipmentCatalog.Slot.values()[i]),27,143+i*45,19,slot==i?MenuDesign.INK:MenuDesign.WHITE,0);}
  var list=choices();for(int i=0;i<Math.min(8,list.size()-offset);i++){String id=list.get(offset+i);var gear=EquipmentCatalog.get(id);var item=Supplies.get(id);boolean focus=selected==offset+i;c.fill(246,134+i*33,296,31,focus?MenuDesign.WHITE:0xAA001438);c.text(gear==null?item.name():gear.name(),255,137+i*33,16,focus?MenuDesign.INK:MenuDesign.WHITE,0);String tag=shop?"¥ "+(gear==null?item.price():gear.price()):gear==null?"x"+data.bag().count(id):data.bag().equipped.containsValue(id)?"EQUIPADO":gear.metric()+" "+gear.value();c.text(tag,260,154+i*33,11,focus?0xFF126BAD:MenuDesign.CYAN,0);}
  c.text(list.isEmpty()?"Nenhum item nesta categoria":(selected+1)+" / "+list.size(),252,405,13,MenuDesign.CYAN,0);
  if(slot==0)c.text("F  Tipo: "+(family==0?"TODOS":EquipmentCatalog.Family.values()[family]),250,425,13,MenuDesign.WHITE,0);
  if(!list.isEmpty()){
   String id=list.get(selected);var gear=EquipmentCatalog.get(id);var item=Supplies.get(id);c.wrap(gear==null?item.name():gear.name(),572,139,26,302,2);
   if(gear!=null){var equipped=EquipmentCatalog.get(data.bag().equipped.get(gear.slot()));c.text(gear.metric()+"  "+gear.value(),580,201,26,MenuDesign.CYAN,0);if(equipped!=null){int delta=gear.value()-equipped.value();c.text("Atual: "+equipped.value()+"   "+(delta>=0?"+":"")+delta,580,236,18,delta>=0?0xFF70FFAB:0xFFFF799C,0);}c.wrap(EquipmentText.effect(gear.effect()),574,278,16,295,6);}
   else c.wrap(item.description(),574,225,17,295,7);
   c.poly(new double[]{559,404,880,404,865,441,550,441},MenuDesign.CYAN);c.text(waiting?"Aguarde…":shop?"ENTER  Comprar":slot==4?"ENTER  Usar":"ENTER  Equipar",574,414,22,MenuDesign.INK,0);
  }
  c.text("Q / E  Categoria    ↑ / ↓  Selecionar    PgUp / PgDn  Página    X  Remover    F1  Efeito    ESC  Voltar",25,475,13,MenuDesign.WHITE,0);
  if(!data.message().isBlank())c.wrap(data.message(),255,448,14,625,1);
 }
}
