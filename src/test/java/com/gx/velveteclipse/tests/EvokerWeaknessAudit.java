package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.WeaknessHitS2C;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.Direction;
import io.github.kosmx.bendylib.ICuboidBuilder;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import org.joml.Vector3f;
import java.util.*;
import java.nio.file.*;

public final class EvokerWeaknessAudit {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    private static BendableCuboid bent(boolean slim){return new BendableCuboid.Builder().setDirection(Direction.UP).build(new ICuboidBuilder.Data(40,16,slim?-2:-3,-2,-2,slim?3:4,12,4,0,0,0,false,64,64));}
    public static void main(String[] args)throws Exception{
        for(boolean slim:new boolean[]{false,true}){
            var root=LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,slim),64,64).m_171564_();var arm=root.m_171324_("right_arm");var mesh=bent(slim);Vector3f previous=null;
            for(double t=3;t<=54;t+=.1){
                var solution=EvokerPose.arm(t,slim);var r=solution.rotation();arm.m_171327_(r.getX(),r.getY(),r.getZ());
                check(solution.bend().getY()<2.2,"elbow does not fold beyond natural range at "+t+" slim="+slim);
                var stack=new PoseStack();var orient=EvokerHandLayer.bindHand(stack,arm,solution.bend(),slim);var hand=new Vector3f().mulPosition(stack.m_85850_().m_252922_());
                check(hand.distance(solution.target())<.008,"actual native hand reaches target at "+t+" slim="+slim+" actual="+hand+" target="+solution.target()+" rotation="+r+" bend="+solution.bend());
                // Compare the prop with Bendy's actual deformation, independently of our IK.
                var deformation=mesh.applyBend(solution.bend().getX(),solution.bend().getY());
                var physicalHand=new Vector3f(slim?-.5f:-1,10,0).mulPosition(deformation).div(16);
                var upper=new PoseStack();arm.m_104299_(upper);physicalHand.mulPosition(upper.m_85850_().m_252922_());
                check(physicalHand.distance(hand)<.002,"Evoker remains attached to Bendy hand at "+t);
                if(previous!=null)check(hand.distance(previous)<.03,"no hand jump between partial frames");previous=hand;
                if(t>=31&&t<=37){
                    stack.m_252781_(orient.invert());stack.m_252781_(EvokerPose.weaponRotation(t));
                    var muzzle=new Vector3f(-3.6225f/16,-1.52f/16,0).mulPosition(stack.m_85850_().m_252922_());
                    var direction=new Vector3f(-1,0,0).mulDirection(stack.m_85850_().m_252922_());
                    check(muzzle.x<-.25&&muzzle.x>-.32&&muzzle.y<-.2&&muzzle.y>-.35,"muzzle beside temple without crossing face");
                    check(direction.x>.99,"barrel aims inward toward temple");
                }
            }
        }
        check(Math.abs(EvokerPose.spin(20)-EvokerPose.spin(10)-2*Math.PI)<1e-8,"one full weapon spin before aiming");
        check(EvokerPose.blend(0)==0&&EvokerPose.blend(60)==0,"smooth layer endpoints");
        battle();state();preview();cutinPreview();
        System.out.println("PASS evoker/weakness: "+checks+" checks; native Bendy grip, temple alignment, spin, confirmed weakness/miss/critical/kill, cut-in lifecycle.");
    }
    private static void battle(){
        PersonaRegistry.bootstrap();
        PersonaRegistry.registerSkill(new SkillDefinition("audit_cast","Audit Cast",Element.FIRE,40,96,4,0,true,SkillTarget.ONE_ENEMY));
        for(Affinity affinity:Affinity.values())for(boolean miss:new boolean[]{false,true})for(boolean critical:new boolean[]{false,true})for(int targetHp:new int[]{1,1000}){
            PersonaRegistry.registerPersona(new PersonaDefinition("audit_player","P",Arcana.FOOL,1,new PersonaStats(6,6,6,99,6),Map.of(),Map.of(1,List.of("audit_cast"))));
            PersonaRegistry.registerPersona(new PersonaDefinition("audit_enemy","E",Arcana.FOOL,1,new PersonaStats(6,6,6,1,6),Map.of(Element.FIRE,affinity),Map.of()));
            var player=new BattleCombatant(new UUID(0,1),"P",BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona("audit_player").orElseThrow()),1000,1000,100,100);
            var enemy=new BattleCombatant(new UUID(0,2),"E",BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("audit_enemy").orElseThrow()),1000,targetHp,100,100);
            var session=new BattleSession(List.of(player,enemy),new Random(){public int nextInt(int bound){return miss?99:critical?0:50;}});
            var result=session.perform(player.id(),BattleAction.skill("audit_cast",enemy.id()));
            check(result.weaknessHit()==(affinity==Affinity.WEAK&&!miss),"weakness event follows resolved damage "+affinity+" miss="+miss);
            if(session.state()==BattleState.ACTIVE)check(!session.perform(session.currentActor().id(),BattleAction.guard()).weaknessHit(),"flag does not leak into next action");
        }
    }
    private static void state(){
        var s=new WeaknessCutinState();for(int i=0;i<120;i++)s.tick();check(!s.visible(),"time alone cannot show cut-in");
        s.trigger(7);check(s.visible(),"server event shows cut-in");s.tick();s.trigger(7);check(s.time(0)==1,"duplicate event cannot restart");
        for(int i=0;i<20;i++)s.tick();check(!s.visible(),"cut-in expires");s.trigger(8);check(s.visible(),"next weak hit works");s.reset();check(!s.visible(),"logout/reset clears cut-in");
        var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{WeaknessHitS2C.encode(new WeaknessHitS2C(1234),buffer);check(WeaknessHitS2C.decode(buffer).id()==1234,"event codec");}finally{buffer.release();}
        for(int[] size:new int[][]{{320,180},{480,270},{854,480},{1920,1080}}){var l=WeaknessCutinDesign.layout(size[0],size[1],5);check(l.y()>size[1]/4&&l.y()+l.height()<size[1]*.7,"cut-in leaves world visible above and below");}
    }
    private static void preview()throws Exception{
        var frames=new ArrayList<Object>();
        var gunField=EvokerHandLayer.class.getDeclaredField("gun");gunField.setAccessible(true);var gun=(ModelPart)gunField.get(new EvokerHandLayer(null));
        for(double time:new double[]{5,11,15,20,31,38}){
            var root=LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_,false),64,64).m_171564_();var vertices=new Geometry();
            for(String name:List.of("head","body","left_arm","right_leg","left_leg")){vertices.part=name;root.m_171324_(name).m_104301_(new PoseStack(),vertices,15728880,655360);}
            var r=EvokerPose.arm(time,false);var arm=root.m_171324_("right_arm");arm.m_171327_(r.rotation().getX(),r.rotation().getY(),r.rotation().getZ());
            var pose=new PoseStack();arm.m_104299_(pose);var bent=bent(false);bent.applyBend(r.bend().getX(),r.bend().getY());vertices.part="right_arm";bent.render(pose.m_85850_(),vertices,1,1,1,1,15728880,655360);
            pose=new PoseStack();var rotation=EvokerHandLayer.bindHand(pose,arm,r.bend(),false);pose.m_252781_(rotation.invert());pose.m_252781_(EvokerPose.weaponRotation(time));vertices.part="gun";gun.m_104301_(pose,vertices,15728880,655360);
            frames.add(Map.of("time",time,"quads",vertices.quads));
        }
        Path file=Path.of("build/reports/evoker-geometry.json");Files.createDirectories(file.getParent());Files.writeString(file,new com.google.gson.Gson().toJson(frames));
    }
    private static class Geometry implements VertexConsumer{
        String part;List<Object> quads=new ArrayList<>();List<double[]> points=new ArrayList<>();
        public VertexConsumer m_5483_(double x,double y,double z){points.add(new double[]{x,y,z});return this;}
        public VertexConsumer m_6122_(int a,int b,int c,int d){return this;}public VertexConsumer m_7421_(float u,float v){return this;}
        public VertexConsumer m_7122_(int a,int b){return this;}public VertexConsumer m_7120_(int a,int b){return this;}public VertexConsumer m_5601_(float x,float y,float z){return this;}
        public void m_5752_(){if(points.size()==4){quads.add(Map.of("part",part,"points",points));points=new ArrayList<>();}}public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
    }
    private static void cutinPreview()throws Exception{
        StringBuilder svg=new StringBuilder("<svg xmlns='http://www.w3.org/2000/svg' width='960' height='540'><rect width='960' height='540' fill='#102522'/>");
        WeaknessCutinDesign.paint(new WeaknessCutinDesign.Canvas(){
            public void fill(int x,int y,int w,int h,int color){if(w>0&&h>0)svg.append(String.format(Locale.ROOT,"<rect x='%d' y='%d' width='%d' height='%d' fill='#%06x' fill-opacity='%.3f'/>",x,y,w,h,color&0xffffff,(color>>>24)/255.0));}
            public void text(String s,int x,int y,int c){}public int width(String s){return s.length()*6;}
            public void eyes(int x,int y,int w,int h,float alpha){
                int[][] pixels={{0,0,0,0,0,0,0,0},{0,1,2,0,0,2,1,0},{0,0,0,0,0,0,0,0}};
                int[] colors={0xff3095c4,0xffdcfcff,0xff092954};
                for(int row=0;row<3;row++)for(int col=0;col<8;col++)fill(x+col*w/8,y+row*h/3,(col+1)*w/8-col*w/8,(row+1)*h/3-row*h/3,colors[pixels[row][col]]);
            }
        },960,540,6);
        svg.append("<text x='18' y='510' fill='white' font-family='sans-serif' font-size='16'>Previa offline da faixa; padrao de olhos ilustrativo. No jogo: skin do jogador.</text></svg>");
        Files.writeString(Path.of("build/reports/weakness-cutin-preview.svg"),svg);
    }
}
