package com.gx.velveteclipse.tests;

import java.util.*;import java.util.List;import java.nio.file.*;import java.io.*;import java.util.zip.GZIPInputStream;
import java.awt.*;import java.awt.geom.*;import java.awt.image.BufferedImage;import javax.imageio.ImageIO;
import com.gx.velveteclipse.city.*;
import com.gx.velveteclipse.client.city.*;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.equipment.*;
import com.gx.velveteclipse.darkhour.TartarusLayout;
import com.google.gson.*;

/** Production shop drawing and exported Minecraft collision/material data, not HTML fixtures. */
public final class CityPolishAudit {
    static int checks;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static class Canvas extends PersonaFramingAudit.Canvas {
        Canvas(Graphics2D g){super(g);}
        @Override public void body(String t,double x,double y,double size,int color,double angle){var old=g.getTransform();g.translate(x,y);g.rotate(Math.toRadians(angle));g.setFont(PersonaFramingAudit.body.deriveFont((float)size));g.setColor(new Color(color,true));g.drawString(t,0,g.getFontMetrics().getAscent()*.8f);g.setTransform(old);}
        @Override public double bodyWidth(String t,double size){return PersonaFramingAudit.body.deriveFont((float)size).getStringBounds(t,g.getFontRenderContext()).getWidth();}
    }
    public static void main(String[] args)throws Exception {
        PersonaRegistry.bootstrap();var out=Path.of("build/reports/city-polish");Files.createDirectories(out);var assets=Path.of("src/main/resources/assets/velvet_eclipse");
        PersonaFramingAudit.display=Font.createFont(Font.TRUETYPE_FONT,assets.resolve("font/display.ttf").toFile());PersonaFramingAudit.body=Font.createFont(Font.TRUETYPE_FONT,assets.resolve("font/body.ttf").toFile());
        for(var recipe:CityShopOffers.RECIPES){check(PersonaRegistry.skill(recipe.output()).isPresent(),"Real skill card output");for(var e:recipe.materials().entrySet())check(Supplies.get(e.getKey())!=null&&e.getValue()>0,"Real barter material");}
        var wallet=new com.gx.velveteclipse.forge.PlayerPersonaData();wallet.addYen(100000);var bag=EquipmentLoadout.starter();long yen=wallet.yen();int count=bag.count("medicine");
        for(int quantity:new int[]{-1,0,100,Integer.MAX_VALUE}){CityShopOffers.buyQuantity(bag,wallet,"medicine",quantity);check(wallet.yen()==yen&&bag.count("medicine")==count,"Invalid quantity cannot charge or grant");}
        CityShopOffers.buyQuantity(bag,wallet,"medicine",3);check(wallet.yen()==yen-900&&bag.count("medicine")==count+3,"Atomic multi-unit purchase");
        bag.supplies.put("medicine",999);yen=wallet.yen();CityShopOffers.buyQuantity(bag,wallet,"medicine",1);check(wallet.yen()==yen&&bag.count("medicine")==999,"Full inventory preserves payment");
        CityShopOffers.sellStored(bag,wallet,"shortsword");check(wallet.yen()==yen&&bag.owned.contains("shortsword"),"Equipped weapon protected from sale");
        var gear=EquipmentCatalog.all().stream().filter(g->g.sell()>0&&!g.id().equals("shortsword")).findFirst().orElseThrow();bag.grant(gear.id());CityShopOffers.sellStored(bag,wallet,gear.id());check(wallet.yen()==yen+gear.sell()&&!bag.owned.contains(gear.id()),"Real stored equipment sale");yen=wallet.yen();CityShopOffers.sellStored(bag,wallet,gear.id());check(wallet.yen()==yen,"Repeated sale cannot pay twice");
        wallet.spendYen(wallet.yen());bag.supplies.clear();CityShopOffers.buyQuantity(bag,wallet,"medicine",1);check(wallet.yen()==0&&bag.count("medicine")==0,"Insufficient funds cannot grant items");
        var maximumRows=new ArrayList<CityNetwork.Row>(CityCatalog.rows("police",9));for(var g:EquipmentCatalog.all())maximumRows.add(new CityNetwork.Row(g.id(),g.name(),"Equipamento guardado · venda definitiva",g.sell(),"sell",1,g.metric(),g.value(),0));
        String payload=new Gson().toJson(new CityNetwork.View("police","Tatsumi East Police","00000000-0000-0000-0000-000000000000","",9999999,maximumRows,true,true));check(payload.length()<CityNetwork.MAX_VIEW_CHARS,"Full unlocked catalog plus every owned equipment fits the network response");
        int images=0;
        for(String shop:CityCatalog.SHOPS){var rows=new ArrayList<CityNetwork.Row>(CityCatalog.rows(shop,0));
            if(shop.equals("police")){rows.clear();for(String title:List.of("Comprar armas","Comprar armaduras","Comprar calçados","Comprar acessórios","Vender equipamentos"))rows.add(new CityNetwork.Row(title,title,"",0));}
            if(shop.equals("mayoido"))rows=new ArrayList<>(List.of(new CityNetwork.Row("forge","Fundir equipamento","",0),new CityNetwork.Row("cards","Trocar cartas","",0),new CityNetwork.Row("items","Comprar itens","",0)));
            if(shop.equals("blue"))rows=new ArrayList<>(List.of(new CityNetwork.Row("host","Jogador do mundo","Selecione uma habilidade natural para relembrar.",0)));
            if(shop.equals("kenneth"))rows.add(new CityNetwork.Row("leave","Sair da vitrine","",0));
            var view=new CityShopDesign.View(shop,CityCatalog.NAMES.get(CityCatalog.SHOPS.indexOf(shop)),"root","",rows,List.of(),0,0,47407,"",false,1,false,"");
            render(view,assets,out.resolve(shop+".png"));images++;
            for(int i=0;i<Math.min(rows.size(),CityShopDesign.box(view).count());i++){var box=CityShopDesign.box(view);check(CityShopDesign.pick(view,box.x()+20,box.y()+i*box.step()+10)==i,"Native pointer matches visible row "+shop);}
            check(CityShopDesign.pick(view,0,0)==-1,"No invisible root hit region");
            if(shop.equals("aohige")){render(new CityShopDesign.View(shop,view.title(),"root","",rows,List.of(),0,0,47407,"",true,3,false,""),assets,out.resolve("aohige-confirmacao.png"));images++;}
        }
        for(String shop:List.of("police","mayoido","blue")){
            var rows=shop.equals("police")?CityCatalog.rows(shop,0):List.of(new CityNetwork.Row("bufu","Carta Bufu","Gema Bufula: 2 / 2",0,"cards",2,"",0,0));
            if(shop.equals("blue"))rows=List.of(new CityNetwork.Row("pixie/dia","Dia","Pixie · Recupera HP de um aliado.",3000,"skill:host",0,"",0,0));
            var view=new CityShopDesign.View(shop,"","list",shop.equals("police")?"Armas":"Trocar cartas",rows,rows,0,0,47407,"",false,1,false,"Jogador do mundo");render(view,assets,out.resolve(shop+"-detalhes.png"));images++;
        }
        // Exact packed vertex stride, local positions, normals, emission and UV bounds.
        int expected=0,lit=0,glass=0;
        try(var in=new DataInputStream(new GZIPInputStream(Files.newInputStream(assets.resolve("city/architecture.bin.gz"))))){
            for(int id=0;id<CityArchitecture.size();id++){var tile=CityArchitecture.tile(id);check(tile.offset()==expected&&tile.length()%39==0,"Continuous triangle index");expected+=tile.length();
                if(tile.boxes().length>65){var shape=CityArchitecture.shape(id);check(!shape.m_83281_(),"Complex exported collision builds a real Minecraft VoxelShape");}
                for(var box:tile.boxes()){check(box.length==6,"AABB stride");for(int a=0;a<3;a++)check(box[a]>=0&&box[a+3]<=1&&box[a]<box[a+3],"Collision contained in its visible block");}
                for(int n=0;n<tile.length();n+=13){float[] v=new float[13];for(int k=0;k<13;k++)v[k]=in.readFloat();for(int k=0;k<3;k++)check(Float.isFinite(v[k])&&v[k]>=-1e-5&&v[k]<=1.00001,"No out-of-block rendering");check(v[9]>=0&&v[9]<28,"Material atlas index");check(v[11]>=-.0001&&v[11]<=1.0001&&v[12]>=-.0001&&v[12]<=1.0001,"Atlas UV clamp is not hiding wrapped textures");if(v[9]==18)glass++;if(v[10]>=.2)lit++;}
            }check(in.read()==-1,"No stale trailing model buffer");
        }
        check(glass>0&&lit>0,"Real translucent and emissive surfaces");
        var tower=TartarusLayout.create(240,1);check(tower.stream().allMatch(c->CityNightSchool.contains(c.x(),c.z()+CityNightSchool.CENTER_Z)),"Entire night tower inside rollback/protection campus");check(tower.stream().anyMatch(c->c.material()==4),"Usable Tartarus entrance");
        var state=new CityNightSchool.State();var field=CityNightSchool.State.class.getDeclaredField("journal");field.setAccessible(true);@SuppressWarnings("unchecked") var journal=(Map<Long,net.minecraft.nbt.CompoundTag>)field.get(state);var row=new net.minecraft.nbt.CompoundTag();row.m_128356_("pos",37);var inventory=new net.minecraft.nbt.CompoundTag();inventory.m_128359_("custom_name","School storage");row.m_128365_("entity",inventory);journal.put(37L,row);var saved=state.m_7176_(new net.minecraft.nbt.CompoundTag());var load=CityNightSchool.State.class.getDeclaredMethod("load",net.minecraft.nbt.CompoundTag.class);load.setAccessible(true);var resumed=(CityNightSchool.State)load.invoke(null,saved);check(field.get(resumed).equals(journal),"Restoration journal preserves block entity payload across restart");
        // Night minimap uses the same geographical bounds as the daytime world map.
        var map=ImageIO.read(assets.resolve("textures/gui/city/map.png").toFile());var g=map.createGraphics();var layer=CityMapLayout.world();g.setColor(new Color(0xff0c3526,true));int x0=(int)(layer.u(CityNightSchool.MIN_X)*512),z0=(int)(layer.v(CityNightSchool.MIN_Z)*512);g.fillRect(x0,z0,(int)((CityNightSchool.MAX_X-CityNightSchool.MIN_X+1)*512./(layer.maxX()-layer.minX()+1)),(int)((CityNightSchool.MAX_Z-CityNightSchool.MIN_Z+1)*512./(layer.maxZ()-layer.minZ()+1)));g.setColor(new Color(0xff70efb8,true));for(var c:tower)if(c.y()>=0&&c.y()<10)g.fillRect((int)(layer.u(c.x())*512),(int)(layer.v(c.z()+CityNightSchool.CENTER_Z)*512),2,2);g.dispose();ImageIO.write(map,"png",assets.resolve("textures/gui/city/map_night.png").toFile());
        Files.writeString(out.resolve("validation.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("checks",checks,"shopImages",images,"localTiles",CityArchitecture.size(),"translucentVertices",glass,"emissiveVertices",lit,"nightTowerBlocks",tower.size(),"inGame",false)));
        System.out.println("PASS city polish: "+checks+" checks, "+images+" native shop previews, local geometry and night restoration journal");
    }
    private static void render(CityShopDesign.View view,Path assets,Path file)throws Exception{
        var image=new BufferedImage(1280,720,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();g.setPaint(new GradientPaint(0,0,new Color(0xff47616e,true),1280,720,new Color(0xff11262e,true)));g.fillRect(0,0,1280,720);g.setColor(new Color(0xff74919c,true));for(int i=0;i<8;i++)g.drawLine(0,120+i*83,1280,120+i*83);
        var art=assets.resolve("textures/gui/city/shops/"+view.shop()+".png");if(Files.exists(art))g.drawImage(ImageIO.read(art.toFile()),0,0,null);CityShopDesign.draw(new Canvas(g),view);g.dispose();ImageIO.write(image,"png",file.toFile());
    }
}
