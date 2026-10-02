package com.gx.velveteclipse.client.party;
import com.gx.velveteclipse.client.menu.*;
import java.util.*;
import static com.gx.velveteclipse.client.menu.MenuDesign.*;

public final class ReloadPartyDesign {
    public record Row(String title,String detail){}
    public static Rect tab(int i){return ReloadMenuDesign.rect(24+i*303,80,294,55);}
    public static Rect row(int i){return ReloadMenuDesign.rect(37,213+i*57,737,55);}
    public static Rect action(){return ReloadMenuDesign.rect(861,555,336,55);}
    public static Rect amount(){return ReloadMenuDesign.rect(865,372,315,55);}
    public static void paint(Canvas target,int tab,List<Row> rows,int selected,int offset,int members,int limit,String recipient,String amount,boolean focus,boolean waiting,boolean confirm,String message){
        var c=ReloadMenuDesign.scaled(target);c.text("PARTY",-14,-14,149,0xB8EDF9FF,0);c.poly(new double[]{624,0,1280,0,1280,720,886,720},WHITE);c.poly(new double[]{15,150,795,101,840,688,0,718},0xEB06133D);
        String[] tabs={"INTEGRANTES","CONVIDAR","COMPARTILHAR","CONVITES"};for(int i=0;i<4;i++){double x=29+i*303;c.fill(x,86,285,44,i==tab?WHITE:INK);c.text(tabs[i],x+13,94,28,i==tab?INK:CYAN,0);}
        c.text(members==0?"SEM GRUPO":"SUA PARTY",43,155,32,WHITE,-3);c.body(members+" / "+limit+" jogadores",528,158,22,CYAN,0);
        for(int i=0;i<Math.min(7,rows.size()-offset);i++){var r=rows.get(offset+i);boolean active=offset+i==selected;double y=213+i*57;c.fill(44,y,714,52,active?WHITE:0xA0082359);if(active)c.fill(39,y,5,52,RED);c.body(ReloadMenuDesign.fitBody(c,r.title(),679,29),62,y+4,29,active?INK:WHITE,0);c.body(ReloadMenuDesign.fitBody(c,r.detail(),679,16),64,y+34,16,active?INK:CYAN,0);}
        if(rows.isEmpty())c.body(tab==3?"Nenhum convite pendente.":"Nenhum jogador disponível.",51,249,29,CYAN,0);
        c.text(tab==2?"COMPARTILHAR":"GRUPO",870,201,43,INK,-3);c.body(tab==2&&!recipient.isEmpty()?ReloadMenuDesign.fitBody(c,recipient,322,28):"Explorem juntos",869,277,28,INK,0);
        if(tab==2&&!recipient.isEmpty()){c.body("QUANTIDADE",870,338,22,INK,0);c.fill(865,372,315,55,focus?0xFF163F96:INK);c.body(amount.isEmpty()?"0":amount,882,380,32,WHITE,0);c.body("Digite ou use as setas",869,445,21,INK,0);}
        else {c.body("Mapa compartilhado",869,343,24,INK,0);c.body("Itens e yen entre aliados",869,382,22,INK,0);}
        c.fill(861,555,336,55,CYAN);c.body(waiting?"Aguarde…":confirm?"ENTER Confirmar":"ENTER Selecionar",878,571,25,INK,0);
        if(confirm&&!rows.isEmpty()){c.fill(35,207,735,401,0xFC071738);c.text("CONFIRMAR?",66,242,53,CYAN,0);c.body(ReloadMenuDesign.fitBody(c,rows.get(selected).title(),630,34),66,333,34,WHITE,0);if(tab==2)c.body(amount+" para "+recipient,66,390,28,CYAN,0);c.body("ESC Cancelar · ENTER Confirmar",66,539,25,WHITE,0);}
        if(!message.isBlank()){c.fill(24,643,1196,34,INK);c.body(ReloadMenuDesign.fitBody(c,message,1170,21),35,648,21,CYAN,0);}
        c.body("Q / E Aba   Setas Selecionar   ENTER Ação   R Atualizar   ESC Voltar",27,687,21,WHITE,0);
    }
}
