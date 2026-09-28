package com.gx.velveteclipse.core.battle;
import java.util.*;

/** Choreography inspirations, performed by the player's own skin and body. */
public enum AllOutStyle {
 MAKOTO("Makoto",0xFF05BDE9,"DONE AND DUSTED.","Giro da espada, mão junto ao rosto e silhueta em ciano."),
 YUKARI("Yukari",0xFFE9479E,"THERE YOU HAVE IT.","Passo lateral, gesto de despedida e quadro rosa."),
 JUNPEI("Junpei",0xFF6545DF,"THAT'S THE GAME!","Espada no ombro, gesto na testa e quadro violeta."),
 AKIHIKO("Akihiko",0xFF75889E,"ON TO THE NEXT BOUT.","Guarda de boxe, soco curto e ajuste das luvas."),
 MITSURU("Mitsuru",0xFFD61558,"KNEEL BEFORE ME.","Estocada, giro de perfil e postura com a lâmina baixa.");
 public final String label,motto,description;public final int color;
 AllOutStyle(String label,int color,String motto,String description){this.label=label;this.color=color;this.motto=motto;this.description=description;}
 public String id(){return name().toLowerCase(Locale.ROOT);}
 public static AllOutStyle parse(String value){for(var s:values())if(s.id().equals(value))return s;return MAKOTO;}
 public static boolean valid(String value){return Arrays.stream(values()).anyMatch(s->s.id().equals(value));}
}
