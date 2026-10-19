package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.localization.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.menu.MenuSnapshot;
import com.gx.velveteclipse.core.persona.*;
import com.gx.velveteclipse.equipment.*;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public final class LocalizationAudit {
    static int checks;
    static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        Path out=Path.of("build/audit/localization");Files.createDirectories(out);
        var en=new TextCatalog(TextCatalog.read("/assets/velvet_eclipse/localization/en_us.json"));
        var pt=new TextCatalog(TextCatalog.read("/assets/velvet_eclipse/localization/pt_br.json"));
        check(DisplayText.text("Equipar").equals("Equip"),"English before a preference exists");
        Path profiles=Files.createTempDirectory("velvet-language-audit-");
        try{
            Path one=profiles.resolve("one.txt"),two=profiles.resolve("two.txt");
            check(LanguagePreference.read(one).equals("en_us"),"Fresh profile defaults to English");
            LanguagePreference.save(one,"pt_br");check(LanguagePreference.read(one).equals("pt_br"),"Portuguese survives reload");
            check(LanguagePreference.read(two).equals("en_us"),"Other player profile unchanged");
            Files.writeString(two,"invalid");check(LanguagePreference.read(two).equals("en_us"),"Invalid preference defaults safely");
            try{LanguagePreference.save(one,"invalid");throw new AssertionError("invalid language accepted");}catch(IllegalArgumentException expected){}
            check(LanguagePreference.read(one).equals("pt_br"),"Rejected value preserves previous selection");
            LanguagePreference.save(one,"en_us");check(LanguagePreference.read(one).equals("en_us"),"Switch back persists");
        }finally{try(var paths=Files.list(profiles)){for(var p:paths.toList())Files.delete(p);}Files.delete(profiles);}
        var originalLanguage=Language.m_128107_();
        var message=ModText.component("Recupera 30% do HP máximo.");
        try{
            Language.m_128114_(new DisplayLanguage(originalLanguage,(key,fallback)->key.equals(ModText.LITERAL_KEY)?en.translate(fallback.replace("%%","%")).replace("%","%%"):null));
            check(message.getString().equals("Restores 30% of maximum HP."),"Network component displays translated percentage");
            var wire=Component.Serializer.m_130691_(com.google.gson.JsonParser.parseString(Component.Serializer.m_130703_(message)));
            check(wire.getString().equals(message.getString()),"Vanilla component serialization preserves translated fallback");
            Language.m_128114_(new DisplayLanguage(originalLanguage,(key,fallback)->key.equals(ModText.LITERAL_KEY)?pt.translate(fallback.replace("%%","%")).replace("%","%%"):null));
            check(message.getString().equals("Recupera 30% do HP máximo."),"Existing component refreshes immediately after language switch");
            check(wire.getString().equals(message.getString()),"Same network payload supports both languages");
            check(Component.m_237113_("Minha mensagem do chat").getString().equals("Minha mensagem do chat"),"Unmarked player chat remains unchanged");
        }finally{Language.m_128114_(originalLanguage);}
        check(en.translate("Tártaro • Andar 25").equals("Tartarus • Floor 25"),"Dynamic floor message");
        check(en.translate("Compra concluída: Medicine × 3").equals("Purchase complete: Medicine × 3"),"Dynamic shop notification");
        check(pt.translate("XmenBrasil used Bufu on Shadow for 12 damage.").equals("XmenBrasil usou Bufu em Shadow por 12 de dano."),"Dynamic battle notification");
        check(en.translate("Dano de gelo em um inimigo.").equals("Deals Ice damage to one enemy."),"Complete skill sentence uses natural word order");
        for(String loc:List.of("en_us","pt_br")){
            var catalog=loc.equals("pt_br")?pt:en;DisplayText.language(loc);
            var keys=TextCatalog.read("/assets/velvet_eclipse/lang/"+loc+".json");check(keys.size()>=1560&&keys.keySet().equals(TextCatalog.read("/assets/velvet_eclipse/lang/en_us.json").keySet()),"All item, block and keybind labels present");
            for(var skill:PersonaRegistry.skills().values()){
                check(catalog.translate(skill.id()).equals(skill.id()),"Skill ID preserved: "+skill.id());
                if(ReloadCatalogue.info(skill.id())!=null)check(catalog.translate(skill.displayName()).equals(skill.displayName()),"Skill proper name preserved: "+skill.displayName());
            }
            for(var gear:EquipmentCatalog.all())check(catalog.translate(gear.id()).equals(gear.id()),"Equipment ID preserved");
            render(out.resolve("system-"+loc+".png"),loc);
        }
        DisplayText.language("en_us");
        var originals=new LinkedHashMap<String,String>();
        var descriptions=new LinkedHashMap<String,String>();
        for(var skill:PersonaRegistry.skills().values())if(ReloadCatalogue.info(skill.id())!=null){originals.put(skill.id(),SkillDescriptions.describe(skill));descriptions.put(skill.id(),en.translate(SkillDescriptions.describe(skill)));}
        Files.writeString(out.resolve("skill-descriptions-en.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(descriptions));
        Files.writeString(out.resolve("skill-descriptions-source.json"),new com.google.gson.GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(originals));
        // Mixin descriptor must exist in the exact bundled Minecraft/Forge API.
        var node=new org.objectweb.asm.tree.ClassNode();try(var in=LocalizationAudit.class.getResourceAsStream("/net/minecraft/client/resources/language/ClientLanguage.class")){new org.objectweb.asm.ClassReader(in).accept(node,0);}
        check(node.methods.stream().filter(m->m.name.equals("m_118919_")&&m.desc.equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")).count()==1,"I18n mixin resolves its exact target");
        System.out.println("PASS localization: "+checks+" checks; System previews EN/PT and "+descriptions.size()+" skill descriptions exported.");
    }
    static void render(Path file,String loc)throws Exception{render(file,loc,3);}
    static void render(Path file,String loc,int selected)throws Exception{
        var image=new BufferedImage(1280,720,BufferedImage.TYPE_INT_RGB);var g=image.createGraphics();
        g.scale(1280./900,720./500);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        var display=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/display.ttf").toFile());
        var body=Font.createFont(Font.TRUETYPE_FONT,Path.of("src/main/resources/assets/velvet_eclipse/font/body.ttf").toFile());
        MenuDesign.Canvas c=new MenuDesign.Canvas(){
            public void fill(double x,double y,double w,double h,int col){g.setColor(new Color(col,true));g.fill(new Rectangle2D.Double(x,y,w,h));}
            public void poly(double[] xy,int col){var p=new Path2D.Double();p.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)p.lineTo(xy[i],xy[i+1]);p.closePath();g.setColor(new Color(col,true));g.fill(p);}
            void draw(String text,double x,double y,double size,int col,double angle,Font f){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setFont(f.deriveFont((float)size));g.setColor(new Color(col,true));g.drawString(DisplayText.text(text),0,g.getFontMetrics().getAscent());g.setTransform(old);}
            public void text(String t,double x,double y,double s,int col,double a){draw(t,x,y,s,col,a,s>=20?display:body);}
            public void body(String t,double x,double y,double s,int col,double a){draw(t,x,y,s,col,a,body);}
            public double width(String t,double s){return g.getFontMetrics((s>=20?display:body).deriveFont((float)s)).stringWidth(DisplayText.text(t));}
            public double bodyWidth(String t,double s){return g.getFontMetrics(body.deriveFont((float)s)).stringWidth(DisplayText.text(t));}
            public void item(int i,double x,double y){}
        };
        var rows=List.of(new MenuDesign.Row("Opcoes do Minecraft","Video, audio e controles",-1),new MenuDesign.Row("Animacoes do menu","Ativadas",-1),new MenuDesign.Row("Animação All-Out","Makoto / ENTER para escolher",-1),new MenuDesign.Row("Idioma / Language",loc.equals("pt_br")?"Português":"English",-1),new MenuDesign.Row("Dificuldade","Normal / Vale para o grupo; solo usa sua preferência.",-1),new MenuDesign.Row("Voltar ao jogo","Fechar menu",-1));
        var v=new MenuDesign.View(false,8,selected,0,rows,"Opcoes do jogo",selected==4?List.of(com.gx.velveteclipse.core.battle.BattleDifficulty.NORMAL.description,"Ajuste automático por participantes do combate.","Vale para o grupo; solo usa sua preferência."):List.of(),null,"Português / English",selected==4?"":"Idioma salvo.","XmenBrasil",47407,1,0,1,false,8,1,1);
        ReloadMenuDesign.background(c,v,false);ReloadMenuDesign.foreground(c,v,MenuSnapshot.empty(),0,false,0);
        var box=ReloadMenuDesign.row(8,3,false);check(ReloadMenuDesign.pickRow(8,false,box.x()+1,box.y()+1)==3,"Language row click matches visible layout");
        g.dispose();ImageIO.write(image,"png",file.toFile());
    }
}
