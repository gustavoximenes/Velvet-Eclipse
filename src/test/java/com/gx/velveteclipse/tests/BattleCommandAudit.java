package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.*;
import com.gx.velveteclipse.core.battle.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import java.util.*;
import java.nio.file.*;

public final class BattleCommandAudit {
    private static int checks;
    private static void check(boolean condition,String why){checks++;if(!condition)throw new AssertionError(why);}
    private static BattleSession session(Affinity affinity,int hp,int sp,long seed){
        PersonaRegistry.registerSkill(new SkillDefinition("command_audit","Audit",Element.FIRE,40,96,4,0,true,SkillTarget.ONE_ENEMY));
        PersonaRegistry.registerPersona(new PersonaDefinition("command_player","P",Arcana.FOOL,1,new PersonaStats(6,6,6,99,6),Map.of(),Map.of(1,List.of("command_audit"))));
        PersonaRegistry.registerPersona(new PersonaDefinition("command_enemy","E",Arcana.FOOL,1,new PersonaStats(6,6,6,1,6),Map.of(Element.FIRE,affinity,Element.PHYSICAL,affinity),Map.of()));
        var player=new BattleCombatant(new UUID(0,1),"P",BattleSide.PLAYERS,new PersonaInstance(PersonaRegistry.persona("command_player").orElseThrow()),1000,1000,100,sp);
        var enemy=new BattleCombatant(new UUID(0,2),"E",BattleSide.ENEMIES,new PersonaInstance(PersonaRegistry.persona("command_enemy").orElseThrow()),1000,hp,100,100);
        return new BattleSession(List.of(player,enemy),new Random(seed));
    }
    private static String state(BattleSession session){return session.state()+":"+session.participants().stream().map(p->p.hp()+"/"+p.sp()+"/"+p.downed()+"/"+p.guarding()).toList();}
    public static void main(String[] args)throws Exception{
        PersonaRegistry.bootstrap();
        for(Affinity affinity:Affinity.values())for(boolean basic:new boolean[]{false,true})for(int hp:new int[]{1,1000})for(int seed=0;seed<80;seed++){
            var immediate=session(affinity,hp,100,seed);var delayed=session(affinity,hp,100,seed);
            var actor=delayed.currentActor();var target=delayed.enemiesOf(actor).get(0);UUID id=actor.id();
            var action=basic?BattleAction.attack(target.id()):BattleAction.skill("command_audit",target.id());
            String before=state(delayed);var reserved=delayed.prepare(id,action);
            check(reserved!=null,"valid action reserved");check(before.equals(state(delayed)),"preparation cannot spend HP/SP, deal damage or advance turn");
            check(delayed.prepare(id,action)==null,"duplicate click cannot reroll");
            delayed.perform(id,BattleAction.guard());check(before.equals(state(delayed)),"another command cannot interrupt reserved action");
            var now=immediate.perform(id,action);var later=delayed.perform(id,action);
            check(now.equals(later)&&state(immediate).equals(state(delayed)),"reserved outcome exactly matches the direct seeded result");
            check(delayed.prepared()==null,"resolved reservation released");
            check(reserved.cutin()==(!basic&&later.weaknessHit()),"cut-in only for confirmed Persona weakness; never normal sword");
            check(!basic||reserved.hit().element()==Element.SLASH,"sword deals slash");
            var knowledge=new AffinityKnowledge();check(knowledge.get("shadow",reserved.hit().element())==null,"unseen type/element is unknown");
            knowledge.learn("shadow",reserved.hit());var learned=knowledge.get("shadow",reserved.hit().element());
            check(learned==(reserved.hit().missed()?null:affinity),"miss cannot reveal affinity");
            check(knowledge.get("other_shadow",reserved.hit().element())==null,"knowledge scoped to enemy type");
            check(AffinityKnowledge.decode(knowledge.encode()).get("shadow",reserved.hit().element())==learned,"discoveries survive serialization");
        }
        var rejected=session(Affinity.WEAK,1000,0,2);String before=state(rejected);
        check(rejected.prepare(rejected.currentActor().id(),BattleAction.skill("command_audit",new UUID(0,2)))==null,"insufficient SP rejected before animation");
        check(before.equals(state(rejected)),"rejected preparation unchanged");
        var abort=session(Affinity.WEAK,1000,100,2);before=state(abort);abort.prepare(new UUID(0,1),BattleAction.skill("command_audit",new UUID(0,2)));abort.cancelPrepared();
        check(before.equals(state(abort))&&abort.prepared()==null,"abort releases action without cost or damage");
        check(BattleTargetHud.indicator(null).equals("?"),"unknown marker");check(BattleTargetHud.indicator(Affinity.WEAK).equals("! WEAK"),"known weakness marker");
        for(Affinity a:Affinity.values())if(a!=Affinity.WEAK)check(BattleTargetHud.indicator(a).isEmpty(),"no extra badge for known non-weak affinity");
        for(double t=0;t<=110;t+=.05){
            check(WeaknessCutinDesign.summonVisible(t)==(t>=12&&t<51),"cut-in window occurs before impact");
            check(t>=38||WeaknessCutinDesign.shatter(t)==0,"intact until trigger");
            double p=MeleeTimeline.progress(t);check(p>=0&&p<=1,"melee stays inside safe path");
            if(t>0)check(Math.abs(p-MeleeTimeline.progress(t-.05))<.01,"continuous foot displacement");
        }
        check(MeleeTimeline.progress(0)==0&&MeleeTimeline.progress(12)==1&&MeleeTimeline.progress(44)==0,"approach and return endpoints");
        check(MeleeTimeline.slash(MeleeTimeline.IMPACT)==7,"damage synchronized to sword contact pose");
        check(WeaknessCutinDesign.shatter(38)==0&&WeaknessCutinDesign.shatter(51)==1,"shards disperse from the trigger");
        Set<Integer> tones=new HashSet<>();int previousLuma=-1;
        for(int i=0;i<256;i++){
            int result=PersonaSelectorPortrait.posterize((i<<24)|(i<<16)|(i<<8)|i,120);
            check((result>>>24)==i,"portrait preserves every alpha value");
            int r=result&255,g=result>>>8&255,b=result>>>16&255;
            check(b>=g&&g>=r,"portrait remains in the blue/cyan/white palette");
            int luma=r*3+g*6+b;check(luma>=previousLuma,"skin shading retains luminance order");previousLuma=luma;tones.add(result&0xFFFFFF);
        }
        check(tones.size()>150,"fine skin detail survives instead of collapsing into two tones");
        check(PersonaSelectorPortrait.posterize(0xFF202020,70)!=PersonaSelectorPortrait.posterize(0xFF303030,70),"dark hair and face pixels stay distinguishable");
        check(PersonaBattleDesign.entrance(0)==0&&PersonaBattleDesign.entrance(.28)==1,"short opening has exact endpoints");
        codec();layout();previews();portraitGeometry();
        System.out.println("PASS battle-command: "+checks+" checks; prepared outcomes, slash, knowledge, summon/shatter clock, melee path, packet roundtrip, responsive selector.");
    }
    private static void codec(){
        var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{var cue=new BattleCueS2C("skill","ember",true,100,62,-300,104,62,-298);BattleCueS2C.encode(cue,buffer);check(cue.equals(BattleCueS2C.decode(buffer)),"cue codec");
            var target=new BattleTargetS2C(321,"umbra_slime","FIRE:WEAK,SLASH:NORMAL");BattleTargetS2C.encode(target,buffer);check(target.equals(BattleTargetS2C.decode(buffer)),"target codec");
        }finally{buffer.release();}
    }
    private static List<SkillDefinition> skills(){return List.of("ember","frost_shard","volt_spark","gale_cut","cleave","lumen_ray","shade_bite").stream().map(id->PersonaRegistry.skill(id).orElseThrow()).toList();}
    private static void layout(){
        for(int[] resolution:new int[][]{{1280,720},{1920,1080},{2560,1440},{1024,768},{3440,1440}})for(int scale=1;scale<=4;scale++){
            int w=resolution[0]/scale,h=resolution[1]/scale;var canvas=new Capture(w,h);
            PersonaBattleDesign.paint(canvas,new PersonaBattleDesign.View("Thanatos",skills(),6,120,120,45,false,1),w,h);
            check(canvas.portraits==1,"one real-player portrait slot");
            check(PersonaBattleDesign.next(w,h).x()+PersonaBattleDesign.next(w,h).w()<w*.70,"right side of arena remains open");
            int lastY=-1;for(Object op:canvas.ops){var m=(Map<?,?>)op;if("fill".equals(m.get("type"))&&((Number)m.get("color")).intValue()==0xFF161BFF){
                int y=((Number)m.get("y")).intValue(),height=((Number)m.get("h")).intValue();
                check(y>=lastY,"fractional-scale scanlines do not overlap and darken bands");lastY=y+height;
            }}
            for(double age:new double[]{0,.06,.14,.24,.4}){
                var frame=new Capture(w,h);PersonaBattleDesign.paint(frame,new PersonaBattleDesign.View("Thanatos",skills(),0,120,120,45,false,age),w,h);
                var r=PersonaBattleDesign.row(w,h,0,age);int cx=r.x()+r.w()/2,cy=r.y()+r.h()/2;
                boolean drawn=false;for(Object op:frame.ops){var m=(Map<?,?>)op;if("fill".equals(m.get("type"))&&((Number)m.get("color")).intValue()==0xFFF7FFFF){
                    int x=((Number)m.get("x")).intValue(),y=((Number)m.get("y")).intValue(),ww=((Number)m.get("w")).intValue(),hh=((Number)m.get("h")).intValue();
                    if(cx>=x&&cx<x+ww&&cy>=y&&cy<y+hh)drawn=true;
                }}check(drawn,"animated hit box follows its selected row");
            }
            for(int i=0;i<7;i++){var r=PersonaBattleDesign.row(w,h,i);check(r.x()>=0&&r.x()+r.w()<=w&&r.y()>=0&&r.y()+r.h()<h-21,"skill row fits screen");check(r.contains(r.x()+r.w()/2.0,r.y()+r.h()/2.0),"visible row clickable");}
            check(PersonaBattleDesign.previous(w,h).x()+PersonaBattleDesign.previous(w,h).w()<PersonaBattleDesign.next(w,h).x(),"persona navigation distinct");
            for(int index=0;index<13;index++){var element=Element.values()[index];var icon=new Capture(w,h);BattleTargetHud.paintMarker(icon,80,60,.8,.65,.8,element,Affinity.WEAK);check(icon.fills>0,"element icon drawn "+element);}
        }
    }
    private static void previews()throws Exception{
        Files.createDirectories(Path.of("build/reports"));
        for(int count:new int[]{2,7}){
            var c=new Capture(1280,720);PersonaBattleDesign.paint(c,new PersonaBattleDesign.View("Thanatos",skills().subList(0,count),0,120,120,45,false,1),1280,720);
            Files.writeString(Path.of("build/reports/persona-selector-"+count+".json"),new com.google.gson.Gson().toJson(c.ops));
        }
        for(int ms:new int[]{0,60,140,240,340}){
            var c=new Capture(1280,720);PersonaBattleDesign.paint(c,new PersonaBattleDesign.View("Thanatos",skills().subList(0,2),0,120,120,45,false,ms/1000.0),1280,720);
            Files.writeString(Path.of("build/reports/selector-open-"+ms+".json"),new com.google.gson.Gson().toJson(c.ops));
        }
        var markers=new Capture(1000,280);for(int i=0;i<3;i++)BattleTargetHud.paintMarker(markers,80+i*320,120,2,.68,.83,i==2?Element.FIRE:Element.SLASH,i==0?null:i==1?Affinity.NORMAL:Affinity.WEAK);
        Files.writeString(Path.of("build/reports/target-markers.json"),new com.google.gson.Gson().toJson(markers.ops));
        for(int tick:new int[]{25,38,42,47}){
            var c=new Capture(1280,720);WeaknessCutinDesign.paintSummon(c,1280,720,tick);Files.writeString(Path.of("build/reports/shatter-"+tick+".json"),new com.google.gson.Gson().toJson(c.ops));
        }
    }
    private static void portraitGeometry()throws Exception{
        var setup=PersonaSelectorPortrait.class.getDeclaredMethod("model",boolean.class);setup.setAccessible(true);
        var rootField=PersonaSelectorPortrait.class.getDeclaredField("root");rootField.setAccessible(true);
        var armField=PersonaSelectorPortrait.class.getDeclaredField("arm");armField.setAccessible(true);
        var gunField=PersonaSelectorPortrait.class.getDeclaredField("gun");gunField.setAccessible(true);
        var meshField=EvokerHandLayer.class.getDeclaredField("gun");meshField.setAccessible(true);
        for(boolean slim:new boolean[]{false,true}){
            setup.invoke(null,slim);var root=(net.minecraft.client.model.geom.ModelPart)rootField.get(null);
            var geometry=new Geometry();
            var idleArm=root.m_171324_("right_arm");
            var idle=com.gx.velveteclipse.client.exploration.ExplorationPose.rotation("rightArm",0,0,-1);
            idleArm.m_171327_(idle.getX(),idle.getY(),idle.getZ());
            var idlePose=new com.mojang.blaze3d.vertex.PoseStack();
            EvokerHandLayer.bindHand(idlePose,idleArm,com.gx.velveteclipse.client.exploration.ExplorationPose.bend("rightArm",0,0,-1),slim);
            com.gx.velveteclipse.client.exploration.ExplorationSwordLayer.gripCombat(idlePose);
            var tip=new org.joml.Vector3f(0,1.36f,0).mulPosition(idlePose.m_85850_().m_252922_());
            check(tip.z<-.4f&&tip.x<-.4f&&tip.y<1.5f,"idle blade points forward/outward and clears floor: "+tip+" slim="+slim);
            org.joml.Vector3f previous=null;
            for(double time=0;time<=44;time+=.1){var pose=new com.mojang.blaze3d.vertex.PoseStack();com.gx.velveteclipse.client.exploration.ExplorationSwordLayer.gripBattle(pose,time);
                var direction=new org.joml.Vector3f(0,1.36f,0).mulPosition(pose.m_85850_().m_252922_());if(previous!=null)check(direction.distance(previous)<.065f,"continuous battle grip");previous=direction;}
            for(String name:List.of("head","hat","body","right_arm","left_arm","right_leg","left_leg","left_sleeve","right_sleeve","left_pants","right_pants","jacket"))check(root.m_171324_(name)!=null,"player mesh layer exists "+name);
            root.m_171324_("head").m_171327_(.17f,-.07f,-.05f);root.m_171324_("left_arm").m_171327_(-.08f,.03f,-.13f);
            for(String name:List.of("head","body","left_arm","right_leg","left_leg")){geometry.part=name;root.m_171324_(name).m_104301_(new com.mojang.blaze3d.vertex.PoseStack(),geometry,15728880,655360);}
            var solution=EvokerPose.arm(34,slim);var right=root.m_171324_("right_arm");right.m_171327_(solution.rotation().getX(),solution.rotation().getY(),solution.rotation().getZ());
            var stack=new com.mojang.blaze3d.vertex.PoseStack();right.m_104299_(stack);
            var mesh=(io.github.kosmx.bendylib.impl.BendableCuboid)armField.get(null);mesh.applyBend(solution.bend().getX(),solution.bend().getY());geometry.part="right_arm";mesh.render(stack.m_85850_(),geometry,1,1,1,1,15728880,655360);
            stack=new com.mojang.blaze3d.vertex.PoseStack();var orientation=EvokerHandLayer.bindHand(stack,right,solution.bend(),slim);stack.m_252781_(orientation.invert());stack.m_252781_(EvokerPose.weaponRotation(34));
            geometry.part="gun";((net.minecraft.client.model.geom.ModelPart)meshField.get(gunField.get(null))).m_104301_(stack,geometry,15728880,655360);
            Files.writeString(Path.of("build/reports/selector-geometry-"+(slim?"slim":"wide")+".json"),new com.google.gson.Gson().toJson(geometry.quads));
        }
    }
    private static class Geometry implements com.mojang.blaze3d.vertex.VertexConsumer {
        String part;List<Object> quads=new ArrayList<>();List<double[]> vertices=new ArrayList<>();double[] point;
        public com.mojang.blaze3d.vertex.VertexConsumer m_5483_(double x,double y,double z){point=new double[]{x,y,z,0,0};return this;}
        public com.mojang.blaze3d.vertex.VertexConsumer m_6122_(int a,int b,int c,int d){return this;}
        public com.mojang.blaze3d.vertex.VertexConsumer m_7421_(float u,float v){point[3]=u;point[4]=v;return this;}
        public com.mojang.blaze3d.vertex.VertexConsumer m_7122_(int a,int b){return this;}public com.mojang.blaze3d.vertex.VertexConsumer m_7120_(int a,int b){return this;}public com.mojang.blaze3d.vertex.VertexConsumer m_5601_(float a,float b,float c){return this;}
        public void m_5752_(){vertices.add(point);if(vertices.size()==4){quads.add(Map.of("part",part,"points",vertices));vertices=new ArrayList<>();}}
        public void m_7404_(int a,int b,int c,int d){}public void m_141991_(){}
    }
    private static class Capture implements WeaknessCutinDesign.Canvas {
        int portraits,fills,w,h;List<Object> ops=new ArrayList<>();Capture(int w,int h){this.w=w;this.h=h;}
        public void fill(int x,int y,int width,int height,int color){check(width>=0&&height>=0,"nonnegative drawing dimensions");fills++;ops.add(Map.of("type","fill","x",x,"y",y,"w",width,"h",height,"color",color));}
        public void text(String s,int x,int y,int color){styled(s,x,y,1,color);}
        public void styled(String s,int x,int y,float scale,int color){angled(s,x,y,scale,color,0);}
        public void angled(String s,int x,int y,float scale,int color,float degrees){ops.add(Map.of("type","text","text",s,"x",x,"y",y,"scale",scale,"color",color,"angle",degrees));}
        public int width(String s){return s.length()*6;}
        public void personaPortrait(int x,int y,int width,int height,float alpha){portraits++;ops.add(Map.of("type","portrait","x",x,"y",y,"w",width,"h",height,"alpha",alpha));}
        public void eyes(int x,int y,int width,int height,float alpha){}
        public void fragment(double[] xy,double[] uv,float alpha){check(xy.length==uv.length,"shard texture coordinates");ops.add(Map.of("type","fragment","xy",xy,"uv",uv,"alpha",alpha));}
    }
}
