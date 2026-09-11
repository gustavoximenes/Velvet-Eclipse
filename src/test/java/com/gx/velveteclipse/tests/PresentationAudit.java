package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.exploration.*;
import com.gx.velveteclipse.client.HudDesign;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import org.joml.Quaternionf;

public final class PresentationAudit {
    private static int checks;
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
    private static double length(Vec3 v){return Math.sqrt(v.f_82479_*v.f_82479_+v.f_82480_*v.f_82480_+v.f_82481_*v.f_82481_);}
    private static double box(Vec3 from,Vec3 to,double[] min,double[] max){
        double[] start={from.f_82479_,from.f_82480_,from.f_82481_},end={to.f_82479_,to.f_82480_,to.f_82481_};double enter=0,exit=1;
        for(int i=0;i<3;i++){
            double d=end[i]-start[i];if(Math.abs(d)<1e-10){if(start[i]<min[i]||start[i]>max[i])return Double.POSITIVE_INFINITY;continue;}
            double a=(min[i]-start[i])/d,b=(max[i]-start[i])/d;
            enter=Math.max(enter,Math.min(a,b));exit=Math.min(exit,Math.max(a,b));if(enter>exit)return Double.POSITIVE_INFINITY;
        }
        return enter*length(to.m_82546_(from));
    }
    public static void main(String[] args){
        Vec3 feet=new Vec3(0,64,0);
        // Reproduces the previous y error: interpolated feet minus eye height.
        check(feet.f_82480_-1.62+1.45<64,"old anchor should reproduce ground penetration");
        for(int yaw=-180;yaw<=180;yaw+=15)for(int pitch=-90;pitch<=90;pitch+=10){
            var f=CameraPath.exploration(feet,yaw,pitch,false);
            check(f.pivot().f_82480_>65,"pivot above feet");check(f.desired().f_82480_>65.6,"camera above standing ground");
            var flight=CameraPath.exploration(new Vec3(0,94,0),yaw,pitch,false);
            check(Math.abs(flight.desired().f_82480_-f.desired().f_82480_-30)<1e-8,"flight must not fix/change relative framing");
            Vec3 clear=CameraPath.clip(f.pivot(),f.desired(),(a,b)->Double.POSITIVE_INFINITY);
            check(clear.m_82554_(f.desired())<1e-9,"no obstruction preserves framing");
            Vec3 ceiling=CameraPath.clip(f.pivot(),f.desired(),(a,b)->box(a,b,new double[]{-20,66,-20},new double[]{20,100,20}));
            check(ceiling.f_82480_<65.85,"ceiling clearance");
            Vec3 room=CameraPath.clip(f.pivot(),f.desired(),(a,b)->Math.min(box(a,b,new double[]{-20,0,-20},new double[]{20,64,20}),box(a,b,new double[]{-20,0,-4},new double[]{20,100,-2})));
            check(room.f_82480_>64.2&&room.f_82481_> -1.86,"wall/floor clearance");
        }
        for(String part:List.of("body","head","rightArm","leftArm","rightLeg","leftLeg"))for(double speed:new double[]{0,.5,1}){
            var idle=ExplorationPose.rotation(part,.7,speed,-1);var start=ExplorationPose.rotation(part,.7,speed,0);var end=ExplorationPose.rotation(part,.7,speed,18);
            check(distance(idle,start)<1e-5&&distance(idle,end)<1e-5,"no pop on attack start/end: "+part);
            var previous=start;
            for(double tick=.025;tick<=18;tick+=.025){var current=ExplorationPose.rotation(part,.7,speed,tick);check(distance(previous,current)<.07,"bounded per-frame pose motion "+part);previous=current;}
        }
        hud();models();System.out.println("PASS presentation: "+checks+" checks; floors/walls/ceilings, 475 camera views, pose continuity, native standard/slim model vertices, enemy-only skill HUD.");
    }
    private static double distance(dev.kosmx.playerAnim.core.util.Vec3f a,dev.kosmx.playerAnim.core.util.Vec3f b){return Math.abs(a.getX()-b.getX())+Math.abs(a.getY()-b.getY())+Math.abs(a.getZ()-b.getZ());}
    private static void hud(){
        class Canvas implements HudDesign.Canvas {int faces;List<String> text=new ArrayList<>();public void fill(int x,int y,int w,int h,int c){}public void text(String s,int x,int y,int c){text.add(s);}public int width(String s){return s.length()*5;}public void portrait(int x,int y,int size){faces++;}public String playerName(){return "ActualPlayer";}}
        for(boolean cinematic:new boolean[]{false,true}){
            var c=new Canvas();var v=new HudDesign.View(true,true,false,"PERSONA_SHOULD_NOT_HAVE_HP",31,120,29,45,"Umbra Slime",70,85,.3,.5,.8,.9,"","MAIN",List.of(),List.of("1","2","3","4","5","6"),"Back",0,cinematic,65,"Ember");
            HudDesign.paint(c,v,480,270,0,0);
            check(!c.text.contains("PERSONA_SHOULD_NOT_HAVE_HP"),"no persona health label");
            check(c.faces==(cinematic?0:1),"skin portrait only on player HUD");
            check(!cinematic||c.text.stream().noneMatch(s->s.equals("HP")||s.contains("SP")),"enemy only during skill");
        }
    }
    private static void models(){
        for(boolean slim:new boolean[]{false,true}){
            ModelPart root=LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,slim),64,64).m_171564_();
            for(int frame=0;frame<=32;frame++){
                PoseStack pose=new PoseStack();pose.m_85837_(1920*.43+frame/32.0*1920*.035,1080*.49,1000);pose.m_85841_(1080*.33f,1080*.33f,-1080*.33f);
                pose.m_252781_(new Quaternionf().rotationZ(-.19f).rotateY(-.72f).rotateX(.22f));pose.m_85837_(0,-.55,0);
                var vertices=new Capture();
                for(String name:List.of("head","body","right_arm","left_arm","right_leg","left_leg")){
                    var part=root.m_171324_(name);String key=switch(name){case "right_arm"->"rightArm";case "left_arm"->"leftArm";case "right_leg"->"rightLeg";case "left_leg"->"leftLeg";default->name;};
                    var r=ExplorationPose.rotation(key,frame*.52,1,-1);if(!name.equals("body"))part.m_171327_(r.getX(),r.getY(),r.getZ());part.m_104301_(pose,vertices,15728880,655360);
                }
                check(vertices.count>100,"native model has geometry");check(vertices.minX>0&&vertices.maxX<1920&&vertices.minY>0&&vertices.maxY<1080,"3D entry model stays inside viewport");check(vertices.minZ>100&&vertices.maxZ<2000,"3D entry depth remains in front of wipe");
            }
        }
    }
    private static class Capture implements VertexConsumer{
        int count;double minX=1e6,maxX=-1e6,minY=1e6,maxY=-1e6,minZ=1e6,maxZ=-1e6;
        public VertexConsumer m_5483_(double x,double y,double z){count++;minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);minZ=Math.min(minZ,z);maxZ=Math.max(maxZ,z);return this;}
        public VertexConsumer m_6122_(int r,int g,int b,int a){return this;}public VertexConsumer m_7421_(float u,float v){return this;}public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float x,float y,float z){return this;}public void m_5752_(){}public void m_7404_(int r,int g,int b,int a){}public void m_141991_(){}
    }
}
