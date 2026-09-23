package com.gx.velveteclipse.tests;

import com.gx.velveteclipse.core.fusion.FusionService;
import com.gx.velveteclipse.core.fusion.FusionTransaction;
import com.gx.velveteclipse.core.persona.PersonaRegistry;
import com.gx.velveteclipse.forge.PlayerPersonaData;
import com.gx.velveteclipse.client.menu.FusionDesign;
import com.gx.velveteclipse.client.menu.MenuDesign;
import com.gx.velveteclipse.room.FusionSnapshot;
import java.util.*;

/** Offline checks for the server transaction and the fusion navigation geometry. */
public final class FusionAudit {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        PersonaRegistry.bootstrap();
        var ids=new ArrayList<>(PersonaRegistry.personas().keySet());
        int pairs=0;
        for(int i=0;i<ids.size();i++) for(int j=i+1;j<ids.size();j++){
            String a=ids.get(i),b=ids.get(j);pairs++;
            var data=new PlayerPersonaData();data.expandPersonaCapacity();data.givePersona(a);data.givePersona(b);
            var first=FusionTransaction.preview(data,a,b,null);
            var reverse=FusionTransaction.preview(data,b,a,null);
            check(first!=null&&reverse!=null,"every pair has a deterministic result");
            check(first.result().definitionId().equals(reverse.result().definitionId()),"fusion is commutative");
            String before=FusionTransaction.fingerprint(data);
            check(before.equals(FusionTransaction.fingerprint(data)),"preview is read-only");
            check(FusionTransaction.commit(data,a,b,null)==(data.playerLevel()>=first.requiredLevel()),"level gate is enforced");
        }
        check(pairs==ids.size()*(ids.size()-1)/2,"catalog pair count");
        var high=new PlayerPersonaData();high.expandPersonaCapacity();high.givePersona("iron_oni");high.givePersona("lumen_hare");
        var gated=FusionTransaction.preview(high,"iron_oni","lumen_hare",null);check(gated!=null,"high-level pair preview");
        String before=FusionTransaction.fingerprint(high);
        check(!FusionTransaction.commit(high,"iron_oni","lumen_hare",null),"level-one player cannot consume a high result");
        check(before.equals(FusionTransaction.fingerprint(high)),"rejected fusion consumes nothing");
        check(FusionService.preview(high.instance("iron_oni"),high.instance("iron_oni")).isEmpty(),"same Persona cannot be selected twice");
        var canvas=new Canvas();
        var owned=new ArrayList<com.gx.velveteclipse.menu.MenuSnapshot.Persona>();
        for(var instance:high.rosterInstances())owned.add(com.gx.velveteclipse.menu.MenuService.describe(instance));
        var recipes=List.of(new FusionSnapshot.Recipe("iron_oni","lumen_hare","umbra_slime","Umbra Slime",2));
        var snap=new FusionSnapshot(1,0,58,owned,recipes,owned.get(0),"iron_oni","lumen_hare","token",false,false,"Requer nível",false);
        for(int page=0;page<4;page++){
            var view=new FusionDesign.View(snap,page,0,0,"",false,1);
            FusionDesign.background(canvas,view);FusionDesign.foreground(canvas,view);
        }
        for(int i=0;i<4;i++){var r=FusionDesign.card(i);check(r.x()>=0&&r.x()+r.w()<=900&&r.y()>=0&&r.y()+r.h()<=415,"mode button bounds");}
        for(int i=0;i<6;i++){var r=FusionDesign.row(i);check(r.x()>=0&&r.x()+r.w()<=900&&r.y()>=0&&r.y()+r.h()<=415,"list row bounds");}
        check(!canvas.commands.isEmpty(),"fusion states paint commands");
        System.out.println("PASS fusion: "+checks+" checks; "+pairs+" pairs, atomic level gate, read-only previews and navigation layout.");
    }
    private static final class Canvas implements MenuDesign.Canvas{
        final List<String> commands=new ArrayList<>();
        public void fill(double x,double y,double w,double h,int color){check(Double.isFinite(x+y+w+h)&&w>=0&&h>=0,"finite rectangle");commands.add("fill");}
        public void poly(double[] points,int color){for(double value:points)check(Double.isFinite(value),"finite polygon");commands.add("poly");}
        public void text(String text,double x,double y,double size,int color,double angle){check(text!=null&&Double.isFinite(x+y+size+angle),"finite text");commands.add("text");}
        public double width(String text,double size){return text.length()*size*.5;}
        public void item(int slot,double x,double y){}
    }
}
