package com.gx.velveteclipse.client.tartarus;
import com.gx.velveteclipse.darkhour.TartarusPropEntity;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

/** Same authored geometry as the approved room; native entity materials and world lighting. */
public final class TartarusPropRenderer extends EntityRenderer<TartarusPropEntity> {
 private final String mesh;
 private final ResourceLocation[] textures=new ResourceLocation[10];
 public TartarusPropRenderer(EntityRendererProvider.Context context,String mesh){super(context);this.mesh=mesh;for(int i=0;i<10;i++)textures[i]=new ResourceLocation("velvet_eclipse","textures/block/tartarus_native_"+i+".png");}
 @Override public ResourceLocation m_5478_(TartarusPropEntity e){return textures[2];}
 @Override public boolean m_5523_(TartarusPropEntity e,net.minecraft.client.renderer.culling.Frustum frustum,double x,double y,double z){return frustum.m_113029_(e.m_20191_().m_82400_(3));}
 @Override public void m_7392_(TartarusPropEntity entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
  pose.m_85836_();pose.m_252781_(new Quaternionf().rotationY((float)Math.toRadians(-entity.m_146908_())));pose.m_85837_(0,-1,0);
  float[] data=TartarusMeshData.load(mesh);var matrix=pose.m_85850_();
  for(int i=0;i<data.length;i+=33){
   int material=Math.max(0,Math.min(9,Math.round(data[i+9])));var buffer=buffers.m_6299_(RenderType.m_110458_(textures[material]));
   float[] a=java.util.Arrays.copyOfRange(data,i,i+11),b=java.util.Arrays.copyOfRange(data,i+11,i+22),c=java.util.Arrays.copyOfRange(data,i+22,i+33);
   if(TartarusMeshData.backwards(a,b,c)){var tmp=b;b=c;c=tmp;}
   int block=Math.max(light&0xffff,material==5||material==6?15<<4:material>=7?12<<4:7<<4),lit=(light&0xffff0000)|block;
   int normal=TartarusMeshData.dominant(a),u=normal==0?2:0,v=normal==1?2:1;
   for(var p:new float[][]{a,b,c,c}){
    var n=new org.joml.Vector3f(p[3],p[4],p[5]).normalize();
    // Entity textures repeat naturally; portal sheets use one frame to avoid vertical atlas stretching.
    float uu=p[u]/8,vv=p[v]/8;if(material>=7){uu=(float)(p[u]/5.4+.5);vv=(float)((Math.floor((entity.f_19797_+partial)/2)%24+Math.max(.001,Math.min(.999,(7.5-p[v])/6.5)))/24);}
    buffer.m_252986_(matrix.m_252922_(),p[0],p[1],p[2]).m_85950_(material>=7?1:p[6],material>=7?1:p[7],material>=7?1:p[8],1).m_7421_(uu,vv).m_86008_(OverlayTexture.f_118083_).m_85969_(lit).m_252939_(matrix.m_252943_(),n.x,n.y,n.z).m_5752_();
   }
  }
  pose.m_85849_();super.m_7392_(entity,yaw,partial,pose,buffers,light);
 }
}
