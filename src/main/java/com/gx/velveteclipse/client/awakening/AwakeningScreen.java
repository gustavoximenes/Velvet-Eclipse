package com.gx.velveteclipse.client.awakening;

import com.gx.velveteclipse.awakening.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.VelvetEclipseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;

/** Exclusive per-player presentation. ESC cancels; the server alone awards the contract. */
public final class AwakeningScreen extends Screen {
    private final String token;
    private AwakeningScene scene;
    private final PersonaModelPreview previews=new PersonaModelPreview();
    private double epoch=now(),base,menuSince;
    private String chosen="orpheus";
    private boolean menu,waiting,closed,finishSent;
    private int index,ticks;
    private SoundInstance sound;
    private static final int[] COLORS={0xFF32D8FF,0xFFFFDE30,0xFFFF2D46};
    private static final String[] NAMES={"ORPHEUS","IZANAGI","ARSÈNE"};
    private static final String[] TAGS={"FOGO / CURA","ELETRICIDADE / CORTE","MALDIÇÃO / FÍSICO"};
    private static final String[] WORDS={"Dê voz à sua determinação.","Encare o que está diante de você.","Recuse o destino que impuseram a você."};
    public AwakeningScreen(String token)throws java.io.IOException{super(Component.m_237113_("Despertar"));this.token=token;scene=new AwakeningScene("intro");}
    static double now(){return System.nanoTime()*1e-9;}
    public static boolean active(){return Minecraft.m_91087_().f_91080_ instanceof AwakeningScreen;}
    public static void accept(AwakeningNetwork.State state){
        var mc=Minecraft.m_91087_();
        try{
            if(state.phase().equals("start")){
                if(mc.f_91074_==null)return;
                if(mc.f_91080_ instanceof AwakeningScreen old&&old.token.equals(state.token()))return;
                var screen=new AwakeningScreen(state.token());mc.m_91152_(screen);mc.m_91106_().m_120405_();screen.epoch=now();screen.audio("intro",false);return;
            }
            if(!(mc.f_91080_ instanceof AwakeningScreen s)||!s.token.equals(state.token()))return;
            if(state.phase().equals("continue")&&AwakeningTimeline.STARTERS.contains(state.persona())){
                if(s.base==23)return;
                s.chosen=state.persona();s.index=AwakeningTimeline.STARTERS.indexOf(s.chosen);s.scene=new AwakeningScene(s.chosen);s.menu=false;s.waiting=false;s.base=23;s.epoch=now();s.audio(s.chosen+"_continuation",false);
            }else if(state.phase().equals("cancel")||state.phase().equals("complete")){s.closed=true;mc.m_91152_(null);}
        }catch(Exception|LinkageError e){
            VelvetEclipseMod.LOGGER.error("Awakening presentation failed",e);AwakeningNetwork.request(state.token(),"cancel","");
            if(mc.f_91080_ instanceof AwakeningScreen s){s.closed=true;mc.m_91152_(null);}
            if(mc.f_91074_!=null)mc.f_91074_.m_5661_(Component.m_237113_("Não foi possível carregar o despertar. Seus dados foram preservados; tente novamente com a Evoker."),false);
        }
    }
    private double time(){return menu?16:base+now()-epoch;}
    @Override public boolean m_7043_(){return false;}
    @Override public void m_86600_(){
        if(f_96541_.f_91074_==null||!f_96541_.f_91074_.m_6084_()){m_7379_();return;}
        if(++ticks%20==0)AwakeningNetwork.request(token,"alive","");
        if(!menu&&base==0&&time()>=16){menu=true;menuSince=now();audio(chosen+"_choice",true);}
        if(base==23&&time()>=AwakeningTimeline.duration(chosen)&&(ticks%20==0||!finishSent)){finishSent=true;AwakeningNetwork.request(token,"finish",chosen);}
    }
    @Override public void m_7379_(){if(!closed)AwakeningNetwork.request(token,"cancel","");closed=true;f_96541_.m_91152_(null);}
    @Override public void m_7861_(){stopAudio();if(!closed)AwakeningNetwork.request(token,"cancel","");scene=null;}
    private void choose(){if(!menu||waiting||now()-menuSince<.35)return;waiting=true;AwakeningNetwork.request(token,"choose",chosen);}
    private void select(int n){if(waiting)return;index=Math.floorMod(n,3);chosen=AwakeningTimeline.STARTERS.get(index);audio(chosen+"_choice",true);}
    @Override public boolean m_7933_(int key,int scan,int mods){
        if(key==256){m_7379_();return true;}
        if(menu){if(key==65||key==263)select(index-1);if(key==68||key==262)select(index+1);if(key==257||key==335)choose();}
        return true;
    }
    @Override public boolean m_6375_(double mx,double my,int button){
        if(!menu||button!=0)return true;double scale=scale(),x=(mx-left())/scale,y=(my-top())/scale;
        if(y>=145&&y<=458){for(int i=0;i<3;i++)if(x>=34+i*304&&x<326+i*304)select(i);}
        if(x>=684&&x<=930&&y>=458&&y<=506)choose();return true;
    }
    private double scale(){return Math.min(f_96543_/960.,f_96544_/540.);}
    private double left(){return (f_96543_-960*scale())/2;}
    private double top(){return (f_96544_-540*scale())/2;}
    private void audio(String name,boolean loop){
        stopAudio();var manager=Minecraft.m_91087_().m_91106_();
        // One mastered stream: original score + isolated voices. No added gun/impact sounds.
        manager.m_120386_(null,SoundSource.MUSIC);
        sound=new SimpleSoundInstance(new ResourceLocation("velvet_eclipse","awakening."+name),SoundSource.MUSIC,1,1,RandomSource.m_216327_(),loop,0,SoundInstance.Attenuation.NONE,0,0,0,true);
        manager.m_120367_(sound);
    }
    private void stopAudio(){if(sound!=null){Minecraft.m_91087_().m_91106_().m_120399_(sound);sound=null;}}
    @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){
        try{
            g.m_280509_(0,0,f_96543_,f_96544_,menu?new int[]{0xFF030F39,0xFF1C1807,0xFF0E030D}[index]:0xFF02070F);
            // Cover the viewport; interface cards keep a safe centered layout.
            if(!menu){var bg=g.m_280168_();bg.m_85836_();bg.m_85841_(f_96543_/960f,f_96544_/540f,1);AwakeningEffects.sky(new MenuCanvas(g),time());g.m_280262_();bg.m_85849_();}
            var p=g.m_280168_();p.m_85836_();p.m_85837_(left(),top(),0);p.m_85841_((float)scale(),(float)scale(),1);
            var canvas=new MenuCanvas(g);double real=time();
            if(menu){selection(g,canvas);p.m_85849_();return;}
            g.m_280262_();p.m_85849_();
            scene.draw(g,f_96543_,f_96544_,real,chosen);
            p.m_85836_();p.m_85837_(left(),top(),0);p.m_85841_((float)scale(),(float)scale(),1);
            double t=AwakeningTimeline.visualTime(real,chosen);AwakeningEffects.paint(canvas,scene,real,t,chosen,COLORS[index]);

            if(t<2.4){canvas.text("DARK HOUR",42,331,64,0xFFA7E071,0);canvas.text("QUANDO O MUNDO PARA, ALGO EM VOCÊ DESPERTA.",44,401,17,0xFFCFECDD,0);}
            if(t>8.8&&t<10.6)caption(canvas,"Eu não vou recuar.");
            if(t>23.35&&t<24.7)caption(canvas,"Persona!");
            if(t>=30&&t<32.2){canvas.poly(new double[]{29,384,431,365,410,458,29,466},0xEF040D21);canvas.text(NAMES[index],46,381,53,COLORS[index],0);canvas.text("PACTO / "+new String[]{"A VOZ QUE DESPERTA","A FORÇA DA VERDADE","A CHAMA DA REBELDIA"}[index],48,436,13,0xFFE8F2F9,0);}
            if(t>33.35&&t<35.2){canvas.poly(new double[]{40,54,262,44,245,97,33,108},COLORS[index]);canvas.text(new String[]{"AGI","ZIO","EIGA"}[index],65,54,30,0xFF060B19,0);}
            if(t>39){canvas.poly(new double[]{520,368,960,346,960,485,507,485},0xEE040D21);canvas.text("EU SOU VOCÊ.",548,375,36,COLORS[index],0);canvas.text(NAMES[index]+" • CONTRATO FIRMADO",549,418,18,0xFFECF6F9,0);canvas.text("Seu primeiro vínculo foi despertado.",550,454,16,0xFFB9D2E7,0);}
            g.m_280262_();p.m_85849_();
            // Fade the real viewport, including 4:3 and ultrawide edges.
            if(t<.7)g.m_280509_(0,0,f_96543_,f_96544_,((int)(255*(1-AwakeningScene.smooth(t/.7)))<<24)|0x02070F);
            if(t>41.4)g.m_280509_(0,0,f_96543_,f_96544_,((int)(255*AwakeningScene.smooth((t-41.4)/.6))<<24)|0x02070F);

        }catch(RuntimeException|LinkageError e){VelvetEclipseMod.LOGGER.error("Awakening renderer failed",e);m_7379_();}
    }
    private void caption(MenuCanvas c,String text){double w=c.width(text,24),x=(960-w)/2;c.fill(x-18,437,w+36,63,0xFF040D1F);c.fill(x-18,437,w+36,2,COLORS[index]);c.text(f_96541_.f_91074_.m_36316_().getName(),x,444,11,COLORS[index],0);c.text(text,x,463,24,0xFFEFF9FB,0);}
    private void selection(GuiGraphics g,MenuCanvas c){
        int color=COLORS[index],dark=new int[]{0xFF030F39,0xFF1C1807,0xFF0E030D}[index];double time=now();c.fill(0,0,960,540,dark);
        for(int i=0;i<8;i++){double x=(time*24+i*167)%1350-300;c.poly(new double[]{x,0,x+110,0,x-100,540,x-210,540},0x26000000|(color&0xFFFFFF));}
        c.text("QUEM RESPONDE AO SEU CHAMADO?",36,39,34,0xFFF1F8F9,0);c.text("ESCOLHA SUA PRIMEIRA PERSONA",39,88,16,color,0);previews.advance(time,true);
        for(int i=0;i<3;i++){
            int x=34+i*304,y=i==index?152:162,col=COLORS[i];c.poly(new double[]{x+12,y,x+291,y,x+276,y+294,x,y+294},i==index?col:0xFF324254);c.poly(new double[]{x+14,y+3,x+288,y+3,x+273,y+291,x+3,y+291},0xFF040D21);g.m_280262_();
            g.m_280588_((int)(left()+(x+3)*scale()),(int)(top()+y*scale()),(int)(left()+(x+288)*scale()),(int)(top()+(y+294)*scale()));
            try{previews.drawCard(g,AwakeningTimeline.STARTERS.get(i),x+18,y+5,256,225);}finally{g.m_280618_();}
            var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(0,0,MenuDepth.FOREGROUND);
            c.poly(new double[]{x,y+233,x+291,y+218,x+283,y+294,x,y+294},i==index?col:0xFF162236);c.text(NAMES[i],x+19,y+240,32,i==index?0xFF050B16:0xFFDBE8F1,0);c.text(TAGS[i],x+20,y+275,11,i==index?0xFF0D1824:col,0);g.m_280262_();pose.m_85849_();
        }
        c.text(WORDS[index],38,473,20,0xFFEBF1F5,0);c.poly(new double[]{691,462,925,452,914,500,684,505},color);c.text(waiting?"SELANDO...":"CONFIRMAR",715,468,20,0xFF070D17,0);c.text("A / D ESCOLHER     ENTER CONFIRMAR     ESC CANCELAR",39,510,12,color,0);g.m_280262_();
    }
    @Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
    public static final class AudioFilter {
        @SubscribeEvent public static void play(PlaySoundEvent e){if(active()&&e.getSound()!=null&&!e.getSound().m_7904_().m_135815_().startsWith("awakening."))e.setSound(null);}
    }
}
