package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.city.*;
import com.gx.velveteclipse.client.city.CityQuadVertices;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.zip.GZIPInputStream;

/** Regression coverage for triangle-to-quad conversion and installed-world migration. */
public final class CityGeometryAudit {
 static int checks;static void check(boolean v,String s){checks++;if(!v)throw new AssertionError(s);}
 static double[] cross(float[] a,float[] b,float[] c){double x=b[0]-a[0],y=b[1]-a[1],z=b[2]-a[2],u=c[0]-a[0],v=c[1]-a[1],w=c[2]-a[2];return new double[]{y*w-z*v,z*u-x*w,x*v-y*u};}
 static double length(double[] v){return Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);}
 public static void main(String[] args)throws Exception{
  check(CityPlan.get().revision()==5,"Geometry correction schedules revision 5");
  for(int revision:new int[]{3,4}){
   var s=new CityState();s.choice=1;s.revision=revision;s.ready=true;
   check(CityBuilder.beginRevision(s)&&s.targetRevision==5,"Existing city upgrades");
   s.cursor=71;check(!CityBuilder.beginRevision(s)&&s.cursor==71,"Upgrade resumes without resetting progress");
   var method=CityBuilder.class.getDeclaredMethod("legacyDoorTile",int.class,int[].class,int.class);method.setAccessible(true);
   CityFurniture.Catalogue old;
   try(var in=new InputStreamReader(new GZIPInputStream(CityGeometryAudit.class.getResourceAsStream("/data/velvet_eclipse/city/legacy-furniture-v"+revision+".json.gz")))){old=new com.google.gson.Gson().fromJson(in,CityFurniture.Catalogue.class);}
   for(var d:old.doors())for(var c:d.cells()){
    check((boolean)method.invoke(null,revision,c,c[4])&&(boolean)method.invoke(null,revision,c,c[5]),"Both old door states recognized at original coordinates");
    check(!(boolean)method.invoke(null,revision,new int[]{c[0]+5000,c[1],c[2]},c[4]),"Player-placed tile elsewhere is not owned by migration");
   }
   var night=new CityNightSchool.State();var tag=night.m_7176_(new net.minecraft.nbt.CompoundTag());tag.m_128405_("phase",3);tag.m_128405_("schoolRevision",revision);
   var load=CityNightSchool.State.class.getDeclaredMethod("load",net.minecraft.nbt.CompoundTag.class);load.setAccessible(true);
   var restore=(CityNightSchool.State)load.invoke(null,tag);
   check(restore.m_7176_(new net.minecraft.nbt.CompoundTag()).m_128451_("phase")==4,"Previous night journal restores before rebuilding");
  }
  var used=new HashSet<Integer>();for(var c:CityPlan.get().cells())if(c[3]==40)used.add(c[4]);
  for(var d:CityFurniture.catalogue().doors())for(var c:d.cells()){used.add(c[4]);used.add(c[5]);}
  var last=CityArchitecture.tile(CityArchitecture.size()-1);float[] raw=new float[last.offset()+last.length()];
  try(var in=new DataInputStream(new GZIPInputStream(CityGeometryAudit.class.getResourceAsStream("/assets/velvet_eclipse/city/architecture.bin.gz")))){for(int i=0;i<raw.length;i++)raw[i]=in.readFloat();}
  float[][] q=new float[4][13];long faces=0;double maxError=0;
  for(int id:used){var t=CityArchitecture.tile(id);for(int at=t.offset();at<t.offset()+t.length();at+=39){
   for(int i=0;i<4;i++)for(int j=0;j<13;j++)q[i][j]=CityQuadVertices.component(raw,at,i,j);
   double original=length(cross(q[0],q[1],q[3])),first=length(cross(q[0],q[1],q[2])),second=length(cross(q[0],q[2],q[3]));
   // Tiny clipping slivers have no visible area; validate all visible faces strictly.
   if(original<1e-8)continue;
   check(first>0&&second>0,"Nondegenerate halves, tile "+id);
   double error=Math.abs(first+second-original);maxError=Math.max(maxError,error);check(error<2e-7,"Quad preserves exact triangle coverage");
   double[] n=cross(q[0],q[1],q[2]);check(n[0]*q[0][3]+n[1]*q[0][4]+n[2]*q[0][5]>0,"Winding follows authored surface normal");
   faces++;
  }}
  var out=Path.of("build/reports/gekkoukan/geometry-validation.json");Files.createDirectories(out.getParent());
  Files.writeString(out,"{\"checks\":"+checks+",\"faces\":"+faces+",\"maxAreaError\":"+maxError+",\"cityRevision\":5,\"inGame\":false}");
  System.out.println("PASS city geometry: "+checks+" checks; "+faces+" nondegenerate native faces; revisions 3/4 migration");
 }
}
