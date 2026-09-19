package com.gx.velveteclipse.client.darkhour;
import com.gx.velveteclipse.darkhour.*;
import com.gx.velveteclipse.network.DarkHourS2C;
import com.gx.velveteclipse.client.HudDesign;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import java.util.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse",value=Dist.CLIENT)
public final class DarkHourClient {
 private static boolean previous;private static int greenTicks,lastView=-1;private static Object previousLevel;
 private static DarkHourS2C tower;private static final List<BlockPos> patches=new ArrayList<>();private static int patchX=Integer.MIN_VALUE,patchZ;
 public static void accept(DarkHourS2C p){tower=p;}
 public static boolean active(){return DarkHourService.active(Minecraft.m_91087_().f_91073_);}
 public static boolean clock(){var l=Minecraft.m_91087_().f_91073_;return l!=null&&l.m_46472_().equals(net.minecraft.world.level.Level.f_46428_)&&(DarkHourCycle.warning(l.m_46468_())||greenTicks>0);}
 public static void paint(HudDesign.Canvas c,int w,int h){var l=Minecraft.m_91087_().f_91073_;if(l!=null)DarkHourClockDesign.paint(c,w,h,active()?1:DarkHourCycle.approach(l.m_46468_()),active(),greenTicks);}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.m_91087_();var l=mc.f_91073_;
  if(previousLevel!=l){previousLevel=l;previous=false;greenTicks=0;lastView=-1;tower=null;patches.clear();patchX=Integer.MIN_VALUE;}
  if(l!=null&&mc.f_91074_!=null){int view=Math.max(2,Math.min(64,mc.f_91066_.m_193772_()));if(lastView<0||l.m_46467_()%40==0){com.gx.velveteclipse.network.ModNetwork.CHANNEL.sendToServer(new com.gx.velveteclipse.network.DarkHourViewC2S(view));lastView=view;}}
  boolean now=active();if(now&&!previous)greenTicks=70;else if(greenTicks>0)greenTicks--;if(!now)greenTicks=0;previous=now;
  if(!now||mc.f_91074_==null){patches.clear();patchX=Integer.MIN_VALUE;return;}
  int x=(int)Math.floor(mc.f_91074_.m_20185_()/8),z=(int)Math.floor(mc.f_91074_.m_20189_()/8);
  if(x==patchX&&z==patchZ)return;patchX=x;patchZ=z;patches.clear();
  for(int gx=x-5;gx<=x+5;gx++)for(int gz=z-5;gz<=z+5;gz++){
   long hash=gx*341873128712L^gz*132897987541L;if(Math.floorMod(hash,4)!=0)continue;
   int px=gx*8+2+(int)Math.floorMod(hash,4),pz=gz*8+2;var at=new BlockPos(px,64,pz);if(!l.m_46801_(at))continue;
   for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
    if(dx*dx+dz*dz>1&&Math.floorMod(hash+dx+dz,3)!=0)continue;
    int y=l.m_6924_(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,px+dx,pz+dz);var surface=new BlockPos(px+dx,y-1,pz+dz);
    var block=l.m_8055_(surface);if(!block.m_60795_()&&block.m_60819_().m_76178_()&&!DarkHourRegistry.temporary(block.m_60734_())&&l.m_8055_(surface.m_7494_()).m_60795_())patches.add(surface);
   }
  }
 }
 @SubscribeEvent public static void hide(RenderLivingEvent.Pre<?,?> e){if(active()&&DarkHourService.eligible(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void fog(ViewportEvent.ComputeFogColor e){if(active()){e.setRed(.026f);e.setGreen(.13f);e.setBlue(.071f);}}
 @SubscribeEvent public static void render(RenderLevelStageEvent e){
  if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES||!active())return;var mc=Minecraft.m_91087_();var camera=e.getCamera().m_90583_();var pose=e.getPoseStack();
  var buffers=mc.m_91269_().m_110104_();var type=DarkHourRenderType.SOLID;var out=buffers.m_6299_(type);
  pose.m_85836_();try{
   pose.m_85837_(-camera.f_82479_,-camera.f_82480_,-camera.f_82481_);var matrix=pose.m_85850_().m_252922_();
   for(var entity:mc.f_91073_.m_104735_())if(DarkHourService.eligible(entity)&&entity.m_20275_(camera.f_82479_,camera.f_82480_,camera.f_82481_)<96*96){
    pose.m_85836_();pose.m_85837_(entity.m_20185_(),entity.m_20186_(),entity.m_20189_());pose.m_252781_(new org.joml.Quaternionf().rotationY((float)Math.toRadians(-entity.m_146908_())));coffin(out,pose.m_85850_().m_252922_());pose.m_85849_();
   }
   buffers.m_109912_(type);type=DarkHourRenderType.DECAL;out=buffers.m_6299_(type);
   // Individual irregular decals follow the terrain; no global floor tint or world edits.
   for(var at:patches){float x=at.m_123341_()+.5f,y=at.m_123342_()+1.008f,z=at.m_123343_()+.5f;disc(out,matrix,x,y,z,.48f,0xBA710B21);disc(out,matrix,x+.28f,y+.001f,z-.2f,.29f,0xD3910F27);}
   if(tower!=null&&tower.active()&&tower.revealed()<tower.height()){
    float y=tower.y()+tower.revealed();for(int k=0;k<3;k++)ring(out,matrix,tower.x()+.5f,y-k*1.2f,tower.z()+.5f,28+k*.7f,.22f,0x804DF8AA);
   }
   buffers.m_109912_(type);
  }finally{pose.m_85849_();}
 }
 private static void coffin(VertexConsumer out,Matrix4f m){
  float[] x={-.23f,-.39f,-.32f,.32f,.39f,.23f},y={0,1.35f,1.95f,1.95f,1.35f,0};
  for(int i=0;i<6;i++){int j=(i+1)%6;quad(out,m,x[i],y[i],-.22f,x[j],y[j],-.22f,x[j],y[j],.22f,x[i],y[i],.22f,0xFF161C1B);}
  for(float z:new float[]{-.225f,.225f}){
   for(int i=0;i<6;i++){int j=(i+1)%6;quad(out,m,0,1,z,x[i],y[i],z,x[j],y[j],z,0,1,z,0xFF342221);}
   float front=z*1.01f;rect(out,m,-.035f,.32f,.035f,1.68f,front,0xFFB19951);rect(out,m,-.23f,1.23f,.23f,1.3f,front,0xFFB19951);
   rect(out,m,-.19f,.18f,.19f,.21f,front,0xFF806C39);rect(out,m,-.25f,1.77f,.25f,1.8f,front,0xFF806C39);
  }
 }
 private static void rect(VertexConsumer o,Matrix4f m,float x,float y,float u,float v,float z,int c){quad(o,m,x,y,z,u,y,z,u,v,z,x,v,z,c);}
 static void vertex(VertexConsumer o,Matrix4f m,float x,float y,float z,int c){o.m_252986_(m,x,y,z).m_6122_((c>>16)&255,(c>>8)&255,c&255,c>>>24).m_5752_();}
 static void quad(VertexConsumer o,Matrix4f m,float a,float b,float c,float d,float e,float f,float g,float h,float i,float j,float k,float l,int color){vertex(o,m,a,b,c,color);vertex(o,m,d,e,f,color);vertex(o,m,g,h,i,color);vertex(o,m,j,k,l,color);}
 private static void disc(VertexConsumer o,Matrix4f m,float x,float y,float z,float r,int c){for(int i=0;i<16;i++){double a=i*Math.PI/8,b=(i+1)*Math.PI/8;float radius=r*(.82f+(i%3)*.12f);quad(o,m,x,y,z,x+(float)Math.cos(a)*radius,y,z+(float)Math.sin(a)*radius,x+(float)Math.cos(b)*radius,y,z+(float)Math.sin(b)*radius,x,y,z,c);}}
 private static void ring(VertexConsumer o,Matrix4f m,float x,float y,float z,float r,float width,int c){for(int i=0;i<64;i++){double a=i*Math.PI/32,b=(i+1)*Math.PI/32;quad(o,m,x+(float)Math.cos(a)*r,y,z+(float)Math.sin(a)*r,x+(float)Math.cos(b)*r,y,z+(float)Math.sin(b)*r,x+(float)Math.cos(b)*(r-width),y,z+(float)Math.sin(b)*(r-width),x+(float)Math.cos(a)*(r-width),y,z+(float)Math.sin(a)*(r-width),c);}}
}
