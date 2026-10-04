package com.gx.velveteclipse.client.tartarus;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.model.BakedQuad;
import com.gx.velveteclipse.client.presentation.CutawayVolume;

/** Subdivide only occluded surfaces so a large wall quad does not erase the whole room. */
public final class CutawayQuads {
 public static List<BakedQuad> filter(List<BakedQuad> source,BlockPos origin,List<CutawayVolume> volumes){
  if(origin==null||volumes.isEmpty())return source;var result=new ArrayList<BakedQuad>(source.size());
  for(var quad:source){var data=quad.m_111303_();int stride=data.length/4;var vertices=new int[4][];for(int i=0;i<4;i++)vertices[i]=Arrays.copyOfRange(data,i*stride,(i+1)*stride);
   if(!intersects(vertices,origin,volumes)){result.add(quad);continue;}
   split(quad,vertices[0],vertices[1],vertices[2],origin,volumes,result,0);split(quad,vertices[0],vertices[2],vertices[3],origin,volumes,result,0);
  }
  return List.copyOf(result);
 }
 private static float p(int[] v,int axis){return Float.intBitsToFloat(v[axis]);}
 private static boolean intersects(int[][] vertices,BlockPos origin,List<CutawayVolume> volumes){
  double[] lo={Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE},hi={-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE};
  int[] at={origin.m_123341_(),origin.m_123342_(),origin.m_123343_()};
  for(var v:vertices)for(int i=0;i<3;i++){lo[i]=Math.min(lo[i],p(v,i)+at[i]);hi[i]=Math.max(hi[i],p(v,i)+at[i]);}
  for(var volume:volumes)if(volume.intersects(lo[0],lo[1],lo[2],hi[0],hi[1],hi[2]))return true;return false;
 }
 private static void split(BakedQuad template,int[] a,int[] b,int[] c,BlockPos at,List<CutawayVolume> volumes,List<BakedQuad> out,int depth){
  if(!intersects(new int[][]{a,b,c},at,volumes)){int[] data=new int[a.length*4];System.arraycopy(a,0,data,0,a.length);System.arraycopy(b,0,data,a.length,a.length);System.arraycopy(c,0,data,a.length*2,a.length);System.arraycopy(c,0,data,a.length*3,a.length);out.add(new BakedQuad(data,template.m_111305_(),template.m_111306_(),template.m_173410_(),template.m_111307_(),template.hasAmbientOcclusion()));return;}
  double size=0;for(int i=0;i<3;i++)size=Math.max(size,Math.max(Math.max(p(a,i),p(b,i)),p(c,i))-Math.min(Math.min(p(a,i),p(b,i)),p(c,i)));
  if(size<=.65||depth>=6)return;
  var ab=mid(a,b);var bc=mid(b,c);var ca=mid(c,a);split(template,a,ab,ca,at,volumes,out,depth+1);split(template,ab,b,bc,at,volumes,out,depth+1);split(template,ca,bc,c,at,volumes,out,depth+1);split(template,ab,bc,ca,at,volumes,out,depth+1);
 }
 private static int[] mid(int[] a,int[] b){var result=a.clone();for(int i:new int[]{0,1,2,4,5})result[i]=Float.floatToRawIntBits((p(a,i)+p(b,i))*.5f);for(int shift=0;shift<32;shift+=8){int value=(((a[3]>>>shift)&255)+((b[3]>>>shift)&255))/2;result[3]=(result[3]&~(255<<shift))|(value<<shift);}return result;}
 private CutawayQuads(){}
}
