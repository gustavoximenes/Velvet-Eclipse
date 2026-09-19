package com.gx.velveteclipse.client;

import java.util.ArrayList;
import java.util.List;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.network.BattleSnapshotS2C;
import com.gx.velveteclipse.util.RuntimeBridge;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ClientHudEvents {
    private static final net.minecraft.resources.ResourceLocation DISPLAY=new net.minecraft.resources.ResourceLocation("velvet_eclipse","display");
    private static long lastFrame;
    private static boolean wasActive;
    private static String lastEnemy="";
    private static double hp,sp,enemy,trail;
    private static final IGuiOverlay HUD=(forgeGui,graphics,partial,width,height)->render(graphics,partial,width,height);

    private static void render(GuiGraphics g,float partial,int w,int h) {
        BattleSnapshotS2C s=ClientBattleState.snapshot();
        Font font=(Font)RuntimeBridge.minecraftFont();
        if(s==null||font==null)return;
        long now=System.nanoTime();
        boolean first=lastFrame==0;
        double dt=first?0:Math.min(.1,(now-lastFrame)*1e-9);lastFrame=now;
        double th=ratio(s.hp(),s.maxHp()),ts=ratio(s.sp(),s.maxSp()),te=ratio(s.enemyHp(),s.enemyMaxHp());
        if(wasActive!=s.active()||!lastEnemy.equals(s.enemyName())||first){hp=th;sp=ts;enemy=te;trail=te;}
        hp=approach(hp,th,dt,12);sp=approach(sp,ts,dt,12);enemy=approach(enemy,te,dt,18);
        trail=te>=trail?te:approach(trail,te,dt,3);
        wasActive=s.active();lastEnemy=s.enemyName();
        List<String> keys=new ArrayList<>();for(int i=0;i<6;i++)keys.add(ClientKeyMappings.actionLabel(i));
        boolean skills=ClientBattleUiState.page()==ClientBattleUiState.Page.SKILLS;
        List<HudDesign.Entry> entries=new ArrayList<>();
        List<String> ids=skills?s.skills():s.roster();
        for(int i=0;i<Math.min(7,ids.size());i++) {
            String id=ids.get(i);int cost=skills?PersonaRegistry.skill(id).map(v->v.spCost()).orElse(0):0;
            entries.add(new HudDesign.Entry(ClientKeyMappings.optionLabel(i),skills?ClientBattleUiState.displaySkill(id):ClientBattleUiState.displayPersona(id),
                skills?(cost>0?cost+" SP":"--"):id.equals(s.personaId())?"ACTIVE":"",!skills||cost<=s.sp()));
        }
        HudDesign.View view=new HudDesign.View(s.active(),s.playerTurn(),ClientBattleUiState.actionLocked(),s.personaName(),s.hp(),s.maxHp(),s.sp(),s.maxSp(),
            s.enemyName(),s.enemyHp(),s.enemyMaxHp(),hp,sp,enemy,trail,s.message(),ClientBattleUiState.page().name(),entries,keys,ClientKeyMappings.label(ClientKeyMappings.BACK),
            now*1e-9,PersonaCinematics.skillSequenceActive(),PersonaCinematics.skillTicks()+partial,ClientBattleUiState.displaySkill(PersonaCinematics.activeSkill()));
        HudDesign.Canvas canvas=new HudDesign.Canvas(){
            public void fill(int x,int y,int width,int height,int color){if(width>0&&height>0&&(color>>>24)>0)g.m_280509_(x,y,x+width,y+height,color);}
            public void text(String text,int x,int y,int color){if((color>>>24)>3)g.m_280056_(font,text,x,y,color,false);}
            public int width(String text){return font.m_92895_(text);}
            public void styled(String text,int x,int y,float scale,int color){
                var pose=g.m_280168_();pose.m_85836_();pose.m_252880_(x,y,0);pose.m_85841_(scale,scale,1);
                var component=net.minecraft.network.chat.Component.m_237113_(text).m_130938_(style->style.m_131150_(DISPLAY));
                g.m_280614_(font,component,0,0,color,false);pose.m_85849_();
            }
            public void angled(String text,int x,int y,float scale,int color,float degrees){var pose=g.m_280168_();pose.m_85836_();pose.m_252880_(x,y,0);pose.m_252781_(new org.joml.Quaternionf().rotationZ((float)Math.toRadians(degrees)));styled(text,0,0,scale,color);pose.m_85849_();}
            public void personaPortrait(int x,int y,int width,int height,float alpha){PersonaSelectorPortrait.draw(g,x,y,width,height,alpha);}
            public void portrait(int x,int y,int size){com.gx.velveteclipse.client.exploration.PlayerFace.draw(g,x,y,size);}
            public String playerName(){var player=net.minecraft.client.Minecraft.m_91087_().f_91074_;return player==null?"PLAYER":player.m_36316_().getName();}
            public void playerModel(int width,int height,double time,float alpha){com.gx.velveteclipse.client.exploration.EntryPlayerRenderer.draw(g,width,height,time,alpha);}
        };
        ClientBattleUiState.hover(RuntimeBridge.guiMouseX(),RuntimeBridge.guiMouseY(),w,h);
        // GuiGraphics batches all strips and glyphs into its managed GUI buffer.
        g.m_286007_(() -> {
            if(com.gx.velveteclipse.client.darkhour.DarkHourClient.clock()&&!s.active()){
                com.gx.velveteclipse.client.darkhour.DarkHourClient.paint(canvas,w,h);return;
            }
            if(BattleTacticsClient.exclusiveHud()){
                if(AllOutPresentation.active()){g.m_280262_();AllOutPresentation.paint(g,canvas,w,h,partial);}
                else BattleTacticsClient.paint(canvas,w,h);
                return;
            }
            if(s.active()&&ClientBattleUiState.selector()){
                List<com.gx.velveteclipse.core.persona.SkillDefinition> choices=s.skills().stream().map(id->PersonaRegistry.skill(id).orElse(null)).filter(java.util.Objects::nonNull).toList();
                PersonaBattleDesign.paint(canvas,new PersonaBattleDesign.View(s.personaName(),choices,ClientBattleUiState.selected(),s.hp(),s.maxHp(),s.sp(),ClientBattleUiState.actionLocked(),ClientBattleUiState.age()),w,h);
            }else if(!com.gx.velveteclipse.client.exploration.ExplorationClient.entry())HudDesign.paint(canvas,view,w,h,RuntimeBridge.guiMouseX(),RuntimeBridge.guiMouseY());
            if(s.active())BattleTargetHud.paint(canvas,w,h,enemy,trail);
            if(s.active())BattleTacticsClient.paint(canvas,w,h);
            if(AllOutPresentation.active()){g.m_280262_();AllOutPresentation.paint(g,canvas,w,h,partial);}
            else if(!s.active())BattleEncounterClient.paint(canvas,w,h);
            if(com.gx.velveteclipse.client.exploration.EntryTransition.visible()){
                g.m_280262_();var pose=g.m_280168_();pose.m_85836_();pose.m_252880_(0,0,100);
                try{com.gx.velveteclipse.client.exploration.EntryTransition.paint(canvas,w,h,partial);}
                finally{g.m_280262_();pose.m_85849_();}
            }
            else if(!s.active()&&com.gx.velveteclipse.client.exploration.ExplorationClient.swordVisible())
                HudDesign.explorationHints(canvas,w,h,ClientKeyMappings.label(com.gx.velveteclipse.client.exploration.ExplorationClient.TOGGLE));
        });
    }
    private static double ratio(int v,int max){return max<=0?0:Math.max(0,Math.min(1,v/(double)max));}
    private static double approach(double from,double to,double dt,double speed){return from+(to-from)*(1-Math.exp(-speed*dt));}

    @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.FORGE,value=Dist.CLIENT)
    public static final class InputHandler {
        @SubscribeEvent public static void hideOtherOverlays(net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre event){
            if((BattleTacticsClient.exclusiveHud()||com.gx.velveteclipse.client.darkhour.DarkHourClient.clock()&&!ClientBattleState.snapshot().active())&&!event.getOverlay().id().toString().equals("velvet_eclipse:persona_hud"))event.setCanceled(true);
        }
        @SubscribeEvent public static void onScreen(net.minecraftforge.client.event.ScreenEvent.Opening event){
            if(AllOutPresentation.active()){event.setCanceled(true);return;}
            if(BattleTacticsClient.prompt()&&event.getNewScreen() instanceof net.minecraft.client.gui.screens.PauseScreen){BattleTacticsClient.dismiss();event.setCanceled(true);return;}
            if(ClientBattleState.snapshot().active()&&event.getNewScreen() instanceof net.minecraft.client.gui.screens.PauseScreen&&ClientBattleUiState.page()!=ClientBattleUiState.Page.MAIN){
                ClientBattleUiState.back();event.setCanceled(true);
            }
        }
        @SubscribeEvent public static void onMouse(InputEvent.MouseButton.Pre event) {
            BattleSnapshotS2C s=ClientBattleState.snapshot();if(s==null||!s.active()||net.minecraft.client.Minecraft.m_91087_().f_91080_!=null)return;
            if(event.getButton()==0||event.getButton()==1)event.setCanceled(true);
            if(event.getAction()!=1||AllOutPresentation.active()||BattleTacticsClient.awaitingAllOut())return;
            if(BattleTacticsClient.prompt()){if(event.getButton()==1)BattleTacticsClient.dismiss();else if(event.getButton()==0)BattleTacticsClient.click(RuntimeBridge.guiMouseX(),RuntimeBridge.guiMouseY(),RuntimeBridge.guiScaledWidth(),RuntimeBridge.guiScaledHeight());return;}
            if(event.getButton()==1){ClientBattleUiState.back();return;}
            if(event.getButton()==0&&BattleTacticsClient.click(RuntimeBridge.guiMouseX(),RuntimeBridge.guiMouseY(),RuntimeBridge.guiScaledWidth(),RuntimeBridge.guiScaledHeight()))return;
            if(event.getButton()==0)ClientBattleUiState.click(RuntimeBridge.guiMouseX(),RuntimeBridge.guiMouseY(),RuntimeBridge.guiScaledWidth(),RuntimeBridge.guiScaledHeight(),s);
        }
    }
    @Mod.EventBusSubscriber(modid="velvet_eclipse",bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static final class OverlayRegistration {
        @SubscribeEvent public static void register(RegisterGuiOverlaysEvent event){event.registerAboveAll("persona_hud",HUD);}
    }
}
