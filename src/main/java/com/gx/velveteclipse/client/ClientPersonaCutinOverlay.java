package com.gx.velveteclipse.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class ClientPersonaCutinOverlay {
    private static final WeaknessCutinState STATE=new WeaknessCutinState();
    public static void trigger(int id){if(Minecraft.m_91087_().f_91074_!=null)STATE.trigger(id);}
    public static void tick(){STATE.tick();}
    public static void reset(){STATE.reset();}
    @SubscribeEvent public static void onGui(RenderGuiEvent.Post event){
        var mc=Minecraft.m_91087_();if(!PersonaCinematics.weakness()||!WeaknessCutinDesign.summonVisible(PersonaCinematics.skillTicks())||mc.f_91074_==null||mc.f_91073_==null)return;
        var player=mc.f_91074_;var g=event.getGuiGraphics();
        int w=com.gx.velveteclipse.util.RuntimeBridge.guiScaledWidth(),h=com.gx.velveteclipse.util.RuntimeBridge.guiScaledHeight();
        var pose=g.m_280168_();float[] previous=RenderSystem.getShaderColor().clone();
        g.m_280262_();pose.m_85836_();pose.m_252880_(0,0,700);
        try{
            RenderSystem.setShaderColor(1,1,1,1);
            WeaknessCutinDesign.paintSummon(new WeaknessCutinDesign.Canvas(){
                public void fill(int x,int y,int width,int height,int color){if(width>0&&height>0)g.m_280509_(x,y,x+width,y+height,color);}
                public void text(String s,int x,int y,int c){}public int width(String s){return mc.f_91062_.m_92895_(com.gx.velveteclipse.localization.DisplayText.text(s));}
                public void eyes(int x,int y,int width,int height,float alpha){
                    int left=Math.max(0,x),right=Math.min(w,x+width),top=Math.max(0,y),bottom=Math.min(h,y+height);
                    if(right<=left||bottom<=top)return;
                    g.m_280262_();g.m_280588_(left,top,right,bottom);
                    try{
                        RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.setShaderColor(.42f,.84f,1,alpha);
                        g.m_280411_(player.m_108560_(),x,y,width,height,8,11,8,3,64,64);
                        if(player.m_36170_(PlayerModelPart.HAT))g.m_280411_(player.m_108560_(),x,y,width,height,40,11,8,3,64,64);
                    }finally{g.m_280262_();g.m_280618_();RenderSystem.setShaderColor(1,1,1,1);}
                }
                public void fragment(double[] xy,double[] uv,float alpha){
                    g.m_280262_();var buffer=g.m_280091_().m_6299_(net.minecraft.client.renderer.RenderType.m_234338_(player.m_108560_()));
                    int layers=player.m_36170_(PlayerModelPart.HAT)?2:1;
                    for(int layer=0;layer<layers;layer++)for(int i=1;i<xy.length/2-1;i++){
                        int[] verts={0,i,i+1,i+1};
                        for(int v:verts)buffer.m_252986_(pose.m_85850_().m_252922_(),(float)xy[v*2],(float)xy[v*2+1],0)
                            .m_6122_(88,207,255,(int)(alpha*255)).m_7421_((float)(8+layer*32+uv[v*2]*8)/64,(float)(10+uv[v*2+1]*4)/64)
                            .m_86008_(655360).m_85969_(15728880).m_252939_(pose.m_85850_().m_252943_(),0,0,1).m_5752_();
                    }
                    g.m_280262_();
                }
            },w,h,PersonaCinematics.skillTicks()+event.getPartialTick());
        }finally{g.m_280262_();pose.m_85849_();RenderSystem.setShaderColor(previous[0],previous[1],previous[2],previous[3]);}
    }
}
