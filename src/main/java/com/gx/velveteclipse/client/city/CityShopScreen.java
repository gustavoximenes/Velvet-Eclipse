package com.gx.velveteclipse.client.city;

import java.util.*;
import com.gx.velveteclipse.city.*;
import com.gx.velveteclipse.client.menu.MenuCanvas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Dedicated Paulownia screen; other menu layouts and their controllers stay unchanged. */
public final class CityShopScreen extends Screen {
    private CityNetwork.View data;private String page="root",category="",member="",memberName="";private int selected,offset,quantity=1,ticks;private boolean confirm,waiting;private long entered=System.nanoTime();
    public CityShopScreen(CityNetwork.View data){super(Component.m_237113_(data.title()));this.data=data;}
    public String shop(){return data.page();}
    public void update(CityNetwork.View value){data=value;confirm=waiting=false;quantity=1;bound();}
    public void error(String value){data=new CityNetwork.View(data.page(),data.title(),data.token(),value,data.yen(),data.rows(),true,true);confirm=waiting=false;}
    private CityNetwork.Row choice(String id,String name){return new CityNetwork.Row(id,name,"",0);}
    private List<CityNetwork.Row> rows(){
        if(page.equals("root"))return switch(shop()){
            case "police"->List.of(choice("WEAPON","Comprar armas"),choice("ARMOR","Comprar armaduras"),choice("FOOTWEAR","Comprar calçados"),choice("ACCESSORY","Comprar acessórios"),choice("sell","Vender equipamentos"));
            case "mayoido"->List.of(choice("forge","Fundir equipamento"),choice("cards","Trocar cartas"),choice("items","Comprar itens"));
            case "blue"->data.rows().stream().filter(r->r.group().equals("member")).toList();
            case "kenneth"->List.of(choice("leave","Sair da vitrine"));
            default->data.rows();
        };
        return data.rows().stream().filter(r->r.group().equals(shop().equals("blue")?"skill:"+member:category)).toList();
    }
    private CityShopDesign.View view(){var known=data.rows().stream().filter(r->r.group().equals("known:"+member)).toList();return new CityShopDesign.View(shop(),data.title(),page,title(),rows(),known,selected,offset,data.yen(),data.message(),confirm,quantity,waiting,memberName);}
    private String title(){return switch(category){case "WEAPON"->"Armas";case "ARMOR"->"Armaduras";case "FOOTWEAR"->"Calçados";case "ACCESSORY"->"Acessórios";case "sell"->"Vender equipamentos";case "forge"->"Fundir equipamento";case "cards"->"Trocar cartas";default->"Itens";};}
    private void bound(){selected=Math.max(0,Math.min(selected,rows().size()-1));int n=CityShopDesign.box(view()).count();offset=Math.max(0,Math.min(offset,Math.max(0,rows().size()-n)));if(selected<offset)offset=selected;if(selected>=offset+n)offset=selected-n+1;}
    private void back(){if(waiting)return;if(confirm){confirm=false;return;}if(!page.equals("root")){page="root";selected=offset=0;entered=System.nanoTime();return;}m_7379_();}
    private void activate(){if(waiting||rows().isEmpty())return;var row=rows().get(selected);if(row.id().equals("leave")){m_7379_();return;}
        if(page.equals("root")&&Set.of("police","mayoido","blue").contains(shop())){if(shop().equals("blue")){member=row.id();memberName=row.name();}else category=row.id();page="list";selected=offset=0;entered=System.nanoTime();return;}
        if(shop().equals("blue")&&!member.equals(Minecraft.m_91087_().f_91074_.m_20148_().toString())){error("Cada jogador confirma o próprio atendimento.");return;}
        if(!confirm){confirm=true;quantity=1;return;}String action=shop().equals("blue")?"recallSkill":category.equals("sell")?"sell":Set.of("forge","cards").contains(category)?"trade":quantityAllowed(row)?"buyqty":"buy";
        waiting=true;ticks=0;CityNetwork.request(action,action.equals("buyqty")?quantity+"|"+row.id():row.id(),data.token());
    }
    private boolean quantityAllowed(CityNetwork.Row row){return com.gx.velveteclipse.equipment.Supplies.get(row.id())!=null;}
    @Override public void m_86600_(){if(!CityScreen.available()){m_7379_();return;}if(waiting&&++ticks>100)error("Sem resposta. Interaja novamente com o balcão.");}
    @Override public boolean m_7043_(){return false;}
    @Override public boolean m_7933_(int k,int scan,int mods){if(k==256){back();return true;}if(k==257||k==335){activate();return true;}if(waiting)return true;if(confirm&&(k==262||k==263)){if(!rows().isEmpty()&&quantityAllowed(rows().get(selected)))quantity=Math.max(1,Math.min(99,quantity+(k==262?1:-1)));return true;}if(!confirm&&(k==264||k==265)){selected+=k==264?1:-1;bound();return true;}return super.m_7933_(k,scan,mods);}
    @Override public boolean m_6050_(double x,double y,double delta){if(!confirm&&!waiting){selected+=delta<0?1:-1;bound();}return true;}
    private double scale(){return Math.min(f_96543_/1280.,f_96544_/720.);}private double left(){return(f_96543_-1280*scale())/2;}private double top(){return(f_96544_-720*scale())/2;}
    @Override public boolean m_6375_(double mx,double my,int button){if(button!=0)return false;double x=(mx-left())/scale(),y=(my-top())/scale();if(waiting)return true;if(confirm){if(y>=438&&y<501){if(x>635&&x<990)activate();else if(x>295&&x<600)back();}return true;}if(y>=677&&x<300){if(x<150)activate();else back();return true;}int i=CityShopDesign.pick(view(),x,y);if(i>=0){if(i==selected)activate();else{selected=i;bound();}}return true;}
    @Override public void m_88315_(GuiGraphics g,int mx,int my,float partial){var pose=g.m_280168_();pose.m_85836_();pose.m_85837_(left()-20*scale()*Math.pow(Math.max(0,1-(System.nanoTime()-entered)/250_000_000.),3),top(),0);pose.m_85841_((float)scale(),(float)scale(),1);try{
        var c=new MenuCanvas(g);g.m_286007_(() -> c.fill(0,0,1280,720,shop().equals("mayoido")?0x70270016:0x26000424));
        if(Set.of("aohige","police","mayoido").contains(shop()))g.m_280411_(new ResourceLocation("velvet_eclipse","textures/gui/city/shops/"+shop()+".png"),0,0,1280,720,0,0,1280,720,1280,720);
        g.m_286007_(() -> CityShopDesign.draw(c,view()));
        if(shop().equals("blue")&&page.equals("root")&&!confirm&&!rows().isEmpty()){
            try{var player=Minecraft.m_91087_().f_91073_.m_46003_(UUID.fromString(rows().get(selected).id()));if(player!=null)net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsAngle(g,1040,405,76,-.15f,.02f,player);}catch(IllegalArgumentException ignored){}
        }
    }finally{g.m_280262_();pose.m_85849_();}}
}
