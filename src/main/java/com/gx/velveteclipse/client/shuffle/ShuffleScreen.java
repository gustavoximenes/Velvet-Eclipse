package com.gx.velveteclipse.client.shuffle;
import com.gx.velveteclipse.shuffle.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.client.ClientBattleState;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class ShuffleScreen extends Screen {
 private ShuffleSnapshot data;private int selected,offset;private boolean waiting,done;private String message="";private final double opened=now();private double sent;
 private final PersonaModelPreview model=new PersonaModelPreview();
 public ShuffleScreen(ShuffleSnapshot d){super(com.gx.velveteclipse.localization.ModText.component(d.screen().equals("skill")?"Skill Card":"Shuffle Time"));data=d;}
 private static double now(){return System.nanoTime()/1e9;}
 private boolean teaching(){return data.screen().equals("skill")||data.screen().equals("learned");}
 public void receive(ShuffleSnapshot d){waiting=false;message=d.message();if(d.screen().equals("claimed")||d.screen().equals("learned")){data=d;done=true;selected=offset=0;}}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_86600_(){if(ClientBattleState.snapshot().active()||f_96541_.f_91074_==null){m_7379_();return;}if(waiting&&now()-sent>8){waiting=false;message="Sem resposta. Você pode tentar novamente.";}}
 @Override public void m_7379_(){ShuffleNetwork.request("close","","");super.m_7379_();}
 private int count(){return teaching()?data.owned().size():data.choices().size();}
 private void move(int delta){selected=Math.floorMod(selected+delta,Math.max(1,count()));if(selected<offset)offset=selected;if(selected>=offset+7)offset=selected-6;}
 private boolean ready(){return !waiting&&(teaching()||now()-opened>=1.25);}
 private void choose(){if(!ready())return;if(done){m_7379_();return;}if(count()==0)return;waiting=true;sent=now();ShuffleNetwork.request(teaching()?"teach":"choose",data.token(),teaching()?data.owned().get(selected).id():String.valueOf(selected));}
 @Override public boolean m_7933_(int key,int scan,int mods){if(key==256){m_7379_();return true;}if(!ready())return true;if(key==257||key==335){choose();return true;}if(key==262||key==264){move(1);return true;}if(key==263||key==265){move(-1);return true;}if(key>=49&&key<=53&&key-49<count()){selected=key-49;return true;}return super.m_7933_(key,scan,mods);}
 private double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}private double left(){return (f_96543_-900*scale())/2;}private double top(){return (f_96544_-500*scale())/2;}
 @Override public boolean m_6375_(double x,double y,int button){if(button!=0||!ready())return true;double px=(x-left())/scale(),py=(y-top())/scale();if(ShuffleDesign.back().contains(px,py)){m_7379_();return true;}if(ShuffleDesign.confirm().contains(px,py)){choose();return true;}
  for(int i=0;i<(teaching()?Math.min(7,count()-offset):count());i++){var r=teaching()?ShuffleDesign.row(i):ShuffleDesign.card(i,count(),selected);if(r.contains(px,py)){int n=i+(teaching()?offset:0);if(n==selected)choose();else selected=n;return true;}}return true;
 }
 @Override public boolean m_6050_(double x,double y,double amount){if(ready())move(amount>0?-1:1);return true;}
 private void portrait(GuiGraphics g,String id,double x,double y,double w,double h){g.m_280588_((int)(left()+x*scale()),(int)(top()+y*scale()),(int)(left()+(x+w)*scale()),(int)(top()+(y+h)*scale()));try{model.drawCard(g,id,x,y,w,h);}finally{g.m_280618_();}}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){
  double time=now(),age=time-opened;model.advance(time,true);g.m_280509_(0,0,f_96543_,f_96544_,0xFF030C2D);var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);var c=new MenuCanvas(g);
  try{
   g.m_286007_(() -> ShuffleDesign.background(c,age,time,teaching()));String title="",detail="";
   if(teaching()){
    for(int i=0;i<Math.min(7,count()-offset);i++){var p=data.owned().get(offset+i);var r=ShuffleDesign.row(i);boolean knows=p.skills().stream().anyMatch(s->s.id().equals(data.skill()));c.fill(r.x(),r.y(),r.w(),r.h(),selected==offset+i?0xFF28E7E1:0xDD182A83);c.text(p.name()+"  Lv "+p.level(),r.x()+12,r.y()+8,18,selected==offset+i?MenuDesign.INK:MenuDesign.WHITE,0);if(knows)c.text("APRENDIDA",r.x()+329,r.y()+12,10,MenuDesign.MUTED,0);}
    if(count()>0){var p=data.owned().get(selected);portrait(g,p.id(),546,86,296,277);title=done?"HABILIDADE APRENDIDA":p.name();detail="Skill Card: "+PersonaRegistry.skill(data.skill()).map(s->s.displayName()).orElse(data.skill());}
   }else{
    double reveal=Math.max(0,Math.min(1,(age-.8)/.5));for(int i=0;i<count();i++){var choice=data.choices().get(i);var r=ShuffleDesign.card(i,count(),selected);final int index=i;g.m_286007_(() -> ShuffleDesign.card(c,choice,r,index==selected,reveal));if(choice.persona()!=null&&reveal>=1)portrait(g,choice.id(),r.x()+10,r.y()+38,r.w()-20,r.h()-71);}
    if(count()>0){var choice=data.choices().get(selected);title=done?"RECOMPENSA RECEBIDA":choice.title();detail=choice.detail();}
   }
   pose.m_85837_(0,0,MenuDepth.FOREGROUND);String label=title,description=detail;g.m_286007_(() -> {ShuffleDesign.footer(c,label,description,message,waiting,done);c.text("¥ "+data.yen()+"    SHUFFLE × "+ShuffleClient.count,590,38,13,MenuDesign.CYAN,0);if(!teaching())ShuffleDesign.intro(c,age);});
  }finally{g.m_280262_();pose.m_85849_();}
 }
}
