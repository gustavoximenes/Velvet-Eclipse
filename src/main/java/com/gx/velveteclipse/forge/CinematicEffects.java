package com.gx.velveteclipse.forge;

import com.gx.velveteclipse.util.RuntimeBridge;
import com.gx.velveteclipse.VelvetEclipseMod;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.core.persona.Element;
import com.gx.velveteclipse.core.persona.SkillTarget;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;
import java.util.Locale;

/** Cosmetic only: bounded particles, sent to the participant in this battle. */
public final class CinematicEffects {
    private static final DustParticleOptions CYAN=dust(.18f,.83f,1f,.8f);
    private static final DustParticleOptions WHITE=dust(.8f,.96f,1f,.65f);
    private static boolean reported;
    private static DustParticleOptions dust(float r,float g,float b,float size){return new DustParticleOptions(new Vector3f(r,g,b),size);}
    public static void tick(Object actor,int tick,String skill,boolean magic,double[] base,float yaw) {
        if(!(actor instanceof ServerPlayer player)||!(player.m_9236_() instanceof ServerLevel level)||base==null)return;
        try {
            double[] origin=RuntimeBridge.localOffset(base,yaw,0,.7,1.95);
            String id=skill==null?"":skill.toLowerCase(Locale.ROOT);
            var definition=PersonaRegistry.skill(id).orElse(null);
            Element element=definition==null?Element.ALMIGHTY:definition.element();
            boolean heal=definition!=null&&(definition.target()==SkillTarget.SELF||definition.target()==SkillTarget.ONE_ALLY||definition.target()==SkillTarget.ALL_ALLIES);
            Object enemy=VisualBattleActors.enemyActor(player);
            double[] target=heal?RuntimeBridge.localOffset(base,yaw,0,.8,0):enemy instanceof Entity e?new double[]{e.m_20185_(),e.m_20186_()+.8,e.m_20189_()}:RuntimeBridge.localOffset(base,yaw,0,1,5);
            DustParticleOptions color=color(element);
            if(tick==38) {
                double[] muzzle=RuntimeBridge.localOffset(base,yaw,.25,1.55,0);
                burst(level,player,muzzle,WHITE,12,.28);
            }
            if(tick==46||tick==52)ring(level,player,origin,CYAN,tick==46?.65:1,24);
            if(tick>=46&&tick<=66&&tick%2==0) {
                double a=(tick-46)*.42;
                for(int i=0;i<3;i++)point(level,player,CYAN,origin[0]+Math.cos(a+i*2.094)*.65,origin[1]+(tick-46)*.06,origin[2]+Math.sin(a+i*2.094)*.65);
            }
            if(tick>=70&&tick<=85) {
                double p=(tick-70)/15.0;
                if(magic&&element!=Element.ELECTRIC) {
                    for(int i=0;i<3;i++) {
                        double a=tick*.8+i*2.094;
                        point(level,player,color,mix(origin[0],target[0],p)+Math.cos(a)*.12,mix(origin[1]+.5,target[1],p)+Math.sin(p*Math.PI)*.6+Math.sin(a)*.12,mix(origin[2],target[2],p));
                    }
                } else if(tick>=75&&tick<=81) {
                    for(int i=0;i<6;i++) {
                        double a=-1.4+(tick-75)*.32+i*.06;
                        double[] pos=RuntimeBridge.localOffset(target,yaw,Math.cos(a)*.85,Math.sin(a)*.75,0);
                        point(level,player,WHITE,pos[0],pos[1],pos[2]);
                    }
                }
            }
            if(tick==86){
                burst(level,player,target,color,18,.4);
                if(element==Element.ELECTRIC){
                    burst(level,player,target,WHITE,28,.32);
                    burst(level,player,target,dust(1,.45f,.75f,.7f),18,.45);
                    ring(level,player,target,CYAN,1.1,24);
                }
                if(element==Element.SUPPORT){for(int i=0;i<3;i++){double[] p={target[0],target[1]+i*.28,target[2]};ring(level,player,p,color,.5+i*.1,18);}}
            }
            if(tick==102||tick==106) {
                ring(level,player,origin,CYAN,tick==102?.8:.4,16);
            }
            if(tick==108)burst(level,player,target,color,12,.25);
        } catch(RuntimeException e) {
            if(!reported){reported=true;VelvetEclipseMod.LOGGER.warn("Velvet Eclipse cosmetic particles unavailable",e);}
        }
    }
    public static void melee(ServerPlayer player){
        if(player.m_9236_() instanceof ServerLevel level && VisualBattleActors.enemyActor(player) instanceof Entity enemy){
            // Extend the slash effect when terrain shortened the safe approach.
            double gap=player.m_20182_().m_82554_(enemy.m_20182_());
            if(gap>2){for(int i=0;i<24;i++){double t=i/23.0;var p=player.m_20182_().m_165921_(enemy.m_20182_(),t);
                point(level,player,CYAN,p.f_82479_,p.f_82480_+.8+Math.sin(t*Math.PI)*.35,p.f_82481_);}}
            double[] pos={enemy.m_20185_(),enemy.m_20186_()+.65,enemy.m_20189_()};burst(level,player,pos,WHITE,22,.33);burst(level,player,pos,CYAN,12,.5);
            for(int i=0;i<25;i++){double a=-1.1+i*.095;double[] p=RuntimeBridge.localOffset(pos,player.m_146908_(),Math.cos(a)*1.1,Math.sin(a),0);point(level,player,CYAN,p[0],p[1],p[2]);}
        }
    }
    private static DustParticleOptions color(Element element) {
        return switch(element) {
            case HEALING,SUPPORT,WIND->dust(.35f,1,.65f,.8f);
            case FIRE->dust(1,.3f,.12f,.85f);
            case ELECTRIC->dust(.85f,.72f,1,.85f);
            case LIGHT->dust(1,.85f,.2f,.85f);
            case DARK,PSY,AILMENT->dust(.68f,.3f,1,.85f);
            case PHYSICAL->WHITE;
            default->CYAN;
        };
    }
    private static double mix(double a,double b,double t){return a+(b-a)*t;}
    private static void point(ServerLevel level,ServerPlayer player,DustParticleOptions color,double x,double y,double z){level.m_8624_(player,color,false,x,y,z,1,0,0,0,0);}
    private static void ring(ServerLevel level,ServerPlayer player,double[] p,DustParticleOptions color,double radius,int count){for(int i=0;i<count;i++){double a=i*Math.PI*2/count;point(level,player,color,p[0]+Math.cos(a)*radius,p[1]-.45,p[2]+Math.sin(a)*radius);}}
    private static void burst(ServerLevel level,ServerPlayer player,double[] p,DustParticleOptions color,int count,double spread){level.m_8624_(player,color,false,p[0],p[1],p[2],count,spread,spread,spread,0);}
}
