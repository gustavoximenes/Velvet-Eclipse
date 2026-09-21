package com.gx.velveteclipse.menu;
import java.util.List;
import com.gx.velveteclipse.core.persona.SkillDefinition;

public record MenuSnapshot(int hp,int maxHp,int sp,int maxSp,int tonics,String active,
    List<Persona> personas,List<Quest> quests,List<Link> links,long day,long time,String message,int playerLevel,int playerXp,int playerNextXp,long yen,List<Enemy> bestiary) {
    public MenuSnapshot {bestiary=bestiary==null?List.of():List.copyOf(bestiary);}
    public MenuSnapshot(int hp,int maxHp,int sp,int maxSp,int tonics,String active,List<Persona> personas,List<Quest> quests,List<Link> links,long day,long time,String message,int playerLevel,int playerXp,int playerNextXp,long yen){this(hp,maxHp,sp,maxSp,tonics,active,personas,quests,links,day,time,message,playerLevel,playerXp,playerNextXp,yen,List.of());}
    public record Enemy(String id,String name,String asset,int level,int seen,int defeated,String affinities,boolean changing) {}
    public MenuSnapshot(int hp,int maxHp,int sp,int maxSp,int tonics,String active,List<Persona> personas,List<Quest> quests,List<Link> links,long day,long time,String message,int playerLevel,int playerXp,int playerNextXp){this(hp,maxHp,sp,maxSp,tonics,active,personas,quests,links,day,time,message,playerLevel,playerXp,playerNextXp,0);}
    public MenuSnapshot(int hp,int maxHp,int sp,int maxSp,int tonics,String active,List<Persona> personas,List<Quest> quests,List<Link> links,long day,long time,String message){this(hp,maxHp,sp,maxSp,tonics,active,personas,quests,links,day,time,message,1,0,58);}
    public record Persona(String id,String name,String arcana,int level,int xp,int nextXp,int[] stats,
                          List<SkillDefinition> skills,List<String> affinities) {}
    public record Quest(String name,String description,boolean done,float progress) {}
    public record Link(String id,String name,int rank,long lastDay) {}
    public static MenuSnapshot empty(){return new MenuSnapshot(0,1,0,1,0,"",List.of(),List.of(),List.of(),1,0,"Sincronizando...");}
    public Persona equipped(){return personas.stream().filter(p->p.id().equals(active)).findFirst().orElse(null);}
}
