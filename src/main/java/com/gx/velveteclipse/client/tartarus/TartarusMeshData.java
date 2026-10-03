package com.gx.velveteclipse.client.tartarus;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

/** Authored triangles retained from the approved geometry, independent of the GPU. */
public final class TartarusMeshData {
 private static final Map<String,float[]> CACHE=new LinkedHashMap<>(64,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<String,float[]> e){return size()>64;}};
 public static synchronized float[] load(String name){return CACHE.computeIfAbsent(name,k->{
  if(!k.matches("[a-z0-9_-]{1,80}"))throw new IllegalArgumentException("Invalid mesh id");
  try(var raw=TartarusMeshData.class.getResourceAsStream("/assets/velvet_eclipse/tartarus/"+k+".mesh.gz")){
   if(raw==null)throw new IllegalStateException("Missing Tartarus mesh "+k);
   try(var in=new GZIPInputStream(raw)){byte[] bytes=in.readAllBytes();var b=ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);if(b.getInt()!=0x56454d31)throw new IllegalStateException("Invalid mesh header");int n=b.getInt();if(n<0||n>1000000||bytes.length!=8+n*44)throw new IllegalStateException("Invalid mesh length");float[] vertices=new float[n*11];b.asFloatBuffer().get(vertices);return vertices;}
  }catch(IOException e){throw new IllegalStateException("Cannot load mesh "+k,e);}
 });}
 public static List<float[]> clip(List<float[]> polygon,int axis,float plane,boolean greater){
  if(polygon.isEmpty())return polygon;var out=new ArrayList<float[]>();
  for(int i=0;i<polygon.size();i++){
   float[] a=polygon.get(i),b=polygon.get((i+1)%polygon.size());boolean ia=greater?a[axis]>=plane:a[axis]<=plane,ib=greater?b[axis]>=plane:b[axis]<=plane;
   if(ia)out.add(a);if(ia!=ib){float t=(plane-a[axis])/(b[axis]-a[axis]);float[] v=new float[a.length];for(int j=0;j<a.length;j++)v[j]=a[j]+(b[j]-a[j])*t;out.add(v);}
  }return out;
 }
 public static int dominant(float[] v){float x=Math.abs(v[3]),y=Math.abs(v[4]),z=Math.abs(v[5]);return y>=x&&y>=z?1:x>=z?0:2;}
 /** Portal coordinates belong to the whole authored surface, independent of chunk/anchor. */
 public static float[][] portalBounds(float[] raw){
  float[][] bounds=new float[3][6];for(var b:bounds){Arrays.fill(b,0,3,Float.POSITIVE_INFINITY);Arrays.fill(b,3,6,Float.NEGATIVE_INFINITY);}
  for(int i=0;i<raw.length;i+=11){int m=Math.round(raw[i+9])-7;if(m<0||m>2)continue;for(int a=0;a<3;a++){bounds[m][a]=Math.min(bounds[m][a],raw[i+a]);bounds[m][a+3]=Math.max(bounds[m][a+3],raw[i+a]);}}
  return bounds;
 }
 public static float[] withPortalUv(float[] raw,int offset,float[][] bounds){
  float[] v=Arrays.copyOfRange(raw,offset,offset+13);System.arraycopy(raw,offset,v,0,11);
  int m=Math.round(v[9])-7;if(m>=0&&m<3){int normal=dominant(v),u=normal==0?2:0,w=normal==1?2:1;float[] b=bounds[m];v[11]=(v[u]-b[u])/Math.max(.0001f,b[u+3]-b[u]);v[12]=1-(v[w]-b[w])/Math.max(.0001f,b[w+3]-b[w]);}
  return v;
 }
 public static boolean backwards(float[] a,float[] b,float[] c){float x=b[0]-a[0],y=b[1]-a[1],z=b[2]-a[2],xx=c[0]-a[0],yy=c[1]-a[1],zz=c[2]-a[2];return (y*zz-z*yy)*a[3]+(z*xx-x*zz)*a[4]+(x*yy-y*xx)*a[5]<0;}
 private TartarusMeshData(){}
}
