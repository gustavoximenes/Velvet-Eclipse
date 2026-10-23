package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.client.vfx.ClientAudio;
import com.gx.velveteclipse.presentation.AudioPreferences.Bus;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

/** Six independent sliders, keyboard and mouse, stored only on this client. */
public final class AudioSettingsScreen extends Screen {
 private final Screen parent;private int selected,drag=-1;private boolean failed;
 public AudioSettingsScreen(Screen parent){super(Component.m_237113_("Audio"));this.parent=parent;}
 private String tr(String en,String pt){return com.gx.velveteclipse.client.localization.ClientLocalization.code().equals("pt_br")?pt:en;}
 private double scale(){return Math.min(f_96543_/800.,f_96544_/500.);}
 private double left(){return (f_96543_-800*scale())/2;}
 private double top(){return (f_96544_-500*scale())/2;}
 private double value(int row){return row==0?f_96541_.f_91066_.m_92147_(SoundSource.MUSIC):ClientAudio.volume(Bus.values()[row-1]);}
 private void set(int row,double value){value=Math.round(Math.max(0,Math.min(1,value))*100)/100.;if(row==0){f_96541_.f_91066_.m_246669_(SoundSource.MUSIC).m_231514_(value);f_96541_.f_91066_.m_92169_();}else failed=!ClientAudio.set(Bus.values()[row-1],(float)value);}
 @Override public boolean m_7043_(){return false;}
 @Override public void m_7379_(){f_96541_.m_91152_(parent);}
 @Override public boolean m_7933_(int key,int scan,int mods){if(key==256){m_7379_();return true;}if(key==265||key==264){selected=Math.floorMod(selected+(key==264?1:-1),6);return true;}if(key==263||key==262){set(selected,value(selected)+(key==262?.05:-.05));return true;}if(key==268||key==269){set(selected,key==268?0:1);return true;}return super.m_7933_(key,scan,mods);}
 @Override public boolean m_6375_(double mx,double my,int button){if(button!=0)return false;double x=(mx-left())/scale(),y=(my-top())/scale();if(y>452){m_7379_();return true;}for(int i=0;i<6;i++)if(y>=100+i*50&&y<140+i*50){selected=i;if(x>=355&&x<=710){drag=i;set(i,(x-370)/320);}return true;}return false;}
 @Override public boolean m_7979_(double x,double y,int button,double dx,double dy){if(drag>=0&&button==0){set(drag,((x-left())/scale()-370)/320);return true;}return false;}
 @Override public boolean m_6348_(double x,double y,int button){drag=-1;return true;}
 @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){
  g.m_280509_(0,0,f_96543_,f_96544_,0xff040c24);var p=g.m_280168_();p.m_85836_();p.m_85837_(left(),top(),0);p.m_85841_((float)scale(),(float)scale(),1);var c=new MenuCanvas(g);
  c.text(tr("AUDIO / LOCAL MIXER","ÁUDIO / VOLUMES LOCAIS"),42,28,34,0xff75efff,0);
  c.text(tr("Music volume does not mute voices or effects.","O volume da música não silencia vozes ou efeitos."),44,73,15,0xffdae9ef,0);
  String[] labels={tr("Minecraft music","Música do Minecraft"),tr("Persona voices","Vozes das Personas"),tr("Combat & summoning","Combate e evocação"),tr("Doors & furniture","Portas e móveis"),tr("Interface","Interface"),tr("Awakening cinematic","Cinemática do despertar")};
  for(int i=0;i<6;i++){double y=100+i*50,v=value(i);if(i==selected)c.fill(35,y,730,41,0xff153865);c.text(labels[i],48,y+9,19,0xffeef7ff,0);c.fill(370,y+19,320,5,0xff35506b);c.fill(370,y+19,320*v,5,0xff70e8f4);c.fill(367+320*v,y+12,6,19,0xffeefaff);c.text(Math.round(v*100)+"%",706,y+9,18,0xffeef7ff,0);}
  c.text(tr("Awakening contains a mixed track of music, voices and effects.","O despertar contém uma faixa mista de música, vozes e efeitos."),44,411,14,0xffb1c9dc,0);
  c.text(failed?tr("Could not save settings.","Não foi possível salvar os ajustes."):tr("Minecraft Master and category volumes also apply.","Os volumes geral e das categorias do Minecraft também se aplicam."),44,433,14,failed?0xffff6977:0xffb1c9dc,0);
  c.text(tr("ESC Back     Arrows Adjust     Home / End 0 / 100%","ESC Voltar     Setas Ajustar     Home / End 0 / 100%"),44,466,15,0xff75efff,0);p.m_85849_();
 }
}
