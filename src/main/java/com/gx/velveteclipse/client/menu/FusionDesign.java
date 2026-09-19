package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.room.FusionSnapshot;
import com.gx.velveteclipse.menu.MenuSnapshot.Persona;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;
/** Shared geometry for actual drawing, hit targets and offline previews. */
public final class FusionDesign {
 public static Rect card(int i){return new Rect(56+(i%2)*20,130+i*60,365,51);}
 public static Rect row(int i){return new Rect(42,130+i*43,410,40);}
 public static Rect confirm(){return new Rect(680,477,208,22);}
 public static Rect tutorial(){return new Rect(44,379,405,25);}
 public record View(FusionSnapshot data,int page,int selected,int offset,String first,boolean waiting,double seconds){}
 public static void background(Canvas c,View v){
  c.fill(0,0,900,500,0xFF04052C);
  c.poly(new double[]{0,0,540,0,348,500,0,500},0xFF08237D);
  for(int i=0;i<10;i++){double x=i*140-300+(v.seconds()*8)%140;c.poly(new double[]{x,0,x+3,0,x+340,500,x+337,500},0x354874FF);}
  for(int y=0;y<6;y++)for(int x=0;x<7;x++)if((x+y)%2==0)c.poly(new double[]{x*160-100,y*110,x*160+60,y*110-25,x*160+140,y*110+60,x*160-20,y*110+85},0x160FC6FF);
  c.poly(new double[]{520,0,900,0,900,500,380,500},0xB0020527);
 }
 public static void foreground(Canvas c,View v){
  var d=v.data();c.poly(new double[]{0,21,510,0,491,82,0,106},0xFF111043);
  c.text(v.page()==0?"VELVET ROOM":v.page()==2?"BUSCA POR RESULTADO":v.page()==3?"PERSONAS POSSUÍDAS":"FUSÃO DE DUAS PERSONAS",38,39,27,WHITE,-3);
  // Compact player progress HUD: the level is independent from the active
  // Persona and the bar makes the current XP range immediately readable.
  int statX=40,statY=76,statW=310;
  c.fill(statX,statY,statW,28,0xD805123B);c.fill(statX,statY,3,28,CYAN);
  c.text("JOGADOR",statX+12,statY+4,9,CYAN,0);
  c.text("LV "+Math.max(1,d.level()),statX+87,statY+3,13,WHITE,0);
  int next=Math.max(1,d.nextXp()),xp=Math.max(0,Math.min(next,d.xp()));
  String xpLabel=d.level()>=99?"EXP MAX":"EXP "+xp+" / "+next;
  c.text(xpLabel,statX+140,statY+4,10,WHITE,0);
  c.fill(statX+12,statY+19,statW-24,5,0xFF182E59);
  c.fill(statX+12,statY+19,(int)Math.round((statW-24)*(d.level()>=99?1:xp/(double)next)),5,0xFF16E3D3);
  if(v.page()==0){String[] labels={"FUSÃO DE DUAS PERSONAS","BUSCA POR RESULTADO","PERSONAS POSSUÍDAS","VOLTAR À SALA"};
   for(int i=0;i<4;i++){var b=card(i);boolean selected=i==v.selected();c.poly(new double[]{b.x(),b.y()+4,b.x()+b.w(),b.y()-6,b.x()+b.w()-19,b.y()+b.h()-3,b.x()-12,b.y()+b.h()+7},selected?0xFF13ECDD:0xFF313AAF);c.text(labels[i],b.x()+13,b.y()+14,21,selected?INK:WHITE,-2);}
   var t=tutorial();c.fill(t.x(),t.y(),t.w(),t.h(),0xD8091743);c.fill(t.x(),t.y(),92,t.h(),CYAN);
   c.text("F1  AJUDA",t.x()+10,t.y()+7,10,INK,0);
   c.text("Como escolher e confirmar uma fusão",t.x()+103,t.y()+7,10,WHITE,0);
  }else{
   int count=v.page()==2?d.recipes().size():d.owned().size();
   for(int i=0;i<6&&i+v.offset()<count;i++){int k=i+v.offset();var b=row(i);boolean sel=k==v.selected();
    String title,sub;boolean locked=false,used=false;
    if(v.page()==2){var recipe=d.recipes().get(k);title="Lv "+recipe.level()+"  "+recipe.name();sub=name(d,recipe.first())+" + "+name(d,recipe.second());locked=recipe.level()>d.level();}
    else {var p=d.owned().get(k);title="Lv "+p.level()+"  "+p.name();sub=PersonaDetails.arcana(p.arcana());used=p.id().equals(v.first());if(used)sub="1ª PERSONA SELECIONADA";}
    c.fill(b.x()-3,b.y(),b.w()+6,b.h(),0xFF090726);c.fill(b.x(),b.y()+2,b.w(),b.h()-4,sel?0xFF14EADB:used?0xFF252548:0xFF31318D);
    c.text(fit(c,title,370,18),b.x()+10,b.y()+4,18,sel?INK:used?MUTED:WHITE,0);
    c.text(fit(c,(locked?"REQUER NÍVEL  ":"")+sub,385,10),b.x()+10,b.y()+25,10,sel?INK:locked?0xFFFF98AD:CYAN,0);
   }
   if(count==0)c.text("Nenhuma combinação disponível.",52,161,17,WHITE,0);
   if(v.page()==1&&d.owned().size()<2)c.text("Obtenha uma segunda Persona para fundir.",52,360,12,CYAN,0);
   c.text(count==0?"0 / 0":(v.selected()+1)+" / "+count+"   ↑ ↓  Rolar lista",46,391,10,CYAN,0);
   if(v.page()==1){c.text(v.first().isBlank()?"1":"2",766,312,91,CYAN,0);c.text("/ 2",814,373,28,WHITE,0);}
  }
  c.poly(new double[]{20,415,882,406,871,468,30,474},0xFF111353);
  c.text("IGOR",44,420,13,CYAN,0);
  String message=v.waiting()?"Consultando a fusão...":d.message();
  if(v.page()==1&&!v.waiting())message=v.first().isBlank()?"Escolha a primeira Persona.":"Escolha a segunda Persona. Nenhuma será consumida antes da confirmação.";
  var lines=wrap(c,message,505,12);for(int i=0;i<Math.min(2,lines.size());i++)c.text(lines.get(i),44,439+i*14,12,WHITE,0);
  // Fusion has no currency: the explicit cost is the two selected Personas,
  // consumed only after the confirmation request succeeds on the server.
  c.fill(584,421,275,43,0xD8050A31);c.fill(584,421,3,43,0xFF16E3D3);
  c.text("CUSTO",598,425,9,CYAN,0);
  String cost=v.first().isBlank()?"2 PERSONAS":"1ª + 2ª PERSONA";
  c.text(cost,598,438,12,WHITE,0);
  c.text("CONSUMO NA CONFIRMAÇÃO",598,453,8,MUTED,0);
  control(c,28,477,180,"ESC","VOLTAR");control(c,218,477,240,"↑ ↓","SELECIONAR");control(c,468,477,210,"ENTER","CONFIRMAR");control(c,688,477,170,"F1","AJUDA");
 }
 public static String name(FusionSnapshot d,String id){return d.owned().stream().filter(p->p.id().equals(id)).map(Persona::name).findFirst().orElse(id);}
 public static void fusionFooter(Canvas c,FusionSnapshot d,boolean waiting){
  c.fill(0,474,900,26,INK);c.text("ESC Voltar  /  F Afinidades",16,483,9,WHITE,0);
  String cost="2 PERSONAS: "+name(d,d.first())+" + "+name(d,d.second());c.text(fit(c,cost,414,10),244,483,10,CYAN,0);
  var b=confirm();c.fill(b.x(),b.y(),b.w(),b.h(),d.allowed()&&!waiting?RED:0xFF263655);
  c.text(waiting?"AGUARDE...":d.allowed()?"ENTER  Confirmar fusão":"REQUER JOGADOR Lv "+d.result().level(),b.x()+10,b.y()+5,11,WHITE,0);
 }
 public static void control(Canvas c,int x,int y,int w,String key,String label){c.poly(new double[]{x+5,y,x+w,y,x+w-5,y+21,x,y+21},0xFF23328A);c.fill(x+6,y+3,40,15,CYAN);if(key.equals("↑ ↓")){c.poly(new double[]{x+15,y+6,x+11,y+13,x+19,y+13},INK);c.poly(new double[]{x+30,y+15,x+26,y+8,x+34,y+8},INK);}else c.text(key,x+10,y+6,9,INK,0);c.text(label,x+53,y+6,10,WHITE,0);}
 public static void help(Canvas c){
  c.fill(0,0,900,500,0xE6020828);c.poly(new double[]{109,95,809,77,790,407,90,425},0xFF111353);
  c.text("FUSÃO • GUIA RÁPIDO",135,117,29,CYAN,-2);
  String[] lines={"1  Escolha duas Personas diferentes da sua lista.","2  Confira o resultado, as habilidades e as afinidades.","3  Seu nível de JOGADOR deve alcançar o nível do resultado.","4  ENTER confirma: as duas Personas dão lugar à nova.","A busca por resultado mostra as combinações disponíveis.","A experiência de jogador é obtida vencendo batalhas."};
  for(int i=0;i<lines.length;i++)c.text(lines[i],135,178+i*29,14,WHITE,0);
  control(c,135,374,225,"ESC","VOLTAR À FUSÃO");
 }
}
