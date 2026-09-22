package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.menu.*;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.client.ClientBattleState;
import com.gx.velveteclipse.VelvetEclipseMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;
import java.util.*;

public final class PersonaMenuScreen extends Screen {
    private static final ResourceLocation DISPLAY=new ResourceLocation("velvet_eclipse","display"),BODY=new ResourceLocation("velvet_eclipse","body");
    private MenuSnapshot data=MenuSnapshot.empty();
    private MenuPortrait portrait;
    private final PersonaSheetState sheet=new PersonaSheetState();
    private final PersonaModelPreview personaPreview=new PersonaModelPreview();
    private final BestiaryModelPreview bestiaryPreview=new BestiaryModelPreview();
    private boolean home=true,closing;
    private static boolean motion=true;
    private int tab,selected,offset,ticks;
    private double selection,homeAmount=1,started=now(),changed=now(),lastFrame=now(),closedAt;
    private double lastMouseX=-1,lastMouseY=-1;
    private String message="Sincronizando...";
    private double messageAt=now();
    private List<MenuDesign.Row> rows=List.of();
    private String action="";
    public PersonaMenuScreen(){super(Component.m_237113_("Velvet Eclipse"));}
    static double now(){return System.nanoTime()*1e-9;}
    @Override protected void m_7856_(){
        if(portrait==null&&f_96541_.f_91074_!=null)portrait=new MenuPortrait(f_96541_.f_91074_);
        request("refresh","");com.gx.velveteclipse.equipment.EquipmentNetwork.request("sync","","");
    }
    public static void accept(String json){
        var mc=Minecraft.m_91087_();if(!(mc.f_91080_ instanceof PersonaMenuScreen screen))return;
        try{
            var next=MenuNetwork.JSON.fromJson(json,MenuSnapshot.class);
            if(next==null||next.personas()==null||next.quests()==null||next.links()==null)return;
            String focused=screen.tab==3&&screen.selected<screen.data.personas().size()?screen.data.personas().get(screen.selected).id():"";
            screen.data=next;
            if(screen.tab==3)for(int i=0;i<next.personas().size();i++)if(next.personas().get(i).id().equals(focused)){screen.selected=i;break;}
            screen.sheet.reconcile(next.personas());
            if(!next.message().isBlank()){screen.message=next.message();screen.messageAt=now();}
            else if(screen.message.equals("Sincronizando..."))screen.message="";
        }catch(RuntimeException e){VelvetEclipseMod.LOGGER.error("Menu snapshot could not be decoded",e);screen.message="Falha ao sincronizar o menu.";}
    }
    private void request(String action,String arg){try{MenuNetwork.request(action,arg);}catch(RuntimeException e){message="Servidor sem suporte ao menu.";messageAt=now();}}
    @Override public boolean m_7043_(){return false;}
    @Override public void m_86600_(){
        if(f_96541_.f_91074_==null||ClientBattleState.snapshot().active()){f_96541_.m_91152_(null);return;}
        if(portrait!=null)portrait.advance(motion);
        if(++ticks%60==0)request("refresh","");
        if(closing&&now()-closedAt>.23)f_96541_.m_91152_(null);
    }
    @Override public void m_7379_(){if(sheet.open()){sheet.close();changed=now();}else if(!home){home=true;changed=now();}else closeMenu();}
    private void closeMenu(){if(!motion){f_96541_.m_91152_(null);return;}if(!closing){closing=true;closedAt=now();}}
    private double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}
    private double left(){return (f_96543_-900*scale())/2;}
    private double top(){return (f_96544_-500*scale())/2;}
    private void selectTab(int index,boolean enter){sheet.close();tab=Math.floorMod(index,MenuDesign.TABS.length);selected=offset=0;if(enter)home=false;changed=now();}
    private void showPersona(){if(sheet.show(data.personas(),selected))changed=now();}
    private void cyclePersona(int delta){sheet.cycle(data.personas(),delta);selected=Math.max(0,sheet.index(data.personas()));adjustScroll();changed=now();}
    private void equipInspected(){var p=sheet.persona(data.personas());if(p!=null)request("equip",p.id());}
    @Override public boolean m_7933_(int key,int scan,int modifiers){
        if(closing)return true;
        if(key==258||MenuInput.OPEN.m_90832_(key,scan)){closeMenu();return true;}
        if(key==256||key==259){m_7379_();return true;}
        if(sheet.open()){
            var p=sheet.persona(data.personas());if(p==null){sheet.close();return true;}
            if(key==88){m_7379_();return true;}
            if(key==257||key==335){equipInspected();return true;}
            if(key==81||key==69){cyclePersona(key==69?1:-1);return true;}
            if(key==65||key==68){personaPreview.rotate(key==68?1:-1);return true;}
            if(key==70){sheet.toggleAffinity();return true;}
            if(key==262||key==263){sheet.move(p,key==262?1:-1,0);return true;}
            if(key==264||key==265){sheet.move(p,0,key==264?1:-1);return true;}
            if(key==266||key==267){sheet.step(p,key==267?8:-8);return true;}
            return true;
        }
        if(key==88&&!home&&tab==3){showPersona();return true;}
        if(key==257||key==335||key==32){if(home){home=false;changed=now();}else activate();return true;}
        if(key==263||key==262){selectTab(tab+(key==262?1:-1),!home);return true;}
        if(key==265||key==264){if(home)selectTab(tab+(key==264?1:-1),false);else moveRow(key==264?1:-1);return true;}
        if(key==266||key==267){moveRow(key==267?8:-8);return true;}
        return super.m_7933_(key,scan,modifiers);
    }
    private void moveRow(int delta){if(rows.isEmpty())return;selected=Math.floorMod(selected+delta,rows.size());adjustScroll();}
    private void adjustScroll(){selected=Math.max(0,Math.min(selected,rows.size()-1));if(selected<offset)offset=selected;if(selected>=offset+8)offset=selected-7;}
    @Override public boolean m_6050_(double mx,double my,double delta){if(sheet.open()){var p=sheet.persona(data.personas());if(p!=null)sheet.step(p,delta<0?1:-1);}else if(home)selectTab(tab+(delta<0?1:-1),false);else moveRow(delta<0?1:-1);return true;}
    @Override public boolean m_6375_(double mx,double my,int button){
        if(closing)return true;
        if(button==1){m_7379_();return true;}if(button!=0)return false;
        double x=(mx-left())/scale(),y=(my-top())/scale();
        if(sheet.open()){
            if(PersonaSheetDesign.back().contains(x,y)){m_7379_();return true;}
            if(PersonaSheetDesign.previous().contains(x,y)){cyclePersona(-1);return true;}
            if(PersonaSheetDesign.next().contains(x,y)){cyclePersona(1);return true;}
            if(PersonaSheetDesign.equip().contains(x,y)){equipInspected();return true;}
            var p=sheet.persona(data.personas());int slot=PersonaSheetDesign.pickSkill(x,y),element=PersonaSheetDesign.pickAffinity(x,y);
            if(p!=null&&slot>=0)sheet.selectSkill(p,sheet.page()*8+slot);
            if(element>=0)sheet.selectAffinity(element);
            return true;
        }
        int pick=MenuDesign.pickTab(x,y,home);
        if(pick>=0){selectTab(pick,true);return true;}
        if(!home){if(MenuDesign.back().contains(x,y)){m_7379_();return true;}
            if(tab==3&&PersonaSheetDesign.details().contains(x,y)){showPersona();return true;}
            if(MenuDesign.action().contains(x,y)){activate();return true;}
            int row=MenuDesign.pickRow(x,y);if(row>=0&&row+offset<rows.size()){selected=row+offset;return true;}}
        return true;
    }
    private ItemStack item(int slot){return f_96541_.f_91074_.m_150109_().m_8020_(slot);}
    private MenuDesign.View view(double seconds){
        var player=f_96541_.f_91074_;var current=data.equipped();
        List<MenuDesign.Row> list=new ArrayList<>();List<String> detail=new ArrayList<>();int[] stats=null;String title=MenuDesign.HELP[tab];action="";
        switch(tab){
            case 0 -> {
                if(current!=null)for(var skill:current.skills())list.add(new MenuDesign.Row(skill.displayName(),skill.element()+"   "+cost(skill),-1));
                if(!list.isEmpty()){var skill=current.skills().get(Math.min(selected,list.size()-1));detail.add(skill.displayName());detail.add(skill.element()+" / "+cost(skill));detail.add("Poder "+skill.power()+" | Precisao "+skill.accuracy()+"%");
                    detail.add("SP: "+data.sp()+" / "+data.maxSp());
                    if(skill.element()==Element.HEALING){action="Usar habilidade de cura";detail.add("Recupera "+Math.max(1,skill.power())+" HP fora do combate.");}else detail.add("Use esta habilidade no menu de combate.");}
                else detail.add("As habilidades da Persona equipada aparecerao aqui.");
            }
            case 1 -> {
                list.add(new MenuDesign.Row("Pulse Tonic","x"+data.tonics()+"   Recupera 30% HP",-1));
                list.add(new MenuDesign.Row("Suprimentos da Elizabeth","HP / SP / Estados negativos",-2));
                for(int i=0;i<36;i++)if(!item(i).m_41619_())list.add(new MenuDesign.Row(item(i).m_41786_().getString(),"x"+item(i).m_41613_()+"   Inventario",i));
                if(selected==0){detail.add("PULSE TONIC");detail.add("Recupera 30% do HP de batalha.");detail.add("HP "+data.hp()+" / "+data.maxHp());detail.add("Quantidade: "+data.tonics());action="Usar Pulse Tonic";}
                else if(selected==1){detail.add("SUPRIMENTOS");detail.add("Veja os consumíveis comprados com yen.");action="Abrir coleção de itens";}
                else{var row=list.get(Math.min(selected,list.size()-1));detail.add(row.title());detail.add("Use, organize ou mova este item no inventario.");action="Abrir inventario";}
            }
            case 2 -> {
                var bag=com.gx.velveteclipse.client.equipment.EquipmentScreen.bag();
                for(var slot:com.gx.velveteclipse.equipment.EquipmentCatalog.Slot.values()){
                    var gear=com.gx.velveteclipse.equipment.EquipmentCatalog.get(bag.equipped.get(slot));
                    list.add(new MenuDesign.Row(com.gx.velveteclipse.equipment.EquipmentCatalog.label(slot),gear==null?"Vazio":gear.name(),-1));
                }
                detail.add("EQUIPAMENTOS");detail.add("Arma, armadura, calçado e acessório.");detail.add("Selecione uma peça para comparar seus atributos e equipá-la.");detail.add("A aparência da roupa permanece a sua. H utiliza a categoria de arma equipada.");action="Gerenciar equipamentos";
            }
            case 3 -> {
                for(var p:data.personas())list.add(new MenuDesign.Row(p.name(),"Lv "+p.level()+"  "+p.arcana()+(data.reserveIds().contains(p.id())?" / RESERVA":p.id().equals(data.active())?" / ATIVA":""),-1));
                if(!list.isEmpty()){var p=data.personas().get(Math.min(selected,list.size()-1));detail.add(p.name()+" / "+PersonaDetails.arcana(p.arcana()));detail.add("Lv "+p.level()+"   Próximo nível: "+PersonaDetails.remainingXp(p)+" EXP");
                    detail.add("Capacidade: "+(data.personas().size()-data.reserveIds().size())+" / "+data.personaCapacity()+" (máximo 8)");
                    detail.add(data.reserveIds().contains(p.id())?"Reserva do save anterior: ENTER troca pela equipada se não houver espaço.":"ENTER equipa. Contratos de Igor ampliam o limite.");detail.add("X abre modelo, atributos e habilidades.");
                    action=p.id().equals(data.active())?"Persona equipada":"Equipar Persona";}
                else detail.add("Você ainda não possui Personas. As Personas obtidas aparecerão nesta lista.");
            }
            case 4 -> {
                list.add(new MenuDesign.Row(player.m_36316_().getName(),"Jogador / Lv "+data.playerLevel(),-1));if(current!=null)list.add(new MenuDesign.Row(current.name(),"Persona / Atributos de batalha",-1));
                if(selected==0){detail.add(player.m_36316_().getName());detail.add("HP batalha: "+data.hp()+" / "+data.maxHp());detail.add("SP: "+data.sp()+" / "+data.maxSp());detail.add("Vida Minecraft: "+Math.round(player.m_21223_())+" / "+Math.round(player.m_21233_()));detail.add("Armadura: "+player.m_21230_());detail.add("Nível do jogador: "+data.playerLevel()+" / EXP "+data.playerXp()+" de "+data.playerNextXp());detail.add("Nível Minecraft: "+player.f_36078_);}
                else if(current!=null){detail.add(current.name()+" / Lv "+current.level());stats=current.stats();detail.add("ST forca / MA magia / EN resistencia");detail.add("AG agilidade / LU sorte");}
            }
            case 5 -> {
                for(var q:data.quests())list.add(new MenuDesign.Row(q.name(),q.done()?"CONCLUIDO":"Em progresso / "+Math.round(q.progress()*100)+"%",-1));
                if(!list.isEmpty()){var q=data.quests().get(Math.min(selected,list.size()-1));detail.add(q.name());detail.add(q.description());detail.add(q.done()?"Avanco concluido.":"Progresso: "+Math.round(q.progress()*100)+"%");}
                else detail.add("Os avancos disponiveis neste mundo aparecerao aqui.");action="Ver arvore de avancos";
            }
            case 6 -> {
                for(var link:data.links())list.add(new MenuDesign.Row(link.name()+" #"+link.id().substring(0,4),"Rank "+link.rank()+" / 10   Ultimo encontro: dia "+link.lastDay(),-1));
                if(!list.isEmpty()){var link=data.links().get(Math.min(selected,list.size()-1));detail.add(link.name());detail.add("VINCULO DA VILA / Rank "+link.rank()+" de 10");detail.add("Ultimo encontro: dia "+link.lastDay());}
                else detail.add("Converse com um aldeao para iniciar um vinculo.");
                detail.add("Um encontro por dia do mundo aumenta o rank, ate 10.");detail.add("Vinculos adaptados aos aldeoes do Minecraft.");
            }
            case 7 -> {
                list.add(new MenuDesign.Row("Hoje","Dia "+data.day(),-1));list.add(new MenuDesign.Row("Fase da lua","Ciclo "+Math.floorMod(data.day()-1,8)+" / 7",-1));
                list.add(new MenuDesign.Row("Vinculos","Reencontre seus aldeoes a cada dia",-1));list.add(new MenuDesign.Row("Calendario do mundo","Cada pagina mostra 28 dias",-1));
            }
            case 10 -> {list.add(new MenuDesign.Row("Minha party","Criar, convidar e compartilhar",-1));detail.add("PARTY");detail.add("Convide jogadores do servidor, envie itens e yen e compartilhe a exploração do mapa.");action="Gerenciar party";}
            case 9 -> {
                for(var e:data.bestiary())list.add(new MenuDesign.Row(e.name(),e.defeated()>0?"DERROTADO / "+e.defeated()+" vezes":"VISTO / Ainda não derrotado",-1));
                if(list.isEmpty())detail.add("Encontre inimigos em batalha para registrá-los aqui. Teste os elementos para descobrir suas afinidades.");
            }
            case 8 -> {
                list.add(new MenuDesign.Row("Opcoes do Minecraft","Video, audio, idioma e controles",-1));list.add(new MenuDesign.Row("Animacoes do menu",motion?"Ativadas":"Movimento reduzido",-1));list.add(new MenuDesign.Row("Voltar ao jogo","Fechar menu",-1));
                if(selected==0){detail.add("SYSTEM");detail.add("Ajuste video, audio, controles e idioma nas opcoes do Minecraft.");action="Abrir opcoes";}
                else if(selected==1){detail.add("Movimento do menu");detail.add("Alterna flutuacao do retrato, entrada das opcoes e transicoes.");detail.add("Preferencia valida nesta sessao.");action=motion?"Reduzir movimento":"Ativar animacoes";}
                else{detail.add("Retornar ao mundo.");action="Fechar menu";}
            }
        }
        rows=list;adjustScroll();
        return new MenuDesign.View(home,tab,selected,offset,rows,title,detail,stats,action,seconds-messageAt<5?message:"",player.m_36316_().getName(),data.yen(),data.day(),data.time(),seconds,motion,selection,seconds-started,seconds-changed);
    }
    private static String cost(SkillDefinition s){return s.spCost()>0?s.spCost()+" SP":s.hpPercentCost()>0?s.hpPercentCost()+"% HP":"Sem custo";}
    private void activate(){
        if(action.isEmpty())return;
        switch(tab){
            case 0 -> {var p=data.equipped();if(p!=null&&selected<p.skills().size()&&p.skills().get(selected).element()==Element.HEALING)request("heal",p.skills().get(selected).id());}
            case 1 -> {if(selected==0)request("tonic","");else if(selected==1)com.gx.velveteclipse.equipment.EquipmentNetwork.request("open","items","");else f_96541_.m_91152_(new InventoryScreen(f_96541_.f_91074_));}
            case 2 -> com.gx.velveteclipse.equipment.EquipmentNetwork.request("open","","");
            case 10 -> com.gx.velveteclipse.party.PartyNetwork.request("open","","",0,"");
            case 3 -> {if(selected<data.personas().size())request("equip",data.personas().get(selected).id());}
            case 5 -> {var connection=f_96541_.m_91403_();if(connection!=null)f_96541_.m_91152_(new AdvancementsScreen(connection.m_105145_()));}
            case 8 -> {if(selected==0)f_96541_.m_91152_(new OptionsScreen(this,f_96541_.f_91066_));else if(selected==1)motion=!motion;else closeMenu();}
        }
    }
    @Override public void m_88315_(GuiGraphics g,int mouseX,int mouseY,float partial){
        if(f_96541_.f_91074_==null)return;
        double seconds=now(),dt=seconds-lastFrame;lastFrame=seconds;
        selection=motion?MenuMotion.approach(selection,tab,dt):tab;homeAmount=motion?MenuMotion.approach(homeAmount,home?1:0,dt):(home?1:0);
        double mx=(mouseX-left())/scale(),my=(mouseY-top())/scale();
        personaPreview.advance(seconds,motion);
        if(sheet.open()){
            var p=sheet.persona(data.personas());
            if(p!=null){
                if(Math.abs(mouseX-lastMouseX)+Math.abs(mouseY-lastMouseY)>1){
                    int slot=PersonaSheetDesign.pickSkill(mx,my),element=PersonaSheetDesign.pickAffinity(mx,my);
                    if(slot>=0)sheet.selectSkill(p,sheet.page()*8+slot);else if(element>=0)sheet.selectAffinity(element);
                }
                lastMouseX=mouseX;lastMouseY=mouseY;
                var v=new PersonaSheetDesign.View(p,p.id().equals(data.active()),sheet.index(data.personas()),data.personas().size(),sheet.skill(),sheet.affinity(),seconds-changed,seconds,motion,seconds-messageAt<5?message:"",personaPreview.error(p.id()));
                drawSheet(g,v);return;
            }
            sheet.close();
        }
        if(home&&(Math.abs(mouseX-lastMouseX)+Math.abs(mouseY-lastMouseY)>1)){int pick=MenuDesign.pickTab(mx,my,true);if(pick>=0)tab=pick;}
        lastMouseX=mouseX;lastMouseY=mouseY;
        MenuDesign.View view=view(seconds);var pose=g.m_280168_();g.m_280509_(0,0,f_96543_,f_96544_,0xFF03143D);
        pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);
        var canvas=new GameCanvas(g);
        try{
            g.m_286007_(() -> MenuDesign.background(canvas,view));
            if(portrait!=null&&homeAmount>.02){
                pose.m_85836_();pose.m_85837_(0,0,MenuDepth.MODEL);
                try{portrait.draw(g,homeAmount,seconds-started,seconds,motion);}finally{pose.m_85849_();}
            }
            pose.m_85837_(0,0,MenuDepth.FOREGROUND);
            g.m_286007_(() -> MenuDesign.foreground(canvas,view));
            if(!home&&tab==9&&!data.bestiary().isEmpty()){
                var enemy=data.bestiary().get(Math.min(selected,data.bestiary().size()-1));
                g.m_286007_(() -> BestiaryDesign.paint(canvas,enemy));
                g.m_280588_((int)(left()+552*scale()),(int)(top()+195*scale()),(int)Math.ceil(left()+850*scale()),(int)Math.ceil(top()+303*scale()));
                try{bestiaryPreview.draw(g,enemy,seconds);}finally{g.m_280618_();}
            }
            if(!home&&tab==3&&!data.personas().isEmpty())g.m_286007_(() -> PersonaSheetDesign.rosterHint(canvas));
            if(motion&&seconds-changed<.28&&!home){double p=MenuMotion.wipe(seconds-changed);double x=210+p*720;
                canvas.poly(new double[]{x-175,40,x+100,40,x-45,445,x-210,445},MenuDesign.alpha(MenuDesign.CYAN,1-p));}
            if(closing){double p=MenuMotion.ease((seconds-closedAt)/.23);canvas.poly(new double[]{-300,0,1100*p,0,1100*p-180,500,-300,500},MenuDesign.INK);}
        }finally{g.m_280262_();pose.m_85849_();}
    }
    private void drawSheet(GuiGraphics g,PersonaSheetDesign.View v){
        var pose=g.m_280168_();g.m_280509_(0,0,f_96543_,f_96544_,0xFF020A2A);pose.m_85836_();
        pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);var canvas=new GameCanvas(g);
        try{
            g.m_286007_(() -> PersonaSheetDesign.background(canvas,v));
            // GUI scissor uses window GUI coordinates, independently of the current pose transform.
            g.m_280588_((int)(left()+485*scale()),(int)top(),(int)Math.ceil(left()+900*scale()),(int)Math.ceil(top()+474*scale()));
            try{personaPreview.draw(g,v);}finally{g.m_280618_();}
            pose.m_85837_(0,0,MenuDepth.FOREGROUND);g.m_286007_(() -> PersonaSheetDesign.foreground(canvas,v));
            if(closing){double p=MenuMotion.ease((now()-closedAt)/.23);canvas.poly(new double[]{-300,0,1100*p,0,1100*p-180,500,-300,500},MenuDesign.INK);}
        }finally{g.m_280262_();pose.m_85849_();}
    }
    private final class GameCanvas implements MenuDesign.Canvas {
        private final GuiGraphics g;GameCanvas(GuiGraphics g){this.g=g;}
        private Component styled(String text,double size){return Component.m_237113_(text).m_130938_(s->s.m_131150_(size>=20?DISPLAY:BODY));}
        public void fill(double x,double y,double w,double h,int color){if(w>0&&h>0&&(color>>>24)>0)g.m_280509_((int)x,(int)y,(int)Math.ceil(x+w),(int)Math.ceil(y+h),color);}
        public void poly(double[] points,int color){
            if((color>>>24)==0)return;
            var buffer=g.m_280091_().m_6299_(net.minecraft.client.renderer.RenderType.m_285907_());
            var matrix=g.m_280168_().m_85850_().m_252922_();
            MenuGeometry.draw(buffer,matrix,points,color);
        }
        public void text(String text,double x,double y,double size,int color,double angle){
            if((color>>>24)<5)return;var p=g.m_280168_();p.m_85836_();p.m_85837_(x,y,0);p.m_252781_(new Quaternionf().rotationZ((float)Math.toRadians(angle)));p.m_85841_((float)(size/11),(float)(size/11),1);
            g.m_280614_(f_96547_,styled(text,size),0,0,color,false);p.m_85849_();
        }
        public double width(String text,double size){return f_96547_.m_92852_(styled(text,size))*size/11;}
        public void item(int slot,double x,double y){if(slot>=0&&slot<41)g.m_280480_(PersonaMenuScreen.this.item(slot),(int)x,(int)y);}
    }
}
