package com.gx.velveteclipse.client.menu;
import com.gx.velveteclipse.menu.MenuSnapshot.Persona;
import java.util.*;

/** Stable IDs preserve the inspected Persona when a server refresh reorders the roster. */
public final class PersonaSheetState {
    private String id="";
    private boolean open;
    private int skill,affinity=-1;
    public boolean open(){return open;}
    public String id(){return id;}
    public int skill(){return skill;}
    public int page(){return skill/8;}
    public int affinity(){return affinity;}
    public Persona persona(List<Persona> list){return list.stream().filter(p->p.id().equals(id)).findFirst().orElse(null);}
    public boolean show(List<Persona> list,int index){
        if(index<0||index>=list.size())return false;
        id=list.get(index).id();open=true;skill=0;affinity=-1;return true;
    }
    public void close(){open=false;affinity=-1;}
    public void reconcile(List<Persona> list){
        var p=persona(list);if(p==null){close();return;}
        skill=Math.max(0,Math.min(skill,p.skills().size()-1));
    }
    public int index(List<Persona> list){for(int i=0;i<list.size();i++)if(list.get(i).id().equals(id))return i;return -1;}
    public void cycle(List<Persona> list,int delta){if(!list.isEmpty())show(list,Math.floorMod(index(list)+delta,list.size()));}
    public void selectSkill(Persona p,int index){if(index>=0&&index<p.skills().size()){skill=index;affinity=-1;}}
    public void move(Persona p,int dx,int dy){
        if(affinity>=0){affinity=Math.floorMod(affinity+(dx!=0?dx:dy),PersonaDetails.ELEMENTS.length);return;}
        if(p.skills().isEmpty())return;
        // Eight slots per page, arranged in reading order, two columns and four rows.
        int index=skill;
        if(dx!=0){int partner=index+(index%2==0?1:-1);if(partner<p.skills().size())index=partner;}
        if(dy!=0){int rows=(p.skills().size()+1)/2;int row=Math.floorMod(index/2+dy,rows);index=Math.min(row*2+index%2,p.skills().size()-1);}
        skill=index;
    }
    public void step(Persona p,int delta){if(!p.skills().isEmpty()){skill=Math.floorMod(skill+delta,p.skills().size());affinity=-1;}}
    public void toggleAffinity(){affinity=affinity>=0?-1:0;}
    public void selectAffinity(int index){if(index>=0&&index<PersonaDetails.ELEMENTS.length)affinity=index;}
}
