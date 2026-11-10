package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.roguelike.*;
import com.gx.velveteclipse.client.menu.*;
import com.gx.velveteclipse.localization.DisplayText;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import com.google.gson.GsonBuilder;

/** Asset writing is an explicit authoring command. Audits only export into build/reports. */
public final class ArcanaUpPreview {
    public static void main(String[] args)throws Exception {
        if(args.length==1&&args[0].equals("--assets"))assets();
        else export(Path.of("build/reports/arcana-up"));
    }
    private static void assets()throws Exception {
        var root=Path.of("src/main/resources/assets/velvet_eclipse");
        var json=new GsonBuilder().setPrettyPrinting().create();DisplayText.language("en_us");
        for(var card:ArcanaUpCatalog.CARDS){
            var c=new RoguePreview.Canvas(64,64,1);RogueDesign.upTarot(c,card,15,4,34,56);c.save(root.resolve("textures/item/arcana_up_"+card.id()+".png"));
            Files.writeString(root.resolve("models/item/arcana_up_"+card.id()+".json"),json.toJson(Map.of("parent","minecraft:item/generated","textures",Map.of("layer0","velvet_eclipse:item/arcana_up_"+card.id()))));
        }
        System.out.println("Authored 23 Arcana Up item textures/models. No JAR.");
    }
    static void export(Path out)throws Exception {
        Files.createDirectories(out);var ranks=new LinkedHashMap<String,Integer>();
        for(int i=0;i<ArcanaUpCatalog.CARDS.size();i++)ranks.put(ArcanaUpCatalog.CARDS.get(i).id(),i%11);
        for(String lang:List.of("en_us","pt_br")){
            DisplayText.language(lang);
            for(boolean enabled:new boolean[]{true,false}){
                var s=new RogueNetwork.State(false,enabled,true,true,"dps_magic",List.of("magician"),List.of("magician"),"","","",ranks);
                for(int selected:new int[]{0,12,22}){
                    var c=new RoguePreview.Canvas(1800,1000,2);RogueDesign.paint(c,new RogueDesign.View(s,3,selected,selected+4,false));c.save(out.resolve(lang+"-ranks-"+selected+"-"+enabled+".png"));
                }
            }
            for(int rank:new int[]{0,9,10}){
                var s=new RogueNetwork.State(true,true,true,true,"dps_magic",List.of(),List.of(),"","up:magician","preview",Map.of("magician",rank));
                var c=new RoguePreview.Canvas(1800,1000,2);RogueDesign.paint(c,new RogueDesign.View(s,3,1,0,false));c.save(out.resolve(lang+"-confirm-"+rank+".png"));
            }
            var s=new RogueNetwork.State(false,true,true,true,"none",List.of(),List.of(),"","","",ranks);
            var c=new RoguePreview.Canvas(1800,1000,2);RogueDesign.paint(c,new RogueDesign.View(s,2,0,2,false));c.save(out.resolve(lang+"-system.png"));
        }
        DisplayText.language("en_us");var sheet=new BufferedImage(1536,768,BufferedImage.TYPE_INT_ARGB);var g=sheet.createGraphics();
        for(int i=0;i<ArcanaUpCatalog.CARDS.size();i++){
            var card=ArcanaUpCatalog.CARDS.get(i);var c=new RoguePreview.Canvas(256,192,1);c.fill(0,0,256,192,RogueDesign.NAVY);RogueDesign.upTarot(c,card,86,8,84,140);c.body(card.name(),24,160,16,RogueDesign.CREAM,0);c.g.dispose();g.drawImage(c.image,(i%6)*256,(i/6)*192,null);
        }
        g.dispose();ImageIO.write(sheet,"png",out.resolve("cards.png").toFile());
    }
}
