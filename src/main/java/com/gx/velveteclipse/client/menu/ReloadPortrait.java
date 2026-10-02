package com.gx.velveteclipse.client.menu;

import com.gx.velveteclipse.VelvetEclipseMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Approved cuboid poses with the connected player's live skin, slim/wide arms and blue grading. */
@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ReloadPortrait {
    private record Mesh(byte[] textures,float[][] frames){}
    private static final Map<String,Mesh> CACHE=new HashMap<>();
    private static ShaderInstance shader;
    private static final BufferBuilder BUFFER=new BufferBuilder(262144);
    @SubscribeEvent public static void shaders(RegisterShadersEvent e)throws IOException{
        CACHE.clear();
        e.registerShader(new ShaderInstance(e.getResourceProvider(),new ResourceLocation("velvet_eclipse","menu_blue"),DefaultVertexFormat.f_85819_),s->shader=s);
    }
    private static Mesh mesh(String name,boolean slim){
        String key=name+(slim?"_slim":"_wide");
        if(CACHE.containsKey(key))return CACHE.get(key);
        try(var raw=ReloadPortrait.class.getResourceAsStream("/assets/velvet_eclipse/menu/"+key+".bin.gz")){
            if(raw==null)throw new IOException("Missing pose "+key);
            try(var in=new DataInputStream(new GZIPInputStream(raw))){
                if(in.readInt()!=0x56454d50||in.readInt()!=1)throw new IOException("Invalid pose header");
                int faces=in.readInt(),count=in.readInt();if(faces<1||faces>1000||count!=3)throw new IOException("Invalid pose size");
                byte[] textures=in.readNBytes(faces);if(textures.length!=faces)throw new EOFException();
                float[][] frames=new float[count][faces*28];
                for(float[] frame:frames)for(int i=0;i<frame.length;i++){frame[i]=in.readFloat();if(!Float.isFinite(frame[i]))throw new IOException("Nonfinite pose");}
                if(in.read()!=-1)throw new IOException("Trailing pose bytes");
                Mesh result=new Mesh(textures,frames);CACHE.put(key,result);return result;
            }
        }catch(IOException e){CACHE.put(key,null);VelvetEclipseMod.LOGGER.error("Cannot render menu pose "+key,e);return null;}
    }
    public static void draw(GuiGraphics g,int tab,boolean home,String playerId,double seconds,boolean motion,double opened){
        var mc=Minecraft.m_91087_();var player=mc.f_91074_;if(player==null||shader==null)return;
        ResourceLocation skin=player.m_108560_();boolean slim=player.m_108564_().equals("slim");
        try{var info=mc.m_91403_()==null?null:mc.m_91403_().m_104949_(UUID.fromString(playerId));if(info!=null){skin=info.m_105337_();slim=info.m_105336_().equals("slim");}}catch(IllegalArgumentException ignored){}
        String name=home?"main":switch(tab){case 0->"skill";case 1->"item";case 2->"equip";case 4->"status";default->"system";};
        Mesh m=mesh(name,slim);if(m==null)return;
        double phase=motion?Math.sin(seconds*.85)+1:1;int a=(int)phase,b=Math.min(2,a+1);float blend=(float)(phase-a);
        var stack=g.m_280168_();g.m_280262_();stack.m_85836_();
        try{
            stack.m_85841_(900f/1280,500f/720,1);stack.m_85837_(0,0,MenuDepth.MODEL);
            double angle,scale,centerX,centerY,opacity=1;
            if(home){stack.m_85837_(480,0,0);stack.m_85841_(-1,1,1);var entry=ReloadMenuOpening.portrait(opened,motion);angle=entry.angle();scale=740./440;centerX=entry.x();centerY=entry.y();}
            else{
                double hx,hy,ax,ay;
                if(tab==0){hx=701;hy=542;ax=223.49111015584333;ay=166.01672493254813;scale=1.42;angle=184;}
                else if(tab==1){hx=656;hy=278;ax=196.6336412117185;ay=242.13582982261292;scale=1.75;angle=34;opacity=.62;}
                else if(tab==2){hx=842;hy=570;ax=211.8240629945809;ay=167.8552019571583;scale=1.58;angle=254;}
                else {hx=1030;hy=255;ax=220;ay=220;scale=1.5;angle=tab==4?-5:185;}
                double r=Math.toRadians(angle),dx=(ax-220)*scale,dy=(ay-320)*scale;
                centerX=hx-(dx*Math.cos(r)-dy*Math.sin(r));centerY=hy-(dx*Math.sin(r)+dy*Math.cos(r));
            }
            stack.m_85837_(centerX,centerY+(motion?Math.sin(seconds*.8)*4:0),0);
            stack.m_252781_(new Quaternionf().rotationZ((float)Math.toRadians(angle+(motion?Math.sin(seconds*.43)*.7:0))));
            stack.m_85841_((float)scale,(float)(home?1076./640:scale),1);
            var matrix=stack.m_85850_().m_252922_();
            RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();RenderSystem.enableDepthTest();RenderSystem.depthMask(true);
            RenderSystem.setShader(()->shader);RenderSystem.setShaderColor(1,1,1,(float)opacity);
            // Resolve the nearest skin surface first: Item fades the whole portrait,
            // not every overlapping cube face or clothing layer independently.
            for(int pass=0;pass<(opacity<1?2:1);pass++){
            boolean depthOnly=opacity<1&&pass==0;
            RenderSystem.colorMask(!depthOnly,!depthOnly,!depthOnly,!depthOnly);
            RenderSystem.depthFunc(opacity<1&&pass==1?514:515);
            RenderSystem.depthMask(depthOnly||opacity==1);
            for(int texture=0;texture<2;texture++){
                boolean present=false;for(byte binding:m.textures)if(binding==texture){present=true;break;}if(!present)continue;
                RenderSystem.setShaderTexture(0,texture==0?skin:new ResourceLocation("velvet_eclipse","textures/entity/evoker_fx.png"));
                BUFFER.m_166779_(VertexFormat.Mode.QUADS,DefaultVertexFormat.f_85819_);
                for(int face=0;face<m.textures.length;face++)if(m.textures[face]==texture){
                    for(int vertex=0;vertex<4;vertex++){
                        int index=(face*4+vertex)*7;float[] v=new float[7];for(int n=0;n<7;n++)v[n]=m.frames[a][index+n]+(m.frames[b][index+n]-m.frames[a][index+n])*blend;
                        BUFFER.m_252986_(matrix,v[0]-220,v[1]-320,-v[2]*10).m_7421_(v[3],v[4]).m_6122_(Math.round(v[5]*127.5f),Math.round(v[6]*2040),0,255).m_5752_();
                    }
                }
                BufferUploader.m_231202_(BUFFER.m_231175_());
            }
            }
        }finally{RenderSystem.colorMask(true,true,true,true);RenderSystem.setShaderColor(1,1,1,1);RenderSystem.depthMask(true);RenderSystem.depthFunc(515);RenderSystem.defaultBlendFunc();RenderSystem.disableDepthTest();stack.m_85849_();}
    }
}
