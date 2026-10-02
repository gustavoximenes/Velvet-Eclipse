package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.client.party.ReloadPartyDesign;
import com.gx.velveteclipse.client.presentation.ReloadBattleDesign;
import com.gx.velveteclipse.client.darkhour.DeadlineLayout;
import com.gx.velveteclipse.darkhour.PausableCountdown;
import com.gx.velveteclipse.exploration.*;
import com.gx.velveteclipse.menu.*;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.equipment.*;
import net.minecraft.world.entity.ai.attributes.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.nio.file.*;
import java.util.zip.GZIPInputStream;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/** Focused offline checks; the exported layers call the production layout directly. */
public final class ReloadMenuAudit {
    private static int checks;
    private static void check(boolean ok,String detail){checks++;if(!ok)throw new AssertionError(detail);}
    private static long ms(long value){return value*1_000_000;}
    public static void main(String[] args)throws Exception{
        var clock=new PausableCountdown();clock.synchronize(10_000,ms(100),false);
        check(clock.remaining(ms(600),false)==9500,"normal countdown interpolation");
        check(clock.remaining(ms(650),true)==9500,"pause transition freezes displayed sample");
        check(clock.remaining(ms(600_000),true)==9500,"long pause cannot expire the clock");
        check(clock.remaining(ms(600_050),false)==9500,"first resumed sample excludes pause interval");
        check(clock.remaining(ms(600_550),false)==9000,"countdown resumes at normal rate");
        clock.synchronize(12000,ms(600_551),true);check(clock.remaining(ms(900_000),true)==12000,"sync during pause remains frozen");
        clock.remaining(ms(900_001),false);check(clock.remaining(ms(999_999),false)==0,"expiration clamped at zero");
        clock.synchronize(-1,ms(10),false);check(clock.remaining(ms(9),false)==0,"negative sample and backward clock safe");
        var server=new org.objectweb.asm.tree.ClassNode();
        try(var in=ReloadMenuAudit.class.getResourceAsStream("/net/minecraft/client/server/IntegratedServer.class")){new org.objectweb.asm.ClassReader(in).accept(server,0);}
        var ticks=server.methods.stream().filter(m->m.name.equals("m_5705_")&&m.desc.equals("(Ljava/util/function/BooleanSupplier;)V")).toList();
        check(ticks.size()==1,"pause mixin target exists exactly once in native server");
        boolean nativePause=false;for(var instruction:ticks.get(0).instructions)if(instruction instanceof org.objectweb.asm.tree.MethodInsnNode call&&call.owner.equals("net/minecraft/client/Minecraft")&&call.name.equals("m_91104_"))nativePause=true;
        check(nativePause,"deadline hook consults the native integrated-server pause condition");

        var speed=new AttributeInstance(new RangedAttribute("audit.speed",.1,0,10),a->{});
        speed.m_22118_(new AttributeModifier(UUID.randomUUID(),"vanilla sprint",.3,AttributeModifier.Operation.MULTIPLY_TOTAL));
        double baseline=speed.m_22135_();
        for(int i=0;i<500;i++)ExplorationSprint.update(speed,true);
        check(Math.abs(speed.m_22135_()-baseline*1.2)<1e-8,"20 percent bonus does not stack across ticks");
        check(!speed.m_22127_(UUID.fromString("9e9c7295-2e08-443e-90fb-62961e2bfdea")),"sprint modifier is transient, not permanent");
        ExplorationSprint.update(speed,false);check(Math.abs(speed.m_22135_()-baseline)<1e-8,"normal sprint restored without erasing other modifiers");
        for(var mode:ExplorationRules.Mode.values())for(boolean running:new boolean[]{false,true})check(ExplorationSprint.eligible(mode,running)==(mode==ExplorationRules.Mode.EXPLORATION&&running),"only H exploration sprint gets bonus");
        for(int[] size:new int[][]{{320,180},{427,240},{640,360},{900,500},{1280,720}}){
            int w=size[0],h=size[1];double u=ReloadBattleDesign.unit(w,h);
            check(DeadlineLayout.x(w,true)+132<=w&&DeadlineLayout.x(w,true)>w/2.,"battle countdown anchored right");
            check(DeadlineLayout.x(w,false)==16,"exploration countdown retains left position");
            for(int count=1;count<=8;count++)for(int index=0;index<count;index++){
                var box=ReloadBattleDesign.party(w,h,count,index,true);
                check(box.y()-13*u>=50,"portrait decoration below countdown");
                check(box.y()+51*u<=h,"party panel within vertical viewport");
            }
        }
        var from=new PlayerPersonaData();var to=new PlayerPersonaData();var bag=EquipmentLoadout.starter();
        from.givePersonaInstance(new PersonaInstance("izanagi",20,0,Set.of("dia","media","zio")));
        to.setResources(10,to.maxSp());int sp=from.sp();
        check(MenuRecovery.apply(from,to,bag,"heal_target","dia").changed(),"owned healing skill works on recipient");
        check(to.hp()==60&&from.sp()==sp-3,"recipient receives health and source pays SP once");
        from.setResources(10,20);check(MenuRecovery.apply(from,from,bag,"heal_target","dia").changed()&&from.hp()==60&&from.sp()==17,"self heal shares one state without losing payment");
        to.setResources(to.maxHp(),to.maxSp());sp=from.sp();
        check(!MenuRecovery.apply(from,to,bag,"heal_target","dia").changed()&&from.sp()==sp,"full target consumes no SP");
        bag.addSupply("medicine");check(!MenuRecovery.apply(from,to,bag,"supply_target","medicine").changed()&&bag.count("medicine")==1,"full target consumes no item");
        to.setResources(1,to.maxSp());check(MenuRecovery.apply(from,to,bag,"supply_target","medicine").changed()&&to.hp()==51&&bag.count("medicine")==0,"supply consumed exactly once");
        check(!MenuRecovery.apply(from,to,bag,"supply_target","medicine").changed()&&to.hp()==51,"missing item cannot heal");
        check(!MenuRecovery.apply(from,to,bag,"heal_target","diarahan").changed(),"unlearned skill rejected");
        from.setResources(from.hp(),0);check(!MenuRecovery.apply(from,to,bag,"heal_target","dia").changed(),"insufficient SP rejected");
        int tonics=from.pulseTonics();check(MenuRecovery.apply(from,to,bag,"tonic_target","tonic").changed()&&from.pulseTonics()==tonics-1,"tonic targets party member and consumes once");
        to.setResources(0,0);tonics=from.pulseTonics();check(!MenuRecovery.apply(from,to,bag,"tonic_target","tonic").changed()&&from.pulseTonics()==tonics,"dead target cannot consume recovery");

        var p=MenuService.describe(from.activeInstance());var p2=MenuService.describe(new PersonaInstance(PersonaRegistry.persona("jack_frost").orElseThrow()));
        var members=List.of(new MenuSnapshot.Member(new UUID(0,1).toString(),"Jogador",25,88,120,37,45,p),new MenuSnapshot.Member(new UUID(0,2).toString(),"Aliado",23,110,140,46,60,p2));
        var data=new MenuSnapshot(88,120,37,45,3,p.id(),List.of(p,p2),List.of(),List.of(),14,5000,"",25,600,1024,47407,List.of(),8,Set.of(),"makoto",members);
        var sheet=new PersonaSheetState();check(sheet.show(data.personas(),1)&&sheet.id().equals(p2.id()),"selector opens requested legacy inspector");
        check(data.active().equals(p.id()),"inspection does not equip");sheet.close();check(!sheet.open(),"back leaves inspector");
        for(int tab=0;tab<11;tab++)for(boolean gear:new boolean[]{false,true})for(int i=0;i<ReloadMenuDesign.visibleRows(tab,gear);i++){
            var r=ReloadMenuDesign.row(tab,i,gear);check(ReloadMenuDesign.pickRow(tab,gear,r.x()+r.w()/2,r.y()+r.h()/2)==i,"drawn rows have matching mouse targets");
        }
        for(String pose:List.of("main","skill","item","equip","roster","status","system"))for(String arms:List.of("slim","wide")){
            Path path=Path.of("src/main/resources/assets/velvet_eclipse/menu/"+pose+"_"+arms+".bin.gz");
            try(var in=new DataInputStream(new GZIPInputStream(Files.newInputStream(path)))){
                check(in.readInt()==0x56454d50&&in.readInt()==1,"pose format");int faces=in.readInt(),frames=in.readInt();check(faces>0&&faces<=1000&&frames==3,"bounded pose size");
                byte[] binding=in.readNBytes(faces);check(binding.length==faces,"texture bindings complete");boolean evoker=false;for(byte b:binding){check(b==0||b==1,"only live skin/native Evoker bindings");evoker|=b==1;}check(evoker==pose.equals("skill"),"Evoker belongs to Skill pose");
                for(int i=0;i<faces*frames*28;i++)check(Float.isFinite(in.readFloat()),"finite rig coordinate");check(in.read()==-1,"no extra pose bytes");
            }
        }
        export(data);System.out.println("PASS reload menu: "+checks+" focused checks; paused clock, sprint lifecycle, right HUD, recovery transactions, inspector selection, hit targets, native poses. Offline visual layers exported; no in-game testing.");
    }
    private static void export(MenuSnapshot data)throws Exception{
        Path out=Path.of("build/reports/reload-menu");Files.createDirectories(out);
        for(int tab=-1;tab<11;tab++){
            String name=tab<0?"main":new String[]{"skill","item","equip","persona","stats","quest","social","calendar","system","bestiary","party"}[tab];
            var rows=new ArrayList<MenuDesign.Row>();
            if(tab==3)for(var p:data.personas())rows.add(new MenuDesign.Row(p.name(),"Lv "+p.level(),-1));
            else if(tab==8)for(String title:List.of("Opções do Minecraft","Animações do menu","Animação All-Out","Voltar ao jogo"))rows.add(new MenuDesign.Row(title,"Configuração",-1));
            else if(tab==2)for(String title:List.of("Arma","Armadura","Calçado","Acessório"))rows.add(new MenuDesign.Row(title,"Equipamento atual",-1));
            else for(int i=0;i<8;i++)rows.add(new MenuDesign.Row(tab==0?new String[]{"Dia","Media","Zio","Mazio","Cleave","Rakukaja","Tarunda","Agi"}[i]:"Registro "+(i+1),tab==0?"Cura / 3 SP":"Dados do jogador",-1));
            var v=new MenuDesign.View(tab<0,Math.max(0,tab),0,0,rows,"",List.of("Descrição da seleção","Dados reais sincronizados pelo servidor"),null,"","","Jogador",47407,14,5000,0,false,0,10,10);
            var bg=new Preview();ReloadMenuDesign.background(bg,v,false);bg.save(out.resolve(name+"-background.png"));
            var fg=new Preview();if(tab==10)ReloadPartyDesign.paint(fg,0,List.of(new ReloadPartyDesign.Row("Jogador","Líder"),new ReloadPartyDesign.Row("Aliado","Online")),0,0,2,4,"","1",false,false,false,"");else ReloadMenuDesign.foreground(fg,v,data,0,false,0);fg.save(out.resolve(name+"-foreground.png"));
        }
    }
    static final class Preview implements MenuDesign.Canvas{
        final BufferedImage im=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);final Graphics2D g=im.createGraphics();final Font display,body;
        Preview()throws Exception{g.scale(1280./900,720./500);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);Path fonts=Path.of("src/main/resources/assets/velvet_eclipse/font");display=Font.createFont(Font.TRUETYPE_FONT,fonts.resolve("display.ttf").toFile());body=Font.createFont(Font.TRUETYPE_FONT,fonts.resolve("body.ttf").toFile());}
        public void fill(double x,double y,double w,double h,int c){g.setColor(new Color(c,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
        public void poly(double[] p,int c){var path=new Path2D.Double();path.moveTo(p[0],p[1]);for(int i=2;i<p.length;i+=2)path.lineTo(p[i],p[i+1]);path.closePath();g.setColor(new Color(c,true));g.fill(path);}
        private void glyph(String s,double x,double y,double size,int c,double angle,Font font){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setColor(new Color(c,true));g.setFont(font.deriveFont((float)size));g.drawString(s,0,g.getFontMetrics().getAscent());g.setTransform(old);}
        public void text(String s,double x,double y,double size,int c,double angle){glyph(s,x,y,size,c,angle,size>=20?display:body);}
        public void body(String s,double x,double y,double size,int c,double angle){glyph(s,x,y,size,c,angle,body);}
        public double width(String s,double size){return g.getFontMetrics((size>=20?display:body).deriveFont((float)size)).stringWidth(s);}
        public double bodyWidth(String s,double size){return g.getFontMetrics(body.deriveFont((float)size)).stringWidth(s);}
        public void item(int slot,double x,double y){}
        void save(Path file)throws Exception{g.dispose();ImageIO.write(im,"png",file.toFile());}
    }
}
