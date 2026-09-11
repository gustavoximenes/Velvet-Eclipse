package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.HudDesign;
import com.gx.velveteclipse.client.exploration.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;
import java.nio.file.*;

/** Regression cases for the player's reported backwards arms and skin-colored entry. */
public final class StylePoseAudit {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        for(boolean slim:new boolean[]{false,true}){
            var root=LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,slim),64,64).m_171564_();
            var right=root.m_171324_("right_arm");var left=root.m_171324_("left_arm");
            for(double phase=0;phase<Math.PI*2;phase+=.15){
                var palm=hand(right,"rightArm",phase,1,-1,false);var blade=hand(right,"rightArm",phase,1,-1,true);
                check(palm.y>.65&&Math.abs(palm.z)<.18,"relaxed hand near hip, not pointing forward");
                check(palm.x-blade.x>.6&&blade.y-palm.y>.35&&Math.abs(blade.z-palm.z)<.65,"blade angles out to the side and down, not straight behind");
                check(blade.y<1.50,"lowered sword stays above standing floor");
                check(Math.abs(ExplorationPose.bodyBob(phase,1,40))<.015,"body translation is blocks, not pixels");
                check(ExplorationPose.bend("rightLeg",Math.PI/2,1,-1).getY()>ExplorationPose.bend("rightLeg",-Math.PI/2,1,-1).getY(),"knee folds in rear recovery");
                check(ExplorationPose.bend("leftArm",phase,1,-1).getY()<0,"elbow must bend towards -Z");
                var r=ExplorationPose.rotation("leftArm",phase,1,-1);var b=ExplorationPose.bend("leftArm",phase,1,-1);
                check(Math.abs(r.getX())<.53&&b.getY()<0,"free arm has moderate counter-swing and natural elbow");
                for(double t=4;t<=7;t+=.5){
                    check(hand(right,"rightArm",phase,1,t,false).z<-.20,"hit-window hand in front of torso");
                    check(hand(right,"rightArm",phase,1,t,true).z<-.65,"hit-window blade points toward forward target");
                }
            }
        }
        for(float alpha:new float[]{0,.25f,.5f,1}){
            var sink=new ColorCapture();var blue=new EntrySilhouette(sink,alpha);
            var root=LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,false),64,64).m_171564_();
            root.m_104306_(new PoseStack(),blue,0,0,.05f,.08f,.01f,.12f);
            check(sink.vertices>100,"blue keeps real model vertices");
            check(sink.colors==sink.vertices,"one uniform color per vertex");
            check(sink.r==22&&sink.g==22&&sink.b==255,"skin/lighting cannot darken the reference blue");
            check(sink.a==Math.round(alpha*255),"transition alpha reaches body and sword unchanged");
            check(sink.extra==0,"POSITION_COLOR receives no texture/lighting attributes");
        }
        for(int[] size:new int[][]{{854,480},{1280,720},{1920,1080},{1024,768},{2560,1080}})for(int scale=1;scale<=4;scale++){
            int w=size[0]/scale,h=size[1]/scale;var panel=HudDesign.explorationPanel(w,h);
            var c=new HudDesign.Canvas(){
                public int width(String s){return s.length()*6;}
                public void fill(int x,int y,int bw,int bh,int color){check(x>=0&&y>=panel.y()+panel.h()&&x+bw<=w&&y+bh<=h-28,"hints below HUD and above hotbar");}
                public void text(String s,int x,int y,int color){check(y>=panel.y()+panel.h()&&x>=panel.x()&&x+width(s)<=panel.x()+panel.w(),"hint text attached to player card");}
            };
            HudDesign.explorationHints(c,w,h,"H");HudDesign.explorationHints(c,w,h,"RIGHT SHIFT");
        }
        preview();
        System.out.println("PASS style/pose: "+checks+" checks; forward sword/arm geometry, unlit blue vertices, HUD footer at 20 sizes/scales.");
    }
    private static Vector3f hand(ModelPart arm,String name,double phase,double speed,double tick,boolean tip){
        var r=ExplorationPose.rotation(name,phase,speed,tick);arm.m_171327_(r.getX(),r.getY(),r.getZ());
        var pose=new PoseStack();ExplorationSwordLayer.bindHand(pose,arm,ExplorationPose.bend(name,phase,speed,tick));
        ExplorationSwordLayer.grip(pose,tick);
        return new Vector3f(0,tip?1.36f:0,0).mulPosition(pose.m_85850_().m_252922_());
    }
    private static class ColorCapture implements VertexConsumer{
        int vertices,colors,extra,r,g,b,a;
        public VertexConsumer m_5483_(double x,double y,double z){vertices++;return this;}
        public VertexConsumer m_6122_(int r,int g,int b,int a){colors++;this.r=r;this.g=g;this.b=b;this.a=a;return this;}
        public VertexConsumer m_7421_(float u,float v){extra++;return this;}public VertexConsumer m_7122_(int u,int v){extra++;return this;}
        public VertexConsumer m_7120_(int u,int v){extra++;return this;}public VertexConsumer m_5601_(float x,float y,float z){extra++;return this;}
        public void m_5752_(){}public void m_7404_(int r,int g,int b,int a){extra++;}public void m_141991_(){extra++;}
    }
    private static void preview()throws Exception{
        // Offline geometric preview, not a Minecraft GPU capture.
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='960' height='540' viewBox='0 0 960 540'><rect width='960' height='540' fill='#000416'/>");
        var root=LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,false),64,64).m_171564_();
        var pose=new PoseStack();pose.m_85837_(960*.45,540*.49,1000);pose.m_85841_(540*.33f,540*.33f,-540*.33f);
        pose.m_252781_(new Quaternionf().rotationZ(-.19f).rotateY(-.72f).rotateX(.22f));pose.m_85837_(0,-.55,0);
        var capture=new ColorCapture(){int count;StringBuilder quad=new StringBuilder();
            @Override public VertexConsumer m_5483_(double x,double y,double z){quad.append(String.format(Locale.ROOT,"%.2f,%.2f ",x,y));return this;}
            @Override public void m_5752_(){if(++count%4==0){svg.append("<polygon fill='#1616ff' points='").append(quad).append("'/>");quad.setLength(0);}}
        };
        var blue=new EntrySilhouette(capture,1);
        for(String name:List.of("head","body","right_arm","left_arm","right_leg","left_leg")){
            var part=root.m_171324_(name);String key=switch(name){case "right_arm"->"rightArm";case "left_arm"->"leftArm";case "right_leg"->"rightLeg";case "left_leg"->"leftLeg";default->name;};
            var r=ExplorationPose.rotation(key,2,1,-1);if(!name.equals("body"))part.m_171327_(r.getX(),r.getY(),r.getZ());part.m_104301_(pose,blue,0,0);
        }
        svg.append("<text x='605' y='459' fill='#f3fcff' font-family='sans-serif' font-weight='bold' font-style='italic' font-size='54'>BATTLE</text>");
        HudDesign.Canvas c=new HudDesign.Canvas(){
            public int width(String s){return s.length()*6;}
            public void fill(int x,int y,int w,int h,int argb){svg.append(String.format(Locale.ROOT,"<rect x='%d' y='%d' width='%d' height='%d' fill='#%06x' fill-opacity='%.3f'/>",x,y,w,h,argb&0xffffff,(argb>>>24)/255.0));}
            public void text(String s,int x,int y,int color){svg.append(String.format("<text x='%d' y='%d' font-family='sans-serif' font-size='9' fill='#%06x'>%s</text>",x,y+8,color&0xffffff,s));}
            public String playerName(){return "JOGADOR";}
        };
        svg.append("<g transform='translate(28,28) scale(2)'>");
        var v=new HudDesign.View(false,false,false,"",90,100,40,50,"",0,1,.9,.8,0,0,"","MAIN",List.of(),List.of(),"",0,false,0,"");
        HudDesign.paint(c,v,300,100,0,0);HudDesign.explorationHints(c,300,100,"H");svg.append("</g></svg>");
        Path path=Path.of("build/reports/style-pose-preview.svg");Files.createDirectories(path.getParent());Files.writeString(path,svg);
    }
}
