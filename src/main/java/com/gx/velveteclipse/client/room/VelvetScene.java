package com.gx.velveteclipse.client.room;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Replays the approved skeletal poses in 3D, binding the live player's skin at draw time. */
public final class VelvetScene {
    record Mesh(String texture,int variant,float[] vertices,int skinSlot){}
    private final String name;
    final List<Mesh> meshes=new ArrayList<>();
    final float[][] frames;
    final float start,fps;
    private final float[] sample;
    private final BufferBuilder buffer=new BufferBuilder(262144);
    public VelvetScene(String name)throws IOException{
        this.name=name;
        try(var raw=VelvetScene.class.getResourceAsStream("/assets/velvet_eclipse/room/"+name+".bin.gz")){
            if(raw==null)throw new IOException("Missing Velvet Room scene: "+name);
            try(var in=new DataInputStream(new BufferedInputStream(new GZIPInputStream(raw)))){
                if(in.readInt()!=0x56454157||in.readInt()!=1)throw new IOException("Invalid Velvet Room asset");
                int count=bounded(in.readInt(),1,1000);
                for(int i=0;i<count;i++){
                    String texture=new String(in.readNBytes(bounded(in.readInt(),1,64)),java.nio.charset.StandardCharsets.UTF_8);
                    int variant=in.readInt(),vertices=bounded(in.readInt(),4,100_000);
                    if(vertices%4!=0)throw new IOException("Incomplete cuboid face");
                    float[] v=read(in,vertices*5);meshes.add(new Mesh(texture,variant,v,in.readInt()));
                }
                start=in.readFloat();fps=in.readFloat();frames=new float[bounded(in.readInt(),1,2000)][];
                for(int i=0;i<frames.length;i++)frames[i]=read(in,14+count*16);
                sample=new float[frames[0].length];
                if(in.read()!=-1)throw new IOException("Trailing scene data");
            }
        }
    }
    private static int bounded(int n,int min,int max)throws IOException{if(n<min||n>max)throw new IOException("Invalid scene size");return n;}
    private static float[] read(DataInputStream in,int n)throws IOException{var values=new float[n];for(int i=0;i<n;i++){values[i]=in.readFloat();if(!Float.isFinite(values[i]))throw new IOException("Invalid pose");}return values;}
    public float[] sample(double time){
        double index=Math.max(0,Math.min(frames.length-1,(time-start)*fps));int a=(int)index,b=Math.min(frames.length-1,a+1);float u=(float)(index-a);
        for(int i=0;i<sample.length;i++)sample[i]=frames[a][i]+(frames[b][i]-frames[a][i])*u;
        // Cuts stay cuts. Never sweep the camera between shots.
        double jump=0;for(int i=0;i<3;i++)jump+=Math.pow(frames[a][i]-frames[b][i],2);
        if(jump>.3)System.arraycopy(frames[a],0,sample,0,14);
        for(int i=14;i<sample.length;i+=16)if(frames[a][i+15]!=frames[b][i+15])System.arraycopy(frames[a],i,sample,i,16);
        return sample;
    }
    public static VelvetScene load(String name){try{return new VelvetScene(name);}catch(IOException e){com.mojang.logging.LogUtils.getLogger().error("Cannot load Velvet Room scene {}",name,e);return null;}}
    public void draw(GuiGraphics g,int width,int height,double time,String persona,int cards){
        draw(g,width,height,time,cards,List.of());
    }
    public void draw(GuiGraphics g,int width,int height,double time,List<String> sources){
        draw(g,width,height,time,sources.size(),sources);
    }
    private void draw(GuiGraphics g,int width,int height,double time,int cards,List<String> sources){
        var mc=Minecraft.m_91087_();if(mc.f_91074_==null)return;
        float[] frame=sample(time);double visual=time;
        var eye=new Vector3f(frame[0],frame[1],frame[2]);var front=new Vector3f(frame[9],frame[10],frame[11]);var up=new Vector3f(frame[6],frame[7],frame[8]);
        float aspect=width/(float)height,tan=(float)(270/frame[12]*Math.max(1,(16./9)/aspect));
        var projection=new Matrix4f().translation(name.equals("portrait")?.49f*Math.min(width,height*1.8f)/width:0,0,0).perspective(2*(float)Math.atan(tan),aspect,.08f,100f);
        projection.rotateZ(-frame[13]).lookAt(eye,new Vector3f(eye).add(front),up);
        g.m_280262_();var old=new Matrix4f(RenderSystem.getProjectionMatrix());var mv=RenderSystem.getModelViewStack();mv.m_85836_();
        try{
            mv.m_85850_().m_252922_().identity();RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection,VertexSorting.f_276450_);
            RenderSystem.clear(256,false);RenderSystem.enableDepthTest();RenderSystem.depthMask(true);RenderSystem.enableBlend();RenderSystem.defaultBlendFunc();RenderSystem.disableCull();RenderSystem.setShaderColor(1,1,1,1);

            int variant="slim".equals(mc.f_91074_.m_108564_())?2:1;
            // Opaque bodies first, materialization last; textures are batched per actor.
            var bindings=new ArrayList<String>();var textures=new LinkedHashSet<String>();
            for(var m:meshes){int index=m.skinSlot-100;String texture=m.texture;
                if(texture.equals("velvet_fusion_front"))texture=index>=0&&index<sources.size()?FusionCardTextures.front(sources.get(index)):"velvet_fusion_card";
                bindings.add(texture);textures.add(texture);
            }
            for(String texture:textures){
                if(texture.equals("velvet_room_glow"))RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE);else RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::m_172820_);
                RenderSystem.setShaderTexture(0,texture.equals("player")?mc.f_91074_.m_108560_():new ResourceLocation("velvet_eclipse","textures/entity/"+texture+".png"));
                buffer.m_166779_(VertexFormat.Mode.QUADS,DefaultVertexFormat.f_85819_);
                for(int j=0;j<meshes.size();j++){
                    var mesh=meshes.get(j);int offset=14+j*16;
                    if(mesh.skinSlot>=100&&mesh.skinSlot-100>=cards)continue;
                    if(!texture.equals(bindings.get(j))||mesh.variant!=0&&mesh.variant!=variant||frame[offset+15]<.5||!skinVisible(mesh.skinSlot))continue;
                    float opacity=frame[offset+15];
                    vertices(buffer,mesh.vertices,frame,offset,opacity,visual,mesh.skinSlot>=100||texture.equals("velvet_room_palette")||texture.equals("velvet_room_glow"));
                }
                BufferUploader.m_231202_(buffer.m_231175_());
            }
        }finally{
            RenderSystem.setProjectionMatrix(old,VertexSorting.f_276633_);mv.m_85849_();RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderColor(1,1,1,1);RenderSystem.depthMask(true);RenderSystem.clear(256,false);RenderSystem.disableDepthTest();RenderSystem.disableCull();RenderSystem.defaultBlendFunc();
        }
    }
    private static boolean skinVisible(int slot){
        var p=Minecraft.m_91087_().f_91074_;
        PlayerModelPart part=switch(slot){case 1->PlayerModelPart.RIGHT_PANTS_LEG;case 3->PlayerModelPart.LEFT_PANTS_LEG;case 5->PlayerModelPart.JACKET;case 7->PlayerModelPart.HAT;case 9->PlayerModelPart.LEFT_SLEEVE;case 11,13->PlayerModelPart.RIGHT_SLEEVE;default->null;};
        return part==null||p.m_36170_(part);
    }
    private static void vertices(BufferBuilder b,float[] v,float[] m,int o,float alpha,double time,boolean hero){
        float[] q=new float[12];
        for(int i=0;i<v.length;i+=20){
            for(int n=0;n<4;n++){int k=i+n*5;float x=v[k],y=v[k+1],z=v[k+2];for(int axis=0;axis<3;axis++)q[n*3+axis]=m[o+axis*4]*x+m[o+axis*4+1]*y+m[o+axis*4+2]*z+m[o+axis*4+3];}
            var normal=new Vector3f(q[3]-q[0],q[4]-q[1],q[5]-q[2]).cross(q[6]-q[0],q[7]-q[1],q[8]-q[2]);
            float shade=hero||normal.lengthSquared()<1e-10?1:.60f+.40f*Math.max(0,normal.normalize().dot(-.36f,.72f,-.60f));
            float reveal=alpha;
            for(int n=0;n<4;n++){int k=i+n*5;b.m_5483_(q[n*3],q[n*3+1],q[n*3+2]).m_7421_(v[k+3],v[k+4]).m_85950_(shade,shade,shade,reveal).m_5752_();}
        }
    }
    public double[] project(double x,double y,double z){var f=sample;double dx=x-f[0],dy=y-f[1],dz=z-f[2],depth=dx*f[9]+dy*f[10]+dz*f[11];double px=(dx*f[3]+dy*f[4]+dz*f[5])*f[12]/Math.max(.08,depth),py=-(dx*f[6]+dy*f[7]+dz*f[8])*f[12]/Math.max(.08,depth),c=Math.cos(f[13]),s=Math.sin(f[13]);return new double[]{480+c*px-s*py,270+s*px+c*py};}
    static double smooth(double t){t=Math.max(0,Math.min(1,t));return t*t*(3-2*t);}
}
