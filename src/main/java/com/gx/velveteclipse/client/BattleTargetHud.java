package com.gx.velveteclipse.client;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.BattleTargetS2C;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector4f;
import java.util.*;

/** A marker anchored to the actual selected enemy, not an always-visible screen panel. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class BattleTargetHud {
    private static int entityId=-1;
    private static final EnumMap<Element,Affinity> known=new EnumMap<>(Element.class);
    private static double x,y,minX,minY,maxX,maxY;
    private static boolean visible;
    private static BattleMenuLayout.Rect marker;
    public static void reset(){entityId=-1;visible=false;marker=null;known.clear();}
    public static void accept(BattleTargetS2C p){entityId=p.entityId();known.clear();for(String text:p.known().split(",")){String[] pair=text.split(":");if(pair.length==2)try{known.put(Element.valueOf(pair[0]),Affinity.valueOf(pair[1]));}catch(IllegalArgumentException ignored){}}}
    public static String indicator(Affinity affinity){return affinity==null?"?":affinity==Affinity.WEAK?"! WEAK":"";}
    public static boolean hit(double mx,double my){return visible&&((mx>=minX&&mx<=maxX&&my>=minY&&my<=maxY)||(marker!=null&&marker.contains(mx,my)));}
    @SubscribeEvent public static void project(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;visible=false;
        var mc=Minecraft.m_91087_();if(!ClientBattleState.snapshot().active()||mc.f_91073_==null)return;
        var entity=mc.f_91073_.m_6815_(entityId);if(entity==null||!entity.m_6084_())return;
        var position=entity.m_20318_(e.getPartialTick());var camera=e.getCamera().m_90583_();
        int w=RuntimeBridge.guiScaledWidth(),h=RuntimeBridge.guiScaledHeight();
        var matrix=new org.joml.Matrix4f(e.getProjectionMatrix()).mul(e.getPoseStack().m_85850_().m_252922_());
        var clip=matrix.transform(new Vector4f((float)(position.f_82479_-camera.f_82479_),(float)(position.f_82480_+entity.m_20206_()*.45-camera.f_82480_),(float)(position.f_82481_-camera.f_82481_),1));
        if(clip.w<=0)return;x=(clip.x/clip.w*.5+.5)*w;y=(.5-clip.y/clip.w*.5)*h;
        if(x<0||x>w||y<0||y>h)return;
        double radius=Math.max(.4,entity.m_20205_()*.65);minX=maxX=x;minY=maxY=y;
        for(int i=0;i<8;i++){
            var corner=matrix.transform(new Vector4f((float)(position.f_82479_+((i&1)==0?-radius:radius)-camera.f_82479_),
                (float)(position.f_82480_+((i&2)==0?0:entity.m_20206_())-camera.f_82480_),
                (float)(position.f_82481_+((i&4)==0?-radius:radius)-camera.f_82481_),1));
            if(corner.w<=0)continue;double sx=(corner.x/corner.w*.5+.5)*w,sy=(.5-corner.y/corner.w*.5)*h;
            minX=Math.min(minX,sx);maxX=Math.max(maxX,sx);minY=Math.min(minY,sy);maxY=Math.max(maxY,sy);
        }
        minX-=12;maxX+=48;minY-=12;maxY+=18;visible=true;
    }
    public static void paint(HudDesign.Canvas c,int w,int h,double hp,double trail){
        if(!visible||ClientBattleState.snapshot().enemyHp()<=0){marker=null;return;}
        boolean target=ClientBattleUiState.page()==ClientBattleUiState.Page.TARGET||PersonaCinematics.meleeActive()||PersonaCinematics.skillSequenceActive()&&PersonaCinematics.skillTicks()>=68;
        if(!target&&!hit(RuntimeBridge.guiMouseX(),RuntimeBridge.guiMouseY())){marker=null;return;}
        if(ClientBattleUiState.selector()){marker=null;return;}
        double unit=Math.max(.65,Math.min(w/640.0,h/360.0));int px=(int)Math.max(22*unit,Math.min(w-122*unit,x)),py=(int)Math.max(34*unit,Math.min(h-39*unit,y));
        marker=new BattleMenuLayout.Rect(px-(int)(26*unit),py-(int)(40*unit),(int)(127*unit),(int)(70*unit),"",0);
        paintMarker(c,px,py,unit,hp,trail,ClientBattleUiState.selectedElement(),known.get(ClientBattleUiState.selectedElement()));
    }
    public static void paintMarker(HudDesign.Canvas c,int x,int y,double u,double hp,double trail,Element element,Affinity affinity){
        double r=22*u;BattleGraphics.poly(c,new double[]{x,y-r-3,x+r+4,y,x,y+r+3,x-r-4,y},0xFFFF2655);
        BattleGraphics.poly(c,new double[]{x,y-r,x+r,y,x,y+r,x-r,y},0xFF010610);
        BattleGraphics.poly(c,new double[]{x,y-r+5,x+r-5,y,x,y+r-5,x-r+5,y},0xFF574016);
        BattleGraphics.icon(c,element,x,y,14*u);
        int bx=x+(int)(6*u),by=y+(int)(9*u),bw=(int)(81*u),bh=Math.max(4,(int)(8*u));
        BattleGraphics.poly(c,new double[]{bx-3,by-3,bx+bw+7,by-3,bx+bw+3,by+bh+3,bx-5,by+bh+3},0xFFFF2655);
        c.fill(bx-2,by-2,bw+5,bh+4,0xFF000510);c.fill(bx,by,bw,bh,0xFF173348);
        c.fill(bx,by,(int)(bw*Math.max(0,Math.min(1,trail))),bh,0xFFFFFFFF);c.fill(bx,by,(int)(bw*Math.max(0,Math.min(1,hp))),bh,0xFF4FF5ED);
        String label=indicator(affinity);if(!label.isEmpty()){
            int tx=x+(int)(9*u),ty=y-(int)(34*u);float scale=(float)(label.equals("?")?3.3*u:2.1*u);
            for(int dx=-2;dx<=2;dx+=2)for(int dy=-2;dy<=2;dy+=2)c.angled(label,tx+dx,ty+dy,scale,0xFF00040B,-12);
            c.angled(label,tx,ty,scale,0xFFFFFFFF,-12);
        }
    }
}
