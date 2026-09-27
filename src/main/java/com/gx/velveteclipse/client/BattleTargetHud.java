package com.gx.velveteclipse.client;

import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.network.*;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector4f;
import java.util.*;

@Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
public final class BattleTargetHud {
    private static List<BattleRosterS2C.Target> roster=List.of();
    private static UUID selected;
    private static final Map<UUID,BattleMenuLayout.Rect> boxes=new LinkedHashMap<>(),cards=new LinkedHashMap<>();
    private static final Map<UUID,double[]> positions=new HashMap<>();
    private static double lastX=-1,lastY=-1;
    public static int entityId(){return roster.stream().filter(t->t.id().equals(selected)).mapToInt(BattleRosterS2C.Target::entityId).findFirst().orElse(-1);}
    public static List<BattleRosterS2C.Target> roster(){return roster;}
    public static UUID selectedId(){return selected;}
    public static void lockActionTarget(UUID id){if(id!=null)selected=id;}
    public static double[] projected(UUID id){return positions.get(id);}
    public static void reset(){roster=List.of();selected=null;boxes.clear();positions.clear();cards.clear();com.gx.velveteclipse.client.presentation.BattleHitNumbers.reset();com.gx.velveteclipse.client.presentation.ReloadBattleHud.reset();}
    public static void accept(BattleTargetS2C ignored){}
    public static void accept(BattleRosterS2C p){
        // Defensive normalization of the authoritative roster.
        var seen=new HashSet<UUID>();roster=p.targets().stream().filter(t->t!=null&&seen.add(t.id())).toList();
        com.gx.velveteclipse.client.presentation.BattleHitNumbers.accept(roster);
        if(roster.isEmpty()){reset();return;}if(ClientBattleUiState.page()==ClientBattleUiState.Page.TARGET&&candidates().stream().noneMatch(t->t.id().equals(selected)))chooseDefault();
    }
    public static List<BattleRosterS2C.Target> candidates(){
        if(ClientBattleUiState.pendingSkill().equals("__baton_pass"))return roster.stream().filter(t->t.ally()&&t.hp()>0&&BattleTacticsClient.passTargets().contains(t.id())).toList();
        var skill=PersonaRegistry.skill(ClientBattleUiState.pendingSkill()).orElse(null);
        boolean ally=ClientBattleUiState.pendingSkill().startsWith("__item:")&&com.gx.velveteclipse.equipment.DamageItems.get(ClientBattleUiState.pendingSkill().substring(7))==null||skill!=null&&(skill.target()==SkillTarget.ONE_ALLY||skill.target()==SkillTarget.ALL_ALLIES||skill.target()==SkillTarget.SELF);
        return roster.stream().filter(t->(com.gx.velveteclipse.core.battle.SkillEffects.revival(skill)?t.hp()==0:t.hp()>0)&&t.ally()==ally).filter(t->skill==null||skill.target()!=SkillTarget.SELF||t.id().equals(RuntimeBridge.playerUuid(Minecraft.m_91087_().f_91074_))).toList();
    }
    public static void chooseDefault(){var list=candidates();selected=list.isEmpty()?null:list.get(0).id();lastX=lastY=-1;}
    public static void move(int delta){var list=candidates();if(list.isEmpty()){selected=null;return;}int index=0;for(int i=0;i<list.size();i++)if(list.get(i).id().equals(selected))index=i;selected=list.get(Math.floorMod(index+delta,list.size())).id();}
    public static String indicator(Affinity affinity){return affinity==null?"?":affinity==Affinity.WEAK?"! WEAK":"";}
    private static UUID hitId(double x,double y){for(var t:candidates()){var card=cards.get(t.id());var box=boxes.get(t.id());if(card!=null&&card.contains(x,y)||box!=null&&box.contains(x,y))return t.id();}return null;}
    public static boolean hit(double x,double y){return hitId(x,y)!=null;}
    public static boolean click(double x,double y){UUID id=hitId(x,y);if(id==null)return false;selected=id;return true;}
    public static void hover(double x,double y){if(Math.abs(x-lastX)+Math.abs(y-lastY)<.3)return;lastX=x;lastY=y;UUID id=hitId(x,y);if(id!=null)selected=id;}
    @SubscribeEvent public static void project(RenderLevelStageEvent e){
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;positions.clear();boxes.clear();
        var mc=Minecraft.m_91087_();if(!ClientBattleState.snapshot().active()||mc.f_91073_==null)return;
        int w=RuntimeBridge.guiScaledWidth(),h=RuntimeBridge.guiScaledHeight();var camera=e.getCamera().m_90583_();
        var matrix=new org.joml.Matrix4f(e.getProjectionMatrix()).mul(e.getPoseStack().m_85850_().m_252922_());
        for(var t:roster){var entity=mc.f_91073_.m_6815_(t.entityId());if(entity==null||t.hp()<=0)continue;var p=entity.m_20318_(e.getPartialTick());
            var clip=matrix.transform(new Vector4f((float)(p.f_82479_-camera.f_82479_),(float)(p.f_82480_+entity.m_20206_()*.6-camera.f_82480_),(float)(p.f_82481_-camera.f_82481_),1));
            if(clip.w<=0)continue;double x=(clip.x/clip.w*.5+.5)*w,y=(.5-clip.y/clip.w*.5)*h;if(x<0||x>w||y<0||y>h)continue;
            double radius=Math.max(16,Math.min(85,h*.6/clip.w));positions.put(t.id(),new double[]{x,y});boxes.put(t.id(),new BattleMenuLayout.Rect((int)(x-radius),(int)(y-radius),(int)(radius*2),(int)(radius*2),"",0));
        }
    }
    public static void paint(HudDesign.Canvas c,int w,int h,double ignored,double ignoredTrail){
        cards.clear();if(ClientBattleUiState.selector())return;
        boolean targeting=ClientBattleUiState.page()==ClientBattleUiState.Page.TARGET;
        var snapshot=ClientBattleState.snapshot();
        // Only the player whose turn is active may open the target selector.
        // This is also a client-side guard for multiplayer packets arriving in
        // the same frame, so observers never render a second selector HUD.
        if(targeting&&!snapshot.playerTurn())return;
        boolean acting=PersonaCinematics.meleeActive()||PersonaCinematics.skillSequenceActive();
        if(AllOutPresentation.active()||PersonaCinematics.skillSequenceActive()&&PersonaCinematics.skillTicks()<68)return;
        var element=ClientBattleUiState.selectedElement();double u=Math.max(.6,Math.min(w/640.,h/360.));
        if(targeting){
            var list=candidates();int index=0;for(int i=0;i<list.size();i++)if(list.get(i).id().equals(selected))index=i;
            int first=index/5*5,n=Math.min(5,list.size()-first),cw=(int)Math.min(104*u,(w-32.)/Math.max(1,n)),x=(w-cw*n)/2,y=h-(int)(65*u);
            for(int i=0;i<n;i++){var t=list.get(first+i);int px=x+i*cw;boolean active=t.id().equals(selected);cards.put(t.id(),new BattleMenuLayout.Rect(px,y,cw-3,(int)(25*u),"",0));
                c.fill(px,y,cw-3,(int)(25*u),active?0xFFF4FFFF:0xED050C30);c.fill(px,y,cw-3,2,active?0xFFFF286B:0xFF48E8F2);
                String name=t.name();while(c.width(name)>cw-10&&name.length()>3)name=name.substring(0,name.length()-1);c.text(name,px+4,y+4,active?0xFF08102C:0xFF71F5FF);
                c.text(t.hp()+" / "+t.maxHp()+" HP",px+4,y+14,active?0xFF17344B:0xFFE4F9FF);
            }
            c.text("← → alvo   ENTER confirmar   ESC voltar",Math.max(8,(w-c.width("← → alvo   ENTER confirmar   ESC voltar"))/2),y-13,0xFF79F8FF);
        }
        // Resolve one marker before painting. Overlapping projected entities must
        // never produce several markers, nor override a locked action target.
        UUID marker=(targeting||acting)?selected:null;
        if(marker==null&&!acting&&!targeting&&snapshot.playerTurn())for(var t:roster){
            var box=boxes.get(t.id());if(!t.ally()&&t.hp()>0&&box!=null&&box.contains(RuntimeBridge.guiMouseX(),RuntimeBridge.guiMouseY())){marker=t.id();break;}
        }
        for(var t:roster){if(t.hp()<=0||!t.id().equals(marker))continue;if(!t.ally())EnemyAffinityHud.paint(c,t.known(),w,h,u,targeting);var at=positions.get(t.id());if(at==null)continue;
            if(targeting&&candidates().stream().noneMatch(a->a.id().equals(t.id())))continue;
            Affinity affinity=t.ally()?Affinity.NORMAL:null;for(String pair:t.known().split(",")){var a=pair.split(":");if(a.length==2&&a[0].equals(element.name()))try{affinity=Affinity.valueOf(a[1]);}catch(IllegalArgumentException ignoredError){}}
            int px=(int)Math.max(25*u,Math.min(w-110*u,at[0])),py=(int)Math.max(45*u,Math.min(h-90*u,at[1]));double ratio=t.hp()/(double)Math.max(1,t.maxHp());
            paintMarker(c,px,py,u,ratio,ratio,element,t.ally()||element==Element.SUPPORT?Affinity.NORMAL:affinity);
            c.text(t.name(),px-(int)(20*u),py+(int)(26*u),0xFFF4FFFF);
        }
        if(!targeting){var turn=roster.stream().filter(BattleRosterS2C.Target::turn).findFirst().orElse(null);if(turn!=null&&!ClientBattleState.snapshot().playerTurn())c.text("TURNO: "+turn.name(),12,12,0xFF66F5FF);}
    }
    public static void paintMarker(HudDesign.Canvas c,int x,int y,double u,double hp,double trail,Element element,Affinity affinity){
        double r=22*u;BattleGraphics.poly(c,new double[]{x,y-r-3,x+r+4,y,x,y+r+3,x-r-4,y},0xFFFF2655);
        BattleGraphics.poly(c,new double[]{x,y-r,x+r,y,x,y+r,x-r,y},0xFF010610);
        BattleGraphics.poly(c,new double[]{x,y-r+5,x+r-5,y,x,y+r-5,x-r+5,y},0xFF574016);
        BattleGraphics.icon(c,element,x,y,14*u);
        int bx=x+(int)(6*u),by=y+(int)(9*u),bw=(int)(81*u),bh=Math.max(4,(int)(8*u));
        BattleGraphics.poly(c,new double[]{bx-3,by-3,bx+bw+7,by-3,bx+bw+3,by+bh+3,bx-5,by+bh+3},0xFFFF2655);
        c.fill(bx-2,by-2,bw+5,bh+4,0xFF000510);c.fill(bx,by,bw,bh,0xFF173348);
        c.fill(bx,by,(int)(bw*Math.max(0,Math.min(1,trail))),bh,0xFFFFFFFF);c.fill(bx,by,(int)(bw*Math.max(0,Math.min(1,hp))),bh,0xFF4FF5ED);
        String label=indicator(affinity);if(!label.isEmpty()){
            int tx=x+(int)(9*u),ty=y-(int)(34*u);float scale=(float)(label.equals("?")?3.3*u:2.1*u);
            for(int dx=-2;dx<=2;dx+=2)for(int dy=-2;dy<=2;dy+=2)c.angled(label,tx+dx,ty+dy,scale,0xFF00040B,-12);
            c.angled(label,tx,ty,scale,0xFFFFFFFF,-12);
        }
    }
}
