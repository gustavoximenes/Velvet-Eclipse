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
  c.text("JOGADOR  Lv "+d.level()+"   /   EXP "+d.xp()+" / "+d.nextXp(),40,96,12,CYAN,0);
  if(v.page()==0){String[] labels={"FUSÃO DE DUAS PERSONAS","BUSCA POR RESULTADO","PERSONAS POSSUÍDAS","VOLTAR À SALA"};
   for(int i=0;i<4;i++){var b=card(i);boolean selected=i==v.selected();c.poly(new double[]{b.x(),b.y()+4,b.x()+b.w(),b.y()-6,b.x()+b.w()-19,b.y()+b.h()-3,b.x()-12,b.y()+b.h()+7},selected?0xFF13ECDD:0xFF313AAF);c.text(labels[i],b.x()+13,b.y()+14,21,selected?INK:WHITE,-2);}
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
  var lines=wrap(c,message,798,12);for(int i=0;i<Math.min(2,lines.size());i++)c.text(lines.get(i),44,439+i*14,12,WHITE,0);
  c.text("ESC  Voltar     ↑ ↓  Selecionar     ENTER  Confirmar",28,484,11,WHITE,0);
 }
 public static String name(FusionSnapshot d,String id){return d.owned().stream().filter(p->p.id().equals(id)).map(Persona::name).findFirst().orElse(id);}
 public static void fusionFooter(Canvas c,FusionSnapshot d,boolean waiting){
  c.fill(0,474,900,26,INK);c.text("ESC  Voltar   /   F  Afinidades",20,483,10,WHITE,0);
  String cost=name(d,d.first())+" + "+name(d,d.second());c.text(fit(c,cost,336,10),310,483,10,CYAN,0);
  var b=confirm();c.fill(b.x(),b.y(),b.w(),b.h(),d.allowed()&&!waiting?RED:0xFF263655);
  c.text(waiting?"AGUARDE...":d.allowed()?"ENTER  Confirmar fusão":"REQUER JOGADOR Lv "+d.result().level(),b.x()+10,b.y()+5,11,WHITE,0);
 }
}
