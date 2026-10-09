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
    private int actor,gearSlot,targetIndex;
    private boolean gearOpen,targetOpen;
    private String recoveryAction="",recoveryId="",recoveryTitle="";
    private final List<String> choiceIds=new ArrayList<>();
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
    private boolean allOutSelectOpen;
    private int allOutChoice;
    public PersonaMenuScreen(){super(Component.m_237113_("Velvet Eclipse"));}
    static double now(){return System.nanoTime()*1e-9;}
    @Override protected void m_7856_(){

        request("refresh","");com.gx.velveteclipse.equipment.EquipmentNetwork.request("sync","","");
    }
    public static void accept(String json){
        var mc=Minecraft.m_91087_();if(!(mc.f_91080_ instanceof PersonaMenuScreen screen))return;
        try{
            var next=MenuNetwork.JSON.fromJson(json,MenuSnapshot.class);
            if(next==null||next.personas()==null||next.quests()==null||next.links()==null)return;
            String focused=screen.tab==3&&screen.selected<screen.data.personas().size()?screen.data.personas().get(screen.selected).id():"";
            screen.data=next;screen.actor=Math.max(0,Math.min(screen.actor,next.members().size()-1));
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

        if(++ticks%60==0)request("refresh","");
        if(closing&&now()-closedAt>.23)f_96541_.m_91152_(null);
    }
    @Override public void m_7379_(){if(targetOpen){targetOpen=false;return;}if(gearOpen){gearOpen=false;selected=gearSlot;offset=0;return;}if(allOutSelectOpen){allOutSelectOpen=false;return;}if(sheet.open()){sheet.close();changed=now();}else if(!home){home=true;changed=now();}else closeMenu();}
    private boolean opening(){return home&&ReloadMenuOpening.active(now()-started,motion);}
    private void closeMenu(){if(!motion){f_96541_.m_91152_(null);return;}if(!closing){closing=true;closedAt=now();}}
    private double scale(){return Math.min(f_96543_/900.0,f_96544_/500.0);}
    private double left(){return (f_96543_-900*scale())/2;}
    private double top(){return (f_96544_-500*scale())/2;}
    private void selectTab(int index,boolean enter){allOutSelectOpen=gearOpen=targetOpen=false;sheet.close();tab=Math.floorMod(index,MenuDesign.TABS.length);selected=offset=0;actor=0;if(enter){home=false;if(tab==10){home=true;com.gx.velveteclipse.party.PartyNetwork.request("open","","",0,"");}}changed=now();}
    private void openAllOutSelect(){allOutChoice=com.gx.velveteclipse.core.battle.AllOutStyle.parse(data.allOutStyle()).ordinal();allOutSelectOpen=true;changed=now();}
    private void chooseAllOut(){request("all_out_style",com.gx.velveteclipse.core.battle.AllOutStyle.values()[allOutChoice].id());allOutSelectOpen=false;changed=now();}
    private void stepAllOut(int delta){allOutChoice=Math.floorMod(allOutChoice+delta,com.gx.velveteclipse.core.battle.AllOutStyle.values().length);changed=now();}
    public static void equipmentUpdated(String message){var mc=Minecraft.m_91087_();if(mc.f_91080_ instanceof PersonaMenuScreen s){if(!message.isBlank()){s.message=message;s.messageAt=now();}s.request("refresh","");}}
    private void recover(String action,String id,String title){if(data.members().isEmpty())return;recoveryAction=action;recoveryId=id;recoveryTitle=title;targetIndex=actor;targetOpen=true;}
    private void confirmTarget(){if(targetIndex>=data.members().size())return;request(recoveryAction,recoveryId+"|"+data.members().get(targetIndex).id());targetOpen=false;}
    private void cycleActor(int delta){if(data.members().isEmpty())return;actor=Math.floorMod(actor+delta,data.members().size());selected=offset=0;}
    private void showPersona(){if(sheet.show(data.personas(),selected))changed=now();}
    private void cyclePersona(int delta){sheet.cycle(data.personas(),delta);selected=Math.max(0,sheet.index(data.personas()));adjustScroll();changed=now();}
    private void equipInspected(){var p=sheet.persona(data.personas());if(p!=null)request("equip",p.id());}
    @Override public boolean m_7933_(int key,int scan,int modifiers){
        if(closing)return true;
        if(key==258||MenuInput.OPEN.m_90832_(key,scan)){closeMenu();return true;}
        if(key==256||key==259){m_7379_();return true;}
        if(opening())return true;
        if(targetOpen){if(key==265||key==264)targetIndex=Math.floorMod(targetIndex+(key==264?1:-1),Math.max(1,data.members().size()));else if(key==257||key==335)confirmTarget();return true;}
        if(!home&&!sheet.open()&&(key==81||key==69)&&(tab==0||tab==1||tab==4)){cycleActor(key==69?1:-1);return true;}
        if(allOutSelectOpen){
            if(key==265||key==263)stepAllOut(-1);else if(key==264||key==262)stepAllOut(1);
            else if(key==257||key==335||key==32)chooseAllOut();
            return true;
        }
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
        if(key==257||key==335||key==32){if(home){selectTab(tab,true);}else activate();return true;}
        if(key==263||key==262){if(home)selectTab(tab+(key==262?1:-1),false);else if(tab==0||tab==1||tab==4)cycleActor(key==262?1:-1);return true;}
        if(key==265||key==264){if(home)selectTab(tab+(key==264?1:-1),false);else moveRow(key==264?1:-1);return true;}
        if(key==266||key==267){moveRow(key==267?8:-8);return true;}
        return super.m_7933_(key,scan,modifiers);
    }
    private void moveRow(int delta){if(rows.isEmpty())return;selected=Math.floorMod(selected+delta,rows.size());adjustScroll();}
    private void adjustScroll(){selected=Math.max(0,Math.min(selected,rows.size()-1));int count=ReloadMenuDesign.visibleRows(tab,gearOpen);if(selected<offset)offset=selected;if(selected>=offset+count)offset=selected-count+1;}
    @Override public boolean m_6050_(double mx,double my,double delta){if(opening())return true;if(targetOpen){targetIndex=Math.floorMod(targetIndex+(delta<0?1:-1),Math.max(1,data.members().size()));return true;}if(allOutSelectOpen){stepAllOut(delta<0?1:-1);return true;}if(sheet.open()){var p=sheet.persona(data.personas());if(p!=null)sheet.step(p,delta<0?1:-1);}else if(home)selectTab(tab+(delta<0?1:-1),false);else moveRow(delta<0?1:-1);return true;}
    @Override public boolean m_6375_(double mx,double my,int button){
        if(closing)return true;
        if(button==1){m_7379_();return true;}if(button!=0)return false;if(opening())return true;
        double x=(mx-left())/scale(),y=(my-top())/scale();
        if(targetOpen){for(int i=0;i<4;i++)if(ReloadMenuDesign.rect(306,251+i*79,680,71).contains(x,y)&&targetIndex/4*4+i<data.members().size()){targetIndex=targetIndex/4*4+i;confirmTarget();return true;}return true;}
        if(allOutSelectOpen){int option=AllOutSelectDesign.pick(x,y);if(option>=0){allOutChoice=option;chooseAllOut();}else allOutSelectOpen=false;return true;}
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
        if(home){int pick=ReloadMenuDesign.pickHome(x,y);if(pick>=0)selectTab(pick,true);return true;}
        if(ReloadMenuDesign.back().contains(x,y)){m_7379_();return true;}
        if(!gearOpen&&(tab==0||tab==1||tab==4))for(int i=0;i<4;i++)if(ReloadMenuDesign.member(i).contains(x,y)&&actor/4*4+i<data.members().size()){actor=actor/4*4+i;selected=offset=0;return true;}
        if(ReloadMenuDesign.action().contains(x,y)&&tab>4){activate();return true;}
        int row=ReloadMenuDesign.pickRow(tab,gearOpen,x,y);if(row>=0&&row+offset<rows.size()){boolean again=selected==row+offset;selected=row+offset;if(again||tab==3||tab==2||tab==8)activate();return true;}
        return true;
    }
    private ItemStack item(int slot){return f_96541_.f_91074_.m_150109_().m_8020_(slot);}
    private MenuDesign.View view(double seconds){
        var player=f_96541_.f_91074_;var current=actor>0&&actor<data.members().size()?data.members().get(actor).persona():data.equipped();choiceIds.clear();
        List<MenuDesign.Row> list=new ArrayList<>();List<String> detail=new ArrayList<>();int[] stats=null;String title=MenuDesign.HELP[tab];action="";
        switch(tab){
            case 0 -> {
                if(current!=null)for(var skill:current.skills())list.add(new MenuDesign.Row(skill.displayName(),skill.element()+"   "+cost(skill),-1));
                if(!list.isEmpty()){var skill=current.skills().get(Math.min(selected,list.size()-1));detail.add(skill.displayName());detail.add(skill.element()+" / "+cost(skill));detail.add("Poder "+skill.power()+" | Precisao "+skill.accuracy()+"%");
                    detail.add("SP: "+data.sp()+" / "+data.maxSp());
                    if(skill.element()==Element.HEALING&&actor==0){action="Usar habilidade de cura";detail.add("Recupera "+Math.max(1,skill.power())+" HP fora do combate.");}else detail.add("Use esta habilidade no menu de combate.");}
                else detail.add("As habilidades da Persona equipada aparecerao aqui.");
            }
            case 1 -> {
                list.add(new MenuDesign.Row("Pulse Tonic","x"+data.tonics()+"   Recupera 30% HP",-1));choiceIds.add("tonic");
                var bag=com.gx.velveteclipse.client.equipment.EquipmentScreen.bag();
                for(var supply:com.gx.velveteclipse.equipment.Supplies.all())if(bag.count(supply.id())>0){list.add(new MenuDesign.Row(supply.name(),"x"+bag.count(supply.id()),-1));choiceIds.add("s:"+supply.id());}
                for(int i=0;i<36;i++)if(!item(i).m_41619_()){list.add(new MenuDesign.Row(item(i).m_41786_().getString(),"x"+item(i).m_41613_()+"   Inventário",i));choiceIds.add("i:"+i);}
                String id=choiceIds.get(Math.min(selected,choiceIds.size()-1));
                if(id.equals("tonic")){detail.add("Recupera 30% do HP de um integrante da party.");action="Usar Pulse Tonic";}
                else if(id.startsWith("s:")){var supply=com.gx.velveteclipse.equipment.Supplies.get(id.substring(2));detail.add(supply.description());action="Usar item";}
                else{detail.add("Item do inventário Minecraft.");action="Abrir inventário";}
            }
            case 2 -> {
                var bag=com.gx.velveteclipse.client.equipment.EquipmentScreen.bag();
                if(!gearOpen){for(var slot:com.gx.velveteclipse.equipment.EquipmentCatalog.Slot.values()){
                    var gear=com.gx.velveteclipse.equipment.EquipmentCatalog.get(bag.equipped.get(slot));list.add(new MenuDesign.Row(com.gx.velveteclipse.equipment.EquipmentCatalog.label(slot),gear==null?"Vazio":gear.name(),-1));
                }action="Selecionar equipamento";}
                else{
                    var slot=com.gx.velveteclipse.equipment.EquipmentCatalog.Slot.values()[gearSlot];var currentGear=com.gx.velveteclipse.equipment.EquipmentCatalog.get(bag.equipped.get(slot));
                    for(var gear:com.gx.velveteclipse.equipment.EquipmentCatalog.all().stream().filter(g->g.slot()==slot&&bag.owned.contains(g.id())).sorted(Comparator.comparingInt(com.gx.velveteclipse.equipment.EquipmentCatalog.Gear::value).thenComparing(com.gx.velveteclipse.equipment.EquipmentCatalog.Gear::name)).toList()){
                        int delta=gear.value()-(currentGear==null?0:currentGear.value());list.add(new MenuDesign.Row(gear.name(),gear.metric()+" "+gear.value()+"  "+(delta>=0?"+":"")+delta+(gear==currentGear?"  EQUIPADO":""),-1));choiceIds.add(gear.id());
                    }
                    if(!choiceIds.isEmpty()){var gear=com.gx.velveteclipse.equipment.EquipmentCatalog.get(choiceIds.get(Math.min(selected,choiceIds.size()-1)));detail.add(com.gx.velveteclipse.client.equipment.EquipmentText.effect(gear.effect()));action="Equipar";}
                }
            }
            case 3 -> {
                for(var p:data.personas())list.add(new MenuDesign.Row(p.name(),"Lv "+p.level()+"  "+p.arcana()+(data.reserveIds().contains(p.id())?" / RESERVA":p.id().equals(data.active())?" / ATIVA":""),-1));
                if(!list.isEmpty()){var p=data.personas().get(Math.min(selected,list.size()-1));detail.add(p.name()+" / "+PersonaDetails.arcana(p.arcana()));detail.add("Lv "+p.level()+"   Próximo nível: "+PersonaDetails.remainingXp(p)+" EXP");
                    detail.add("Capacidade: "+(data.personas().size()-data.reserveIds().size())+" / "+data.personaCapacity()+" (máximo 8)");
                    detail.add("ENTER abre os detalhes. Equipe pela tela de inspeção.");action="Inspecionar Persona";}
                else {detail.add("Você ainda não despertou uma Persona.");detail.add("Crie uma Evoker e use-a com o botão direito durante a Dark Hour.");detail.add("Seu primeiro vínculo será escolhido no despertar.");}
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
                list.add(new MenuDesign.Row("Opcoes do Minecraft","Video, audio, idioma e controles",-1));list.add(new MenuDesign.Row("Animacoes do menu",motion?"Ativadas":"Movimento reduzido",-1));
                list.add(new MenuDesign.Row("Animação All-Out",com.gx.velveteclipse.core.battle.AllOutStyle.parse(data.allOutStyle()).label+" / ENTER para escolher",-1));
                list.add(new MenuDesign.Row("Voltar ao jogo","Fechar menu",-1));
                if(selected==0){detail.add("SYSTEM");detail.add("Ajuste video, audio, controles e idioma nas opcoes do Minecraft.");action="Abrir opcoes";}
                else if(selected==1){detail.add("Movimento do menu");detail.add("Alterna flutuacao do retrato, entrada das opcoes e transicoes.");detail.add("Preferencia valida nesta sessao.");action=motion?"Reduzir movimento":"Ativar animacoes";}
                else if(selected==2){detail.add("ANIMAÇÃO ALL-OUT");detail.add("Pose final ao vencer o combate.");action="Escolher animação";}
                else{detail.add("Retornar ao mundo.");action="Fechar menu";}
            }
        }
        rows=list;adjustScroll();
        return new MenuDesign.View(home,tab,selected,offset,rows,title,detail,stats,action,seconds-messageAt<5?message:"",player.m_36316_().getName(),data.yen(),data.day(),data.time(),seconds,motion,selection,seconds-started,seconds-changed);
    }
    private static String cost(SkillDefinition s){return s.spCost()>0?s.spCost()+" SP":s.hpPercentCost()>0?s.hpPercentCost()+"% HP":"Sem custo";}
    private void activate(){
        view(now());
        if(action.isEmpty())return;
        switch(tab){
            case 0 -> {var p=data.equipped();if(actor==0&&p!=null&&selected<p.skills().size()&&p.skills().get(selected).element()==Element.HEALING)recover("heal_target",p.skills().get(selected).id(),p.skills().get(selected).displayName());}
            case 1 -> {if(selected>=choiceIds.size())return;String id=choiceIds.get(selected);if(id.equals("tonic"))recover("tonic_target","tonic","Pulse Tonic");else if(id.startsWith("s:"))recover("supply_target",id.substring(2),rows.get(selected).title());else f_96541_.m_91152_(new InventoryScreen(f_96541_.f_91074_));}
            case 2 -> {if(!gearOpen){gearSlot=selected;gearOpen=true;selected=offset=0;changed=now();}else if(selected<choiceIds.size())com.gx.velveteclipse.equipment.EquipmentNetwork.request("equip",choiceIds.get(selected),"");}
            case 10 -> com.gx.velveteclipse.party.PartyNetwork.request("open","","",0,"");
            case 3 -> showPersona();
            case 5 -> {var connection=f_96541_.m_91403_();if(connection!=null)f_96541_.m_91152_(new AdvancementsScreen(connection.m_105145_()));}
            case 8 -> {if(selected==0)f_96541_.m_91152_(new OptionsScreen(this,f_96541_.f_91066_));else if(selected==1)motion=!motion;else if(selected==2)openAllOutSelect();else closeMenu();}
        }
    }
    @Override public void m_88315_(GuiGraphics g,int mouseX,int mouseY,float partial){
        if(f_96541_.f_91074_==null)return;
        double seconds=now(),dt=seconds-lastFrame;lastFrame=seconds;
        selection=motion?MenuMotion.approach(selection,tab,dt):tab;homeAmount=motion?MenuMotion.approach(homeAmount,home?1:0,dt):(home?1:0);
        double mx=(mouseX-left())/scale(),my=(mouseY-top())/scale();
        if(allOutSelectOpen&&Math.abs(mouseX-lastMouseX)+Math.abs(mouseY-lastMouseY)>1){int pick=AllOutSelectDesign.pick(mx,my);if(pick>=0&&pick!=allOutChoice){allOutChoice=pick;changed=now();}}
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
        if(home&&!opening()&&(Math.abs(mouseX-lastMouseX)+Math.abs(mouseY-lastMouseY)>1)){int pick=ReloadMenuDesign.pickHome(mx,my);if(pick>=0)tab=pick;}
        lastMouseX=mouseX;lastMouseY=mouseY;
        MenuDesign.View view=view(seconds);var pose=g.m_280168_();g.m_280509_(0,0,f_96543_,f_96544_,0xFF03143D);
        pose.m_85836_();pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);
        var canvas=new GameCanvas(g);
        try{
            g.m_286007_(() -> ReloadMenuDesign.background(canvas,view,gearOpen));
            if(home||tab<=2||tab==4||tab==8){
                String id=data.members().isEmpty()?f_96541_.f_91074_.m_20148_().toString():data.members().get(Math.min(actor,data.members().size()-1)).id();
                g.m_280588_((int)left(),(int)top(),(int)Math.ceil(left()+900*scale()),(int)Math.ceil(top()+500*scale()));
                try{ReloadPortrait.draw(g,tab,home,id,seconds,motion,seconds-started);}finally{g.m_280618_();}
            }
            if(!home&&tab==3&&!data.personas().isEmpty()){
                var p=data.personas().get(Math.min(selected,data.personas().size()-1));
                g.m_280588_((int)(left()+463*scale()),(int)top(),(int)Math.ceil(left()+900*scale()),(int)Math.ceil(top()+466*scale()));
                try{personaPreview.draw(g,new PersonaSheetDesign.View(p,p.id().equals(data.active()),selected,data.personas().size(),0,-1,1,seconds,motion,"",""));}finally{g.m_280618_();}
            }
            if(!home&&tab==9&&!data.bestiary().isEmpty()){
                var enemy=data.bestiary().get(Math.min(selected,data.bestiary().size()-1));
                g.m_280588_((int)(left()+526*scale()),(int)(top()+92*scale()),(int)Math.ceil(left()+851*scale()),(int)Math.ceil(top()+301*scale()));
                try{bestiaryPreview.draw(g,enemy,seconds,684,302,1.7);}finally{g.m_280618_();}
            }
            pose.m_85837_(0,0,MenuDepth.FOREGROUND);
            if(home)g.m_286007_(() -> ReloadMenuOpening.paint(canvas,seconds-started,motion));
            g.m_286007_(() -> ReloadMenuDesign.foreground(canvas,view,data,actor,gearOpen,gearSlot));
            if(!home&&(tab<=2&&!gearOpen||tab==4))drawPartyFaces(g);
            if(!home&&tab==8&&allOutSelectOpen){
                canvas.fill(0,0,900,500,0xEC03133C);
                var style=allOutSelectOpen?com.gx.velveteclipse.core.battle.AllOutStyle.values()[allOutChoice]:com.gx.velveteclipse.core.battle.AllOutStyle.parse(data.allOutStyle());double age=motion?90+((seconds-changed)*20)%86:145;
                var preview=AllOutSelectDesign.preview();
                canvas.fill(preview.x(),preview.y(),preview.w(),preview.h(),style.color);
                canvas.poly(new double[]{preview.x(),preview.y()+preview.h()*.3,preview.x()+preview.w(),preview.y()+preview.h()*.08,preview.x()+preview.w(),preview.y()+preview.h()*.3,preview.x(),preview.y()+preview.h()*.52},0x46031340);
                g.m_280588_((int)(left()+preview.x()*scale()),(int)(top()+preview.y()*scale()),(int)Math.ceil(left()+(preview.x()+preview.w())*scale()),(int)Math.ceil(top()+(preview.y()+preview.h())*scale()));
                try{com.gx.velveteclipse.client.AllOutPlayerRenderer.draw(g,f_96541_.f_91074_,AllOutSelectDesign.frame(),age,true,style,com.gx.velveteclipse.client.equipment.EquipmentScreen.family(),0);}finally{g.m_280618_();}
                g.m_280262_();pose.m_85836_();pose.m_85837_(0,0,350);
                try{g.m_286007_(() -> AllOutSelectDesign.paint(canvas,com.gx.velveteclipse.core.battle.AllOutStyle.parse(data.allOutStyle()),allOutSelectOpen,allOutChoice));}finally{g.m_280262_();pose.m_85849_();}
            }
            if(targetOpen){pose.m_85836_();pose.m_85837_(0,0,450);try{g.m_286007_(() -> ReloadMenuDesign.target(canvas,data.members(),targetIndex,recoveryTitle));}finally{g.m_280262_();pose.m_85849_();}}
            if(motion&&seconds-changed<.28&&!home&&!targetOpen&&!allOutSelectOpen){double p=MenuMotion.wipe(seconds-changed);double x=210+p*720;
                canvas.poly(new double[]{x-175,40,x+100,40,x-45,445,x-210,445},MenuDesign.alpha(MenuDesign.CYAN,1-p));}
            if(closing){double p=MenuMotion.ease((seconds-closedAt)/.23);canvas.poly(new double[]{-300,0,1100*p,0,1100*p-180,500,-300,500},MenuDesign.INK);}
        }finally{g.m_280262_();pose.m_85849_();}
    }
    private void drawPartyFaces(GuiGraphics g){
        var mc=Minecraft.m_91087_();if(mc.m_91403_()==null)return;int start=actor/4*4;for(int i=0;i<Math.min(4,data.members().size()-start);i++){try{var info=mc.m_91403_().m_104949_(UUID.fromString(data.members().get(start+i).id()));if(info==null)continue;g.m_280262_();g.m_280411_(info.m_105337_(),(int)(42*ReloadMenuDesign.SX),(int)((105+i*85)*ReloadMenuDesign.SY),(int)(49*ReloadMenuDesign.SX),(int)(49*ReloadMenuDesign.SY),8,8,8,8,64,64);}catch(IllegalArgumentException ignored){}}
    }
    private void drawSheet(GuiGraphics g,PersonaSheetDesign.View v){
        var pose=g.m_280168_();g.m_280509_(0,0,f_96543_,f_96544_,0xFF020A2A);pose.m_85836_();
        pose.m_85837_(left(),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);var canvas=new GameCanvas(g);
        try{
            g.m_286007_(() -> PersonaSheetDesign.background(canvas,v));
            // GUI scissor uses window GUI coordinates, independently of the current pose transform.
            g.m_280588_((int)(left()+396*scale()),(int)top(),(int)Math.ceil(left()+900*scale()),(int)Math.ceil(top()+474*scale()));
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
        public void body(String text,double x,double y,double size,int color,double angle){var p=g.m_280168_();p.m_85836_();p.m_85837_(x,y,0);p.m_252781_(new Quaternionf().rotationZ((float)Math.toRadians(angle)));p.m_85841_((float)(size/11),(float)(size/11),1);g.m_280614_(f_96547_,Component.m_237113_(text).m_130938_(style->style.m_131150_(BODY)),0,0,color,false);p.m_85849_();}
        public double width(String text,double size){return f_96547_.m_92852_(styled(text,size))*size/11;}
    public double bodyWidth(String text,double size){return Minecraft.m_91087_().f_91062_.m_92852_(Component.m_237113_(text).m_130938_(style->style.m_131150_(new ResourceLocation("velvet_eclipse","body"))))*size/11;}
    public void item(int slot,double x,double y){if(slot>=0&&slot<41)g.m_280480_(PersonaMenuScreen.this.item(slot),(int)x,(int)y);}
    }
}
